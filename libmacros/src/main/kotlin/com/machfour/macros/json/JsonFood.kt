package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Food
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonFood(
    override val id: EntityId = MacrosEntity.NO_ID,
    override val created: Instant = 0,
    override val modified: Instant = 0,

    // The basic name of the food, e.g. 'apple', 'chicken breast', 'chocolate bar'. It should be
    // a short but descriptive name as when screen space is limited, this is the only field shown.
    @SerialName("basic_name")
    val basicName: String,

    // Brand name or company producing the food. Usually not important for fresh produce.
    val brand: String? = null,

    // Food-specific variety, e.g. low-fat (milk), green (apple), lean (beef mince).
    val variety: String? = null,

    // Extra description that helps identify the food, e.g. flavour, raw/cooked, fresh/frozen.
    // The usage of this field overlaps with variety slightly, and variety alone is often sufficient.
    // This field is good for adding extra detail when needed.
    @SerialName("extra_description")
    val extraDescription: String? = null,

    // A keyword name for the food which can uniquely identify it.
    // It can be generated from basic_name, brand, variety, and extraDescription if necessary.
    @SerialName("index_name")
    val indexName: String = Food.indexNamePrototype(basicName, brand, variety, extraDescription),

    // Used to store longer-form information about the food. Not displayed in lists or summaries;
    // shown in the detailed information screen for this specific food.
    val notes: String? = null,

    val category: String? = null,

    @SerialName("usda_index")
    val usdaIndex: Int? = null,

    @SerialName("nuttab_index")
    val nuttabIndex: String? = null,

    // Source of the nutrition data, e.g. "Label", "Website", "Calculated"
    @SerialName("data_source")
    val dataSource: String? = null,

    // Notes regarding the nutrient data, e.g. any inaccuracies or
    @SerialName("data_notes")
    val dataNotes: String? = null,

    val density: Double? = null,

    // Allows setting a 'relevance score' for the food, e.g. to rank it higher in search
    // results and predictions. This can be added to other offsets from e.g. food type or category.
    @SerialName("relevance_offset")
    val relevanceOffset: Int = 0,

    // A set is used so that unordered comparisons can be done
    val servings: List<JsonServing> = emptyList(),
    val nutrients: Map<String, JsonQuantity> = emptyMap(),
    val ingredients: List<JsonIngredient> = emptyList(),
): JsonEntity() {
    companion object {
        private val Food.jsonServings: List<JsonServing>
            get() = servings.map { JsonServing(it) }

        private val Food.jsonNutrientValues: Map<String, JsonQuantity>
            get() = nutrientData.values.associateBy({ it.nutrient.csvName }) { JsonQuantity(it) }
    }

    constructor(f: Food): this(
        id = f.id,
        created = f.createTime,
        modified = f.modifyTime,
        basicName = f.basicName,
        brand = f.brand,
        variety = f.variety,
        extraDescription = f.extraDesc,
        indexName = f.indexName,
        notes = f.notes,
        category = f.categoryName,
        usdaIndex = f.usdaIndex,
        nuttabIndex = f.nuttabIndex,
        dataSource = f.dataSource,
        dataNotes = f.dataNotes,
        density = f.density,
        relevanceOffset = f.relevanceOffset.value,
        servings = f.jsonServings,
        nutrients = f.jsonNutrientValues,
    )
}
