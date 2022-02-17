package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLILITRES
import com.machfour.macros.units.UnitType

// don't need hashcode override since equals implies super.equals true, so hashcode will match
@Suppress("EqualsOrHashCode")
open class Food internal constructor(dataMap: RowData<Food>, objectSource: ObjectSource) :
        MacrosEntityImpl<Food>(dataMap, objectSource) {

    companion object {
        val descriptionColumns = listOf(
            FoodTable.NAME,
            FoodTable.BRAND,
            FoodTable.VARIETY,
            FoodTable.EXTRA_DESC,
            FoodTable.NOTES,
            FoodTable.INDEX_NAME,
            FoodTable.DATA_SOURCE,
            FoodTable.DATA_NOTES,
        )

        // Dynamically create either a Food or CompositeFood depending on the datamap passsed in.
        // Hooray for preferring static constructors over new!!!
        val factory: Factory<Food>
            get() = Factories.food

        fun indexNamePrototype(
            basicName: String,
            brand: String?,
            variety: String?,
            extraDesc: String?,
        ): String {
            // use sortable name but replace sequences of spaces (and dashes) with a single dash
            return prettyFormat(
                basicName = basicName,
                brand = brand,
                variety = variety,
                extraDesc = extraDesc,
                withBrand = true,
                withVariety = true,
                withExtra = true,
                sortable = true
            )
                .replace(Regex("[()\\[\\]{}&%!$#@*^+=:;<>?/\\\\]"), replacement = "")
                .replace(Regex("[\\s-,]+"), replacement = "-")
                .removeSuffix("-")
        }

        /*
         * Order of fields:
         * if sortable:
         *     <name>, <brand>, <variety> (<extra desc>)
         * else:
         *     <brand> <variety> <name> (<extra desc>)
         */
        fun prettyFormat(
            basicName: String,
            brand: String?,
            variety: String?,
            extraDesc: String?,
            withBrand: Boolean = true,
            withVariety: Boolean = true,
            withExtra: Boolean = false,
            sortable: Boolean = false,
            includeEmptyFields: Boolean = true,
        ): String {
            val isPresent: (String?) -> Boolean = {
                it != null && (includeEmptyFields || it.isNotEmpty())
            }

            val prettyName = StringBuilder(basicName)
            if (sortable) {
                if (withBrand && isPresent(brand)) {
                    prettyName.append(", ").append(brand)
                }
                if (isPresent(variety)) {
                    prettyName.append(", ").append(variety)
                }
            } else {
                if (withVariety && isPresent(variety)) {
                    prettyName.insert(0, "$variety ")
                }
                if (withBrand && isPresent(brand)) {
                    prettyName.insert(0, "$brand ")
                }
            }
            if (withExtra && isPresent(extraDesc)) {
                prettyName.append(" (").append(extraDesc).append(")")
            }
            return prettyName.toString()
        }

        fun processFoodType(rowData: RowData<Food>): FoodType {
            val foodTypeString = rowData[FoodTable.FOOD_TYPE]
            requireNotNull(foodTypeString) { "Null food type string for rowData: $rowData" }
            val foodType = FoodType.fromString(foodTypeString)
            requireNotNull(foodType) { "Invalid food type string: $foodTypeString" }
            return foodType
        }
    }

    private val servingsInternal: MutableList<Serving> = ArrayList()

    val servings: List<Serving>
        get() = servingsInternal

    var defaultServing: Serving? = null
        private set

    // TODO check nutrient data is initialised
    open val nutrientData: FoodNutrientData = FoodNutrientData()

    val foodType: FoodType = processFoodType(dataMap)

    lateinit var foodCategory: FoodCategory
        private set

    open fun addNutrientValue(nv: FoodNutrientValue) {
        // TODO check ID matches
        nv.setFood(this)
        nutrientData[nv.nutrient] = nv
    }

    fun setFoodCategory(c: FoodCategory) {
        assert(!this::foodCategory.isInitialized && foreignKeyMatches(this, FoodTable.CATEGORY, c))
        foodCategory = c
    }

    override val table: Table<Food>
        get() = FoodTable

    override val factory: Factory<Food>
        get() = Companion.factory

    fun addServing(s: Serving) {
        assert(!servingsInternal.contains(s) && foreignKeyMatches(s, ServingTable.FOOD_ID, this))
        servingsInternal.add(s)
        if (s.isDefault) {
            setDefaultServing(s)
        }
    }

    private fun setDefaultServing(s: Serving) {
        assert(defaultServing == null) { "Default serving already set" }
        assert(servingsInternal.contains(s)) { "Serving does not belong to this food" }
        defaultServing = s
    }

    fun getServingById(id: Long): Serving? {
        return servingsInternal.firstOrNull { it.id == id }
    }

    fun getServingByName(name: String): Serving? {
        return servingsInternal.firstOrNull { it.name == name }
    }

    val naturalUnit: Unit
        get() = nutrientData.qtyUnit

    val validUnits: List<Unit>
        get() {
            return ArrayList<Unit>().apply {
                add(naturalUnit)
                // allow conversion if density is given
                if (density != null) {
                    if (naturalUnit.type === UnitType.VOLUME) {
                        add(GRAMS)
                    } else {
                        add(MILLILITRES)
                    }
                }
            }
        }

    // Returns list of quantity units and servings which can be used to measure this food
    val validMeasurements: List<PortionMeasurement>
        get() {
            return validUnits + servings
        }

    val usdaIndex: Int?
        get() = data[FoodTable.USDA_INDEX]

    val nuttabIndex: String?
        get() = data[FoodTable.NUTTAB_INDEX]

    val dataSource: String?
        get() = data[FoodTable.DATA_SOURCE]

    val dataNotes: String?
        get() = data[FoodTable.DATA_NOTES]

    val density: Double?
        get() = data[FoodTable.DENSITY]

    // don't need to override hashcode since equality implies hashcodes equal but not the converse
    override fun equals(other: Any?): Boolean {
        return other is Food
                && super.equals(other)
                && servings == other.servings
                && nutrientData == other.nutrientData
    }

    val shortName: String
        get() = prettyFormat(withBrand = false, withVariety = false)

    val longName: String
        get() = prettyFormat(withExtra = true)

    val mediumName: String
        get() = prettyFormat()

    val sortableName: String
        get() = prettyFormat(withExtra = true, sortable = true)

    val basicName: String
        get() = data[FoodTable.NAME]!!

    val variety: String?
        get() = data[FoodTable.VARIETY]

    val brand: String?
        get() = data[FoodTable.BRAND]

    val extraDesc: String?
        get() = data[FoodTable.EXTRA_DESC]

    val notes: String?
        get() = data[FoodTable.NOTES]

    val indexName: String
        get() = data[FoodTable.INDEX_NAME]!!

    val categoryName: String
        get() = data[FoodTable.CATEGORY]!!

    val searchRelevance: SearchRelevance
        get() = SearchRelevance.fromValue(data[FoodTable.SEARCH_RELEVANCE]!!)

    // Returns the most recent time out of
    // - food (table) modify time
    // - any servings modify time
    // - nutrient value modify time
    // Ensure servings and nutrient values are added first!
    val userModifyTime: Long by lazy {
        val servingModifyTime = servings.maxOfOrNull { it.modifyTime } ?: 0
        val nutrientValueModifyTime = nutrientData.nutrientValues.maxOfOrNull { it.modifyTime } ?: 0
        maxOf(modifyTime, maxOf(servingModifyTime, nutrientValueModifyTime))
    }

    private fun prettyFormat(
        withBrand: Boolean = true,
        withVariety: Boolean = true,
        withExtra: Boolean = false,
        sortable: Boolean = false
    ): String {
        return prettyFormat(
            basicName = basicName,
            brand = brand,
            variety = variety,
            extraDesc = extraDesc,
            withBrand = withBrand,
            withVariety = withVariety,
            withExtra = withExtra,
            sortable = sortable,

            )
    }

}