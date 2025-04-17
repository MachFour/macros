package com.machfour.macros.entities

import com.machfour.macros.core.FoodType
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.SearchRelevance
import com.machfour.macros.foodname.FoodDescription
import com.machfour.macros.nutrients.INutrientValue
import com.machfour.macros.nutrients.IQuantity
import com.machfour.macros.nutrients.NutrientData

interface IFood<N: INutrientValue>: MacrosEntity, FoodDescription {

    val servings: List<IServing>
    val nutrientData: NutrientData<N>
    val quantity: IQuantity
        get() = nutrientData.perQuantity

    fun getServingByNameOrNull(name: String): IServing? {
        return servings.firstOrNull { it.name == name }
    }

    override val basicName: String
    override val variety: String?
    override val brand: String?
    override val extraDesc: String?
    override val notes: String?
    override val indexName: String
    override val dataSource: String?
    override val dataNotes: String?

    val category: FoodCategory
    val categoryName: String
        get() = category.name

    val density: Double?

    val foodType: FoodType

    val usdaIndex: Int?
    val nuttabIndex: String?

    val relevanceOffsetValue: Int?

    val relevanceOffset: SearchRelevance
        get() = SearchRelevance.fromValue(relevanceOffsetValue)

    val searchRelevance: SearchRelevance
        get() = foodType.baseSearchRelevance + relevanceOffset

    // Returns the most recent time out of
    // - food (table) modify time
    // - any servings modify time
    // - nutrient value modify time
    val descendentModifyTime: Instant
        get() {
            val servingModifyTime = servings.maxOfOrNull { it.modifyTime } ?: 0
            val nutrientValueModifyTime = nutrientData.nutrientValues().maxOfOrNull { it.value.modifyTime } ?: 0
            return maxOf(modifyTime, maxOf(servingModifyTime, nutrientValueModifyTime))
        }
}