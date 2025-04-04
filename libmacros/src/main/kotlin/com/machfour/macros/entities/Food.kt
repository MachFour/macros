package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.foodname.FoodDescription
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLILITRES
import com.machfour.macros.units.UnitType

// don't need hashcode override since equals implies super.equals true, so hashcode will match
@Suppress("EqualsOrHashCode")
open class Food internal constructor(dataMap: RowData<Food>, objectSource: ObjectSource):
        MacrosEntityImpl<Food>(dataMap, objectSource), FoodDescription {

    companion object {
        // Dynamically create either a Food or CompositeFood depending on the datamap passsed in.
        // Hooray for preferring static constructors over new!!!
        val factory: Factory<Food>
            get() = Factories.food

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
    open val nutrientData: FoodNutrientData = FoodNutrientData(density = this.density)

    val foodType: FoodType = processFoodType(dataMap)

    lateinit var foodCategory: FoodCategory
        private set

    open fun addNutrientValue(nv: FoodNutrientValue) {
        // TODO check ID matches
        nv.setFood(this)
        nutrientData[nv.nutrient] = nv
    }

    fun setFoodCategory(c: FoodCategory) {
        check(!this::foodCategory.isInitialized && foreignKeyMatches(this, FoodTable.CATEGORY, c))
        foodCategory = c
    }

    override val table: Table<Food>
        get() = FoodTable

    override val factory: Factory<Food>
        get() = Companion.factory

    fun addServing(s: Serving) {
        check(!servingsInternal.contains(s) && foreignKeyMatches(s, ServingTable.FOOD_ID, this))
        servingsInternal.add(s)
        if (s.isDefault) {
            setDefaultServing(s)
        }
    }

    private fun setDefaultServing(s: Serving) {
        check(defaultServing == null) { "Default serving already set" }
        check(servingsInternal.contains(s)) { "Serving does not belong to this food" }
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
            return buildList {
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

    override val dataSource: String?
        get() = data[FoodTable.DATA_SOURCE]

    override val dataNotes: String?
        get() = data[FoodTable.DATA_NOTES]

    override fun hasDescriptionData(col: Column<Food, String>) = hasData(col)
    override fun getDescriptionData(col: Column<Food, String>) = getData(col)

    val density: Double?
        get() = data[FoodTable.DENSITY]

    // don't need to override hashcode since equality implies hashcodes equal but not the converse
    override fun equals(other: Any?): Boolean {
        return other is Food
                && super.equals(other)
                && servings == other.servings
                && nutrientData == other.nutrientData
    }

    override val basicName: String
        get() = data[FoodTable.NAME]!!

    override val variety: String?
        get() = data[FoodTable.VARIETY]

    override val brand: String?
        get() = data[FoodTable.BRAND]

    override val extraDesc: String?
        get() = data[FoodTable.EXTRA_DESC]

    override val notes: String?
        get() = data[FoodTable.NOTES]

    override val indexName: String
        get() = data[FoodTable.INDEX_NAME]!!

    val categoryName: String
        get() = data[FoodTable.CATEGORY]!!

    val relevanceOffset: SearchRelevance
        get() = SearchRelevance.fromValue(data[FoodTable.SEARCH_RELEVANCE])
    val searchRelevance: SearchRelevance
        get() = foodType.baseSearchRelevance + relevanceOffset


    // Returns the most recent time out of
    // - food (table) modify time
    // - any servings modify time
    // - nutrient value modify time
    // Ensure servings and nutrient values are added first!
    val userModifyTime: Long by lazy {
        val servingModifyTime = servings.maxOfOrNull { it.modifyTime } ?: 0
        val nutrientValueModifyTime = nutrientData.values.maxOfOrNull { it.modifyTime } ?: 0
        maxOf(modifyTime, maxOf(servingModifyTime, nutrientValueModifyTime))
    }
}