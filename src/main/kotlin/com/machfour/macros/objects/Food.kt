package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.Schema.FoodTable
import java.lang.StringBuilder
import java.util.Collections

open class Food protected constructor(dataMap: ColumnData<Food>, objectSource: ObjectSource) :
        MacrosEntityImpl<Food>(dataMap, objectSource) {

    companion object {
        val DESCRIPTION_COLUMNS = listOf(
            FoodTable.BRAND,
            FoodTable.VARIETY,
            FoodTable.NAME,
            FoodTable.NOTES,
            FoodTable.INDEX_NAME
        )

        fun table(): Table<Food> {
            return FoodTable.instance
        }

        // Dynamically create either a Food or CompositeFood depending on the datamap passsed in.
        // Hooray for preferring static constructors over new!!!
        fun factory(): Factory<Food> {
            return object : Factory<Food> {
                override fun construct(dataMap: ColumnData<Food>, objectSource: ObjectSource): Food {
                    return when (FoodType.fromString(dataMap.get(FoodTable.FOOD_TYPE)!!)) {
                        FoodType.COMPOSITE -> CompositeFood(dataMap, objectSource)
                        else -> Food(dataMap, objectSource)
                    }
                }
            }
        }
    }

    private val servings: MutableList<Serving> = ArrayList()

    val sortableName = makeSortableName()

    var defaultServing: Serving? = null
        private set

    private lateinit var nutritionData: NutritionData

    val foodType: FoodType = FoodType.fromString(dataMap.get(FoodTable.FOOD_TYPE)!!)

    var foodCategory: FoodCategory? = null
        private set

    // quantity corresponds to that contained in the Schema.NutritionDataTable.QUANTITY table
    open fun getNutritionData(): NutritionData {
        return nutritionData
    }

    open fun setNutritionData(nd: NutritionData) {
        assert(foreignKeyMatches(nd, Schema.NutritionDataTable.FOOD_ID, this))
        nutritionData = nd
    }

    fun setFoodCategory(c: FoodCategory) {
        assert(foodCategory == null && foreignKeyMatches(this, FoodTable.CATEGORY, c))
        foodCategory = c
    }

    override val table: Table<Food>
        get() = table()

    override val factory: Factory<Food>
        get() = factory()

    fun addServing(s: Serving) {
        assert(!servings.contains(s) && foreignKeyMatches(s, Schema.ServingTable.FOOD_ID, this))
        servings.add(s)
        if (s.isDefault) {
            setDefaultServing(s)
        }
    }

    val usdaIndex: Long?
        get() = getData(FoodTable.USDA_INDEX)

    val nuttabIndex: String?
        get() = getData(FoodTable.NUTTAB_INDEX)
    val notes: String?
        get() = getData(FoodTable.NOTES)

    override fun equals(other: Any?): Boolean {
        return other is Food && super.equals(other)
    }

    override fun hashCode(): Int = super.hashCode()

    private fun getDescriptionData(fieldName: Column<Food, String>): String? {
        assert(DESCRIPTION_COLUMNS.contains(fieldName))
        return getData(fieldName)
    }

    val shortName: String
        get() = prettyFormat(false, false, false, false)

    val longName: String
        get() = prettyFormat(true, true, true, false)

    val mediumName: String
        get() = prettyFormat(true, true, false, false)

    val indexName: String
        get() = getData(FoodTable.INDEX_NAME)!!

    private fun makeSortableName(): String {
        return prettyFormat(true, true, true, true)
    }

    val categoryName: String
        get() = getData(FoodTable.CATEGORY)!!

    private fun prettyFormat(withBrand: Boolean, withVariety: Boolean, withNotes: Boolean, sortable: Boolean): String {
        val prettyName = StringBuilder(getDescriptionData(FoodTable.NAME))
        val variety = getDescriptionData(FoodTable.VARIETY)
        val brand = getDescriptionData(FoodTable.BRAND)
        if (sortable) {
            if (withBrand && hasDescriptionData(FoodTable.BRAND)) {
                prettyName.append(", ").append(brand)
            }
            if (hasDescriptionData(FoodTable.VARIETY)) {
                prettyName.append(", ").append(variety)
            }
        } else {
            if (withVariety && hasDescriptionData(FoodTable.VARIETY)) {
                if (getData(FoodTable.VARIETY_AFTER_NAME)!!) {
                    prettyName.append(" ").append(variety)
                } else {
                    prettyName.insert(0, "$variety ")
                }
            }
            if (withBrand && hasDescriptionData(FoodTable.BRAND)) {
                prettyName.insert(0, "$brand ")
            }
        }
        if (withNotes && hasDescriptionData(FoodTable.NOTES)) {
            prettyName.append(" (").append(getDescriptionData(FoodTable.NOTES)).append(")")
        }
        return prettyName.toString()
    }

    /*
     * Order of fields:
     * if sortable:
     *     <name>, <brand>, <variety> (<notes>)
     * else if variety_after_name is present:
     *     <brand> <name> <variety> (<notes>)
     * else:
     *     <brand> <variety> <name> (<notes>)
     */
    private fun hasDescriptionData(fieldName: Column<Food, String>): Boolean {
        return hasData(fieldName)
    }

    private fun setDefaultServing(s: Serving) {
        assert(defaultServing == null) { "Default serving already set" }
        assert(servings.contains(s)) { "Serving does not belong to this food" }
        defaultServing = s
    }

    fun getServingById(servingId: Long): Serving? {
        return servings.firstOrNull { it.id == servingId }
    }

    fun getServingByName(name: String): Serving? {
        return servings.firstOrNull { it.name == name }
    }

    fun getServings(): List<Serving> {
        return Collections.unmodifiableList(servings)
    }
}