package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.Schema.FoodTable
import com.machfour.macros.objects.inbuilt.Units
import java.lang.StringBuilder
import java.util.Collections

open class Food internal constructor(dataMap: ColumnData<Food>, objectSource: ObjectSource) :
        MacrosEntityImpl<Food>(dataMap, objectSource) {

    companion object {
        val descriptionColumns = listOf(
            FoodTable.BRAND,
            FoodTable.VARIETY,
            FoodTable.NAME,
            FoodTable.EXTRA_DESC,
            FoodTable.NOTES,
            FoodTable.INDEX_NAME
        )

        // Dynamically create either a Food or CompositeFood depending on the datamap passsed in.
        // Hooray for preferring static constructors over new!!!
        val factory: Factory<Food> = Factory { dataMap, objectSource ->
                when (FoodType.fromString(dataMap[FoodTable.FOOD_TYPE]!!)) {
                    FoodType.COMPOSITE -> CompositeFood(dataMap, objectSource)
                    else -> Food(dataMap, objectSource)
                }
            }

        // if factory is saved as an instance variable, then table has to come after

        val table: Table<Food>
            get() = FoodTable.instance

    }

    private val servingsInternal: MutableList<Serving> = ArrayList()
    val servings: List<Serving>
        get() = Collections.unmodifiableList(servingsInternal)

    var defaultServing: Serving? = null
        private set

    private val nutritionData: NutritionData = NutritionData()

    val foodType: FoodType = FoodType.fromString(dataMap[FoodTable.FOOD_TYPE]!!)

    var foodCategory: FoodCategory? = null
        private set

    // quantity corresponds to that contained in the Schema.NutritionDataTable.QUANTITY table
    open fun getNutritionData(): NutritionData {
        return nutritionData
    }

    open fun addNutrientValue(nv: NutrientValue) {
        // TODO check ID matches
        nv.setFood(this)
        nutritionData.nutrientData[nv.nutrient] = nv
    }

    fun setFoodCategory(c: FoodCategory) {
        assert(foodCategory == null && foreignKeyMatches(this, FoodTable.CATEGORY, c))
        foodCategory = c
    }

    override val table: Table<Food>
        get() = Companion.table

    override val factory: Factory<Food>
        get() = Companion.factory

    fun addServing(s: Serving) {
        assert(!servingsInternal.contains(s) && foreignKeyMatches(s, Schema.ServingTable.FOOD_ID, this))
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

    // Returns list of quantity units and servings which can be used to measure this food
    val validMeasurements: List<PortionMeasurement>
        get() {
            val naturalUnit = getNutritionData().qtyUnit
            return ArrayList<PortionMeasurement>().apply {
                add(naturalUnit)
                // allow conversion if density is given
                if (density != null) {
                    if (naturalUnit.type === UnitType.VOLUME) {
                        add(Units.GRAMS)
                    } else {
                        add(Units.MILLILITRES)
                    }
                }
                addAll(servings)
            }
        }

    val usdaIndex: Long?
        get() = getData(FoodTable.USDA_INDEX)

    val nuttabIndex: String?
        get() = getData(FoodTable.NUTTAB_INDEX)

    val dataSource: String?
        get() = getData(FoodTable.DATA_SOURCE)

    val dataNotes: String?
        get() = getData(FoodTable.DATA_NOTES)

    val density: Double?
        get() = getData(FoodTable.DENSITY)



    override fun equals(other: Any?): Boolean {
        return other is Food && super.equals(other)
    }

    override fun hashCode(): Int = super.hashCode()

    val shortName: String
        get() = prettyFormat(withBrand = false, withVariety = false)

    val longName: String
        get() = prettyFormat(withExtra = true)

    val mediumName: String
        get() = prettyFormat()

    val sortableName: String
        get() = prettyFormat(withExtra = true, sortable = true)

    val basicName: String
        get() = getData(FoodTable.NAME)!!

    val variety: String?
        get() = getData(FoodTable.VARIETY)

    val brand: String?
        get() = getData(FoodTable.BRAND)

    val extraDesc: String?
        get() = getData(FoodTable.EXTRA_DESC)

    val notes: String?
        get() = getData(FoodTable.NOTES)

    val indexName: String
        get() = getData(FoodTable.INDEX_NAME)!!

    val categoryName: String
        get() = getData(FoodTable.CATEGORY)!!

    /*
     * Order of fields:
     * if sortable:
     *     <name>, <brand>, <variety> (<notes>)
     * else if variety_after_name is present:
     *     <brand> <name> <variety> (<notes>)
     * else:
     *     <brand> <variety> <name> (<notes>)
     */
    private fun prettyFormat(
        withBrand : Boolean = true,
        withVariety : Boolean = true,
        withExtra : Boolean = false,
        sortable : Boolean = false
    ): String {
        val prettyName = StringBuilder(basicName)
        if (sortable) {
            if (withBrand && brand != null) {
                prettyName.append(", ").append(brand)
            }
            if (variety != null) {
                prettyName.append(", ").append(variety)
            }
        } else {
            if (withVariety && variety != null) {
                prettyName.insert(0, "$variety ")
            }
            if (withBrand && brand != null) {
                prettyName.insert(0, "$brand ")
            }
        }
        if (withExtra && extraDesc != null) {
            prettyName.append(" (").append(extraDesc).append(")")
        }
        return prettyName.toString()
    }

}