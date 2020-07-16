package com.machfour.macros.cli.utils

import com.machfour.macros.cli.utils.MealSpec.Companion.makeMealSpec
import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Schema
import com.machfour.macros.objects.*
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.DateStamp.Companion.currentDate
import com.machfour.macros.util.FoodPortionSpec
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.sql.SQLException
import java.util.regex.Pattern

class FileParser {
    private val errorLines: MutableMap<String, String?> = LinkedHashMap() // LinkedHashMap maintains insertion order

    fun getErrorLines(): Map<String, String?> {
        return errorLines.toMap()
    }

    companion object {
        private val mealPattern = Pattern.compile("\\[(?<mealdesc>.*)]")
        private const val quantityRegex = "(?<qty>-?[0-9]+(?:.[0-9]+)?)"
        private const val unitRegex = "(?<unit>[a-zA-Z]+)?"
        private val servingCountPattern = Pattern.compile(quantityRegex)
        private val quantityAndUnitPattern = Pattern.compile("$quantityRegex\\s*$unitRegex")

        // returns an array holding MealSpec or FoodPortionSpec objects describing the objects that should be created
        // only checked for syntax, not whether those foods/servings actually exist
        // the first item is always a MealSpec
        private fun createSpecFromLines(fileLines: List<String>): Map<MealSpec, List<FoodPortionSpec>> {
            val specMap: MutableMap<MealSpec, List<FoodPortionSpec>> = LinkedHashMap()
            // if we encounter a food line before a meal title,
            // have to instantiate a dummy meal to hold it
            var currentFpSpecs: MutableList<FoodPortionSpec>? = null
            for (index in fileLines.indices) {
                val line = fileLines[index].trim { it <= ' ' }
                val mealTitle = mealPattern.matcher(line)
                if (mealTitle.find()) {
                    // make a new meal
                    val m = makeMealSpec(mealTitle.group("mealdesc"))
                    currentFpSpecs = ArrayList()
                    specMap[m] = currentFpSpecs
                } else if (!line.isEmpty() && !line.startsWith("#")) {
                    // ignore 'comment lines' and treat anything else as a FoodPortionSpec.
                    // make a new meal if necessary
                    if (currentFpSpecs == null) {
                        val m = makeMealSpec("Unnamed meal")
                        currentFpSpecs = ArrayList()
                        specMap[m] = currentFpSpecs
                    }
                    val fpSpec = makefoodPortionSpecFromLine(line)
                    fpSpec.lineIdx = index
                    currentFpSpecs.add(fpSpec)
                }
            }
            return specMap
        }

        private fun getAllIndexNames(allFpSpecs: Collection<List<FoodPortionSpec>>): Set<String?> {
            // assume around 8 food portions per list
            val foodIndexNames: MutableSet<String?> = HashSet(8 * allFpSpecs.size)
            for (portionSpecs in allFpSpecs) {
                for (fps in portionSpecs) {
                    foodIndexNames.add(fps.foodIndexName)
                }
            }
            return foodIndexNames
        }

        // like Files.readAllLines() but for a reader input.
        // Make sure to close the reader afterwards
        @Throws(IOException::class)
        private fun readAllLines(`in`: Reader): List<String> {
            val allLines: MutableList<String> = ArrayList()
            val r = BufferedReader(`in`)
            var s = r.readLine()
            while (s != null) {
                allLines.add(s)
                s = r.readLine()
            }
            return allLines
        }

        @JvmStatic
        fun processFpSpec(fps: FoodPortionSpec, m: Meal, f: Food) {
            assert(f.indexName == fps.foodIndexName) { "Food does not match index name of spec" }
            var s: Serving? = null
            val quantity: Double
            val unit: QtyUnit?
            if (fps.isServingMode) {
                assert(fps.servingName != null && fps.servingCount != 0.0)
                if (fps.servingName == "") {
                    // default serving
                    s = f.defaultServing
                    if (s == null) {
                        fps.error = "food has no default serving"
                        return
                    }
                } else {
                    s = f.getServingByName(fps.servingName!!)
                    if (s == null) {
                        fps.error = "food has no serving named '" + fps.servingName + "'"
                        return
                    }
                }
                quantity = fps.servingCount * s.quantity
                unit = s.qtyUnit
            } else {
                // not serving mode
                assert(fps.unit != null)
                quantity = fps.quantity
                unit = fps.unit
            }
            val fpData = ColumnData(FoodPortion.table())
            fpData.put(Schema.FoodPortionTable.FOOD_ID, f.id)
            fpData.put(Schema.FoodPortionTable.SERVING_ID, s?.id)
            fpData.put(Schema.FoodPortionTable.MEAL_ID, m.id)
            fpData.put(Schema.FoodPortionTable.QUANTITY_UNIT, unit!!.abbr)
            fpData.put(Schema.FoodPortionTable.QUANTITY, quantity)
            val fp = FoodPortion.factory().construct(fpData, ObjectSource.USER_NEW)
            fp.setFood(f)
            if (s != null) {
                fp.setServing(s)
            }
            fps.createdObject = fp
        }

        // returns a FoodPortionSpec object, from the given line of text
        // input forms (whitespace ignored):
        // 1.
        //   egg         ,          60
        //    ^          ^          ^
        //index name  separator  quantity (default metric units)
        //
        // 2.
        //   egg         ,        large     ,       1
        //    ^          ^          ^               ^
        //index name  separator  serving_name   num_servings
        // (serving name must be a valid serving for the food)
        //
        // 3.
        //   egg         ,        ,       1
        //    ^          ^                ^
        //index name  separator  number of servings
        // (default serving assumed, error if no default serving registered)
        // returns null if there was an error during parsing (not a DB error)
        @JvmStatic
        fun makefoodPortionSpecFromLine(line: String): FoodPortionSpec {
            // if you don't specify an array length limit, it won't match empty strings between commas
            val tokens = line.split(",".toRegex(), 4).toTypedArray()
            for (i in tokens.indices) {
                tokens[i] = tokens[i].trim { it <= ' ' }
            }
            val spec = FoodPortionSpec()
            spec.foodIndexName = tokens[0]
            // have to use run block to be able to escape from 'when' at arbitrary points (using return@run
            run {
                when (tokens.size) {
                    1 -> {
                        // 1 of default serving
                        spec.isServingMode = true
                        spec.servingCount = 1.0
                        spec.servingName = ""
                    }
                    2 -> {
                        // vanilla food and quantity, with optional unit, defaulting to grams
                        spec.isServingMode = false
                        val quantityMatch = quantityAndUnitPattern.matcher(tokens[1])
                        if (!quantityMatch.find()) {
                            // could not understand anything
                            spec.error = "invalid quantity or unit"
                            return@run // 'break from when'
                        }
                        try {
                            spec.quantity = quantityMatch.group("qty").toDouble()
                        } catch (e: NumberFormatException) {
                            // invalid quantity
                            spec.error = "invalid quantity"
                            return@run // 'break from when'
                        }
                        val unitString = quantityMatch.group("unit")
                        if (unitString == null) {
                            spec.unit = QtyUnits.GRAMS
                        } else {
                            spec.unit = QtyUnits.fromAbbreviationNoThrow(unitString)
                            if (spec.unit == null) {
                                // invalid unit
                                spec.error = "unrecognised unit"
                                return@run // 'break from when'
                            }
                        }
                    }
                    3 -> {
                        spec.isServingMode = true
                        spec.servingName = tokens[1]
                        // get quantity, which defaults to 1 of serving if not included
                        val servingCountStr = tokens[2]
                        if (servingCountStr.isEmpty()) {
                            spec.servingCount = 1.0
                        } else {
                            val servingCountMatch = servingCountPattern.matcher(tokens[2])
                            if (servingCountMatch.find()) {
                                try {
                                    spec.servingCount = servingCountMatch.group("qty").toDouble()
                                } catch (e: NumberFormatException) {
                                    spec.error = "invalid serving count"
                                    return@run // 'break from when'
                                }
                            } else {
                                spec.error = "invalid serving count"
                                return@run // 'break from when'
                            }
                        }
                    }
                    else -> spec.error = "too many commas"
                }
            }
            return spec
        }

        private fun makeMeal(description: String, day: DateStamp): Meal {
            val mealData = ColumnData(Meal.table())
            mealData.put(Schema.MealTable.NAME, description)
            mealData.put(Schema.MealTable.DAY, day)
            return Meal.factory().construct(mealData, ObjectSource.USER_NEW)
        }
    }

    // make sure to close the reader afterwards
    @Throws(IOException::class, SQLException::class)
    fun parseFile(fileReader: Reader, db: MacrosDataSource?): List<Meal> {
        val fileLines = readAllLines(fileReader)
        // also gets list of index names to retrieve
        val mealSpecs = createSpecFromLines(fileLines)

        // get all the index names in one place so that we can grab them all at once from the DB
        val foodIndexNames = getAllIndexNames(mealSpecs.values)
        val meals: MutableList<Meal> = ArrayList()
        val foods = FoodQueries.getFoodsByIndexName(db, foodIndexNames)
        val currentDay = currentDate()
        for ((key, value) in mealSpecs) {
            val m = makeMeal(key.name!!, currentDay)
            for (fps in value) {
                if (fps.error != null) {
                    // it was an error, log and then ignore
                    errorLines[fileLines[fps.lineIdx]] = fps.error
                } else if (fps.foodIndexName == null || !foods.containsKey(fps.foodIndexName)) {
                    // no food found
                    val errorMsg = "Unrecognised food index name: '${fps.foodIndexName}'"
                    errorLines[fileLines[fps.lineIdx]] = errorMsg
                } else {
                    // everything seems okay - previous check ensures that the foods map contains the required entry
                    processFpSpec(fps, m, foods[fps.foodIndexName]!!)
                    // was there a DB error?
                    if (fps.createdObject == null) {
                        assert(fps.error != null) { "No FoodPortion created but no error message" }
                        errorLines[fileLines[fps.lineIdx]] = fps.error
                    } else {
                        // finally!
                        m.addFoodPortion(fps.createdObject!!)
                    }
                }
            }
            meals.add(m)
        }
        return meals
    }
}