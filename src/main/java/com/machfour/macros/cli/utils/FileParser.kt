package com.machfour.macros.cli.utils

import com.machfour.macros.cli.utils.MealSpec.Companion.makeMealSpec
import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.schema.FoodPortionTable
import com.machfour.macros.core.schema.MealTable
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.inbuilt.Units
import com.machfour.macros.queries.MacrosDataSource
import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.DateStamp.Companion.currentDate
import com.machfour.macros.util.FoodPortionSpec
import com.machfour.macros.util.javaTrim
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.sql.SQLException

class FileParser {
    private val errorLines = LinkedHashMap<String, String>()

    fun getErrorLines(): Map<String, String?> {
        return errorLines.toMap()
    }

    companion object {
        private val mealPattern = Regex("\\[(.*)]")
        private val quantityRegex = Regex("(-?[0-9]+(?:.[0-9]+)?)") // ?: defines a non-capturing group
        private val unitRegex = Regex("([a-zA-Z]+)?")
        private val servingCountPattern = quantityRegex
        private val quantityAndUnitPattern = Regex("${quantityRegex.pattern}\\s*${unitRegex.pattern}")

        // returns an array holding MealSpec or FoodPortionSpec objects describing the objects that should be created
        // only checked for syntax, not whether those foods/servings actually exist
        // the first item is always a MealSpec
        private fun createSpecFromLines(fileLines: List<String>): Map<MealSpec, List<FoodPortionSpec>> {
            val specMap: MutableMap<MealSpec, List<FoodPortionSpec>> = LinkedHashMap()
            // if we encounter a food line before a meal title,
            // have to instantiate a dummy meal to hold it
            var currentFpSpecs: MutableList<FoodPortionSpec>? = null
            for (index in fileLines.indices) {
                val line = fileLines[index].javaTrim()
                val mealTitle = mealPattern.find(line)
                if (mealTitle != null) {
                    // make a new meal
                    val m = makeMealSpec(mealTitle.groupValues[1])
                    currentFpSpecs = ArrayList()
                    specMap[m] = currentFpSpecs
                } else if (line.isNotEmpty() && !line.startsWith("#")) {
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

        private fun getAllIndexNames(allFpSpecs: Collection<List<FoodPortionSpec>>): Set<String> {
            val foodIndexNames = allFpSpecs.flatMap { it.map { spec -> spec.foodIndexName } }
            return foodIndexNames.toSet()
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

        fun processFpSpec(fps: FoodPortionSpec, m: Meal, f: Food) {
            assert(f.indexName == fps.foodIndexName) { "Food does not match index name of spec" }
            val serving = if (!fps.isServingMode) {
                // use unit if specified, otherwise default unit of food's nutrition data
                null
            } else {
                check(fps.servingName != null && fps.servingCount != 0.0)
                if (fps.servingName.isEmpty()) {
                    // default serving
                    f.defaultServing ?: run {
                        fps.error = "food has no default serving"
                        return
                    }
                } else {
                    f.getServingByName(fps.servingName) ?: run {
                        fps.error = "food has no serving named '" + fps.servingName + "'"
                        return
                    }
                }
            }

            val (quantity, unit) = if (serving != null) {
                Pair(fps.servingCount * serving.quantity, serving.qtyUnit)
            } else {
                Pair(fps.quantity, fps.unit ?: f.nutrientData.qtyUnit)
            }

            val fp = ColumnData(FoodPortion.table).run {
                put(FoodPortionTable.FOOD_ID, f.id)
                put(FoodPortionTable.SERVING_ID, serving?.id)
                put(FoodPortionTable.MEAL_ID, m.id)
                put(FoodPortionTable.QUANTITY_UNIT, unit.abbr)
                put(FoodPortionTable.QUANTITY, quantity)
                FoodPortion.factory.construct(this, ObjectSource.USER_NEW)
            }
            
            fp.initFoodAndNd(f)
            if (serving != null) {
                fp.initServing(serving)
            }
            fps.createdObject = fp
        }

        // returns a FoodPortionSpec object, from the given line of text
        // input forms (whitespace ignored):
        // 1.
        //   egg         ,          60
        //    ^          ^          ^
        //index name  separator  quantity (default metric unit for food)
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
        fun makefoodPortionSpecFromLine(line: String): FoodPortionSpec {
            // if you don't specify an array length limit, it won't match empty strings between commas
            val tokens = line.split(",", limit = 4).map { it.javaTrim() }

            val indexName = tokens[0]
            var quantity = 0.0
            var unit: Unit? = null
            var servingCount = 0.0
            var servingName: String? = null
            var isServingMode = false
            var error = ""

            // run block allows breaking from 'when' at arbitrary points (using return@run)
            run {
                when (tokens.size) {
                    1 -> {
                        // 1 of default serving
                        isServingMode = true
                        servingCount = 1.0
                        servingName = ""
                    }
                    2 -> {
                        // vanilla food and quantity, with optional unit defaulting to whatever food's nutrition data uses
                        isServingMode = false
                        val quantityMatch = quantityAndUnitPattern.find(tokens[1])
                        when (quantityMatch) {
                            null -> {
                                // could not understand anything
                                error = "invalid quantity or unit"
                                return@run // break from when
                            }
                            else ->
                                try {
                                    quantity = quantityMatch.groupValues[1].toDouble()
                                } catch (e: NumberFormatException) {
                                    // invalid quantity
                                    error = "invalid quantity"
                                    return@run // break from when
                                }
                        }

                        val unitString = quantityMatch.groupValues[2]
                        val matchUnit = Units.fromAbbreviationNoThrow(unitString)
                        when {
                            // default unit?
                            unitString.isEmpty() -> unit = null
                            // valid unit specified
                            matchUnit != null -> unit = matchUnit
                            // invalid unit
                            else -> {
                                error = "unrecognised unit"
                                return@run // break from when
                            }
                        }
                    }
                    3 -> {
                        isServingMode = true
                        servingName = tokens[1]
                        // get quantity, which defaults to 1 of serving if not included
                        val servingCountStr = tokens[2]
                        val servingCountMatch = servingCountPattern.find(tokens[2])
                        when {
                            servingCountStr.isEmpty() -> servingCount = 1.0
                            servingCountMatch != null ->
                                try {
                                    servingCount = servingCountMatch.groupValues[1].toDouble()
                                } catch (e: NumberFormatException) {
                                    error = "invalid serving count"
                                    return@run // 'break from when'
                                }
                            else -> {
                                error = "invalid serving count"
                                return@run // 'break from when'
                            }
                        }
                    }
                    else -> error = "too many commas"
                }
            }
            return FoodPortionSpec(indexName, quantity, unit, servingCount, servingName, isServingMode, error)
        }

        private fun makeMeal(description: String, day: DateStamp): Meal {
            val mealData = ColumnData(Meal.table)
            mealData.put(MealTable.NAME, description)
            mealData.put(MealTable.DAY, day)
            return Meal.factory.construct(mealData, ObjectSource.USER_NEW)
        }
    }

    // make sure to close the reader afterwards
    @Throws(IOException::class, SQLException::class)
    fun parseFile(ds: MacrosDataSource, fileReader: Reader): List<Meal> {
        val fileLines = readAllLines(fileReader)
        // also gets list of index names to retrieve
        val mealSpecs = createSpecFromLines(fileLines)

        // get all the index names in one place so that we can grab them all at once from the DB
        val foodIndexNames = getAllIndexNames(mealSpecs.values)
        val meals = ArrayList<Meal>()
        val foods = ds.getFoodsByIndexName(foodIndexNames)
        val currentDay = currentDate()
        for ((key, value) in mealSpecs) {
            val m = makeMeal(key.name!!, currentDay)
            for (fps in value) {
                val fileLine = fileLines[fps.lineIdx]
                if (fps.error.isNotEmpty()) {
                    // it was an error, log and then ignore
                    errorLines[fileLine] = fps.error
                } else if (!foods.containsKey(fps.foodIndexName)) {
                    // no food found
                    val errorMsg = "Unrecognised food index name: '${fps.foodIndexName}'"
                    errorLines[fileLine] = errorMsg
                } else {
                    // everything seems okay - previous check ensures that the foods map contains the required entry
                    processFpSpec(fps, m, foods[fps.foodIndexName]!!)
                    // was there a DB error?
                    val createdObject = fps.createdObject
                    if (createdObject != null) {
                        m.addFoodPortion(createdObject)
                    } else {
                        assert(fps.error.isNotEmpty()) { "No FoodPortion created but no error message" }
                        errorLines[fileLine] = fps.error
                    }
                }
            }
            meals.add(m)
        }
        return meals
    }
}
