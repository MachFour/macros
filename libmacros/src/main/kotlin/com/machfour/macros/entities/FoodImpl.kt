package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.foodname.FoodDescription
import com.machfour.macros.foodname.indexNamePrototype
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.NutrientData
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.sql.rowdata.foodToRowData
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLILITRES
import com.machfour.macros.units.UnitType

typealias Food = FoodImpl

// don't need hashcode override since equals implies super.equals true, so hashcode will match
@Suppress("EqualsOrHashCode")
open class FoodImpl internal constructor(dataMap: RowData<Food>, objectSource: ObjectSource):
    MacrosEntityImpl<Food>(dataMap, objectSource), IFood<FoodNutrientValue>, FoodDescription {

    companion object {
        // Dynamically create either a Food or CompositeFood depending on foodType in dataMap.
        // Hooray for preferring static constructors over new!!!
        val factory: Factory<IFood<*>, Food> by lazy {
            object: Factory<IFood<*>, Food> {
                override fun construct(data: RowData<Food>, source: ObjectSource): Food {
                    // index name completion
                    if (data[FoodTable.INDEX_NAME] == null) {
                        val name = data[FoodTable.NAME] ?: "food"
                        val brand = data[FoodTable.BRAND]
                        val variety = data[FoodTable.VARIETY]
                        val extraDesc = data[FoodTable.EXTRA_DESC]
                        data.put(FoodTable.INDEX_NAME, indexNamePrototype(name, brand, variety, extraDesc))
                    }

                    data.makeImmutable()

                    return when (FoodType.fromString(data[FoodTable.FOOD_TYPE]!!)) {
                        FoodType.COMPOSITE -> CompositeFoodImpl.new(data, source)
                        else -> FoodImpl(data, source)
                    }
                }

                override fun deconstruct(obj: IFood<*>): RowData<Food> {
                    return foodToRowData(obj)
                }
            }
        }

        fun processFoodType(rowData: RowData<FoodImpl>): FoodType {
            val foodTypeString = rowData[FoodTable.FOOD_TYPE]
            requireNotNull(foodTypeString) { "Null food type string for rowData: $rowData" }
            val foodType = FoodType.fromString(foodTypeString)
            requireNotNull(foodType) { "Invalid food type string: $foodTypeString" }
            return foodType
        }
    }

    private val servingsInternal: MutableList<Serving> = ArrayList()

    override val servings: List<Serving>
        get() = servingsInternal

    var defaultServing: Serving? = null
        private set

    // TODO check nutrient data is initialised
    override val nutrientData: NutrientData<FoodNutrientValue>
        get() = foodNutrientData
    private val foodNutrientData = FoodNutrientData(density = density)

    override val foodType: FoodType = processFoodType(dataMap)

    override lateinit var category: FoodCategory

    open fun addNutrientValue(nv: FoodNutrientValue) {
        foodNutrientData[nv.nutrient] = nv
    }

    fun setFoodCategory(c: FoodCategory) {
        check(!this::category.isInitialized && foreignKeyMatches(this, FoodTable.CATEGORY, c))
        category = c
    }

    override fun toRowData(): RowData<Food> {
        return super<MacrosEntityImpl>.toRowData()
    }

    override fun getTable(): Table<*, Food> {
        return FoodTable
    }

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

    fun getServingById(id: EntityId): Serving? {
        return servingsInternal.firstOrNull { it.id == id }
    }

    fun getServingByName(name: String): Serving? {
        return servingsInternal.firstOrNull { it.name == name }
    }

    val naturalUnit: Unit
        get() = nutrientData.perQuantity.unit

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

    override val usdaIndex: Int?
        get() = data[FoodTable.USDA_INDEX]

    override val nuttabIndex: String?
        get() = data[FoodTable.NUTTAB_INDEX]

    override val dataSource: String?
        get() = data[FoodTable.DATA_SOURCE]

    override val dataNotes: String?
        get() = data[FoodTable.DATA_NOTES]

    override fun hasDescriptionData(col: Column<Food, String>) = hasData(col)
    override fun getDescriptionData(col: Column<Food, String>) = getData(col)

    final override val density: Double?
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

    override val categoryName: String
        get() = data[FoodTable.CATEGORY]!!

    override val relevanceOffsetValue: Int?
        get() = data[FoodTable.SEARCH_RELEVANCE]

    // Returns the most recent time out of
    // - food (table) modify time
    // - any servings modify time
    // - nutrient value modify time
    // Ensure servings and nutrient values are added first!
    override val descendentModifyTime: Instant by lazy {
        val servingModifyTime = servings.maxOfOrNull { it.modifyTime } ?: 0
        val nutrientValueModifyTime = foodNutrientData.values.maxOfOrNull { it.modifyTime } ?: 0
        maxOf(modifyTime, maxOf(servingModifyTime, nutrientValueModifyTime))
    }
}
