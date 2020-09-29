package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.Schema.FoodTable
import com.machfour.macros.core.Schema.NutritionDataTable
import java.lang.StringBuilder
import java.util.Collections

open class Food protected constructor(dataMap: ColumnData<Food>, objectSource: ObjectSource) :
        MacrosEntityImpl<Food>(dataMap, objectSource) {

    companion object {
        val DESCRIPTION_COLUMNS = listOf(
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

    private val servings: MutableList<Serving> = ArrayList()

    val sortableName = makeSortableName()

    var defaultServing: Serving? = null
        private set

    private lateinit var nutritionData: NutritionData

    val foodType: FoodType = FoodType.fromString(dataMap[FoodTable.FOOD_TYPE]!!)

    var foodCategory: FoodCategory? = null
        private set

    // quantity corresponds to that contained in the Schema.NutritionDataTable.QUANTITY table
    open fun getNutritionData(): NutritionData {
        return nutritionData
    }

    open fun setNutritionData(nd: NutritionData) {
        assert(foreignKeyMatches(nd, NutritionDataTable.FOOD_ID, this))
        nutritionData = nd
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

    override fun equals(other: Any?): Boolean {
        return other is Food && super.equals(other)
    }

    override fun hashCode(): Int = super.hashCode()

    private fun getDescriptionData(fieldName: Column<Food, String>): String? {
        assert(DESCRIPTION_COLUMNS.contains(fieldName))
        return getData(fieldName)
    }

    val shortName: String
        get() = prettyFormat(withBrand = false, withVariety = false)

    val longName: String
        get() = prettyFormat(withExtra = true)

    val mediumName: String
        get() = prettyFormat()

    val extraDesc: String?
        get() = getData(FoodTable.EXTRA_DESC)

    val notes: String?
        get() = getData(FoodTable.NOTES)

    val indexName: String
        get() = getData(FoodTable.INDEX_NAME)!!

    private fun makeSortableName(): String {
        return prettyFormat(withExtra = true, sortable = true)
    }

    val categoryName: String
        get() = getData(FoodTable.CATEGORY)!!

    private fun prettyFormat(withBrand : Boolean = true,
                             withVariety : Boolean = true,
                             withExtra : Boolean = false,
                             sortable : Boolean = false)
            : String {
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
                prettyName.insert(0, "$variety ")
            }
            if (withBrand && hasDescriptionData(FoodTable.BRAND)) {
                prettyName.insert(0, "$brand ")
            }
        }
        if (withExtra && hasDescriptionData(FoodTable.EXTRA_DESC)) {
            prettyName.append(" (").append(getDescriptionData(FoodTable.EXTRA_DESC)).append(")")
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

    fun getServingById(id: Long): Serving? {
        return servings.firstOrNull { it.id == id }
    }

    fun getServingByName(name: String): Serving? {
        return servings.firstOrNull { it.name == name }
    }

    fun getServings(): List<Serving> {
        return Collections.unmodifiableList(servings)
    }
}