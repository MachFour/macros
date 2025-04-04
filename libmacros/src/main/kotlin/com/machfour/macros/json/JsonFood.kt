package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.foodname.FoodDescription
import com.machfour.macros.foodname.indexNamePrototype
import com.machfour.macros.nutrients.nutrientWithNameOrNull
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.unitWithAbbrOrNull
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class JsonFood(
    override val id: EntityId = MacrosEntity.NO_ID,
    override val created: Instant = 0,
    override val modified: Instant = 0,

    // The basic name of the food, e.g. 'apple', 'chicken breast', 'chocolate bar'. It should be
    // a short but descriptive name as when screen space is limited, this is the only field shown.
    @SerialName("basic_name")
    override val basicName: String,

    // Brand name or company producing the food. Usually not important for fresh produce.
    override val brand: String? = null,

    // Food-specific variety, e.g. low-fat (milk), green (apple), lean (beef mince).
    override val variety: String? = null,

    // Extra description that helps identify the food, e.g. flavour, raw/cooked, fresh/frozen.
    // The usage of this field overlaps with variety slightly, and variety alone is often sufficient.
    // This field is good for adding extra detail when needed.
    @SerialName("extra_description")
    val extraDescription: String? = null,

    // A keyword name for the food which can uniquely identify it.
    // It can be generated from basic_name, brand, variety, and extraDescription if necessary.
    @SerialName("index_name")
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    override val indexName: String = indexNamePrototype(basicName, brand, variety, extraDescription),

    // Used to store longer-form information about the food. Not displayed in lists or summaries;
    // shown in the detailed information screen for this specific food.
    override val notes: String? = null,

    val category: String? = null,

    @SerialName("usda_index")
    val usdaIndex: Int? = null,

    @SerialName("nuttab_index")
    val nuttabIndex: String? = null,

    // Source of the nutrition data, e.g. "Label", "Website", "Calculated"
    @SerialName("data_source")
    override val dataSource: String? = null,

    // Notes regarding the nutrient data, e.g. any inaccuracies or
    @SerialName("data_notes")
    override val dataNotes: String? = null,

    val density: Double? = null,

    // Allows setting a 'relevance score' for the food, e.g. to rank it higher in search
    // results and predictions. This can be added to other offsets from e.g. food type or category.
    @SerialName("relevance_offset")
    val relevanceOffset: Int = 0,

    // A set is used so that unordered comparisons can be done
    val servings: List<JsonServing> = emptyList(),
    val nutrients: Map<String, JsonQuantity> = emptyMap(),
    val ingredients: List<JsonIngredient> = emptyList(),
): JsonEntity(), FoodDescription {
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

    override val extraDesc: String?
        get() = extraDescription

    fun toRowData() = RowData(FoodTable).apply {
        put(FoodTable.ID, id)
        put(FoodTable.CREATE_TIME, created)
        put(FoodTable.MODIFY_TIME, modified)
        put(FoodTable.INDEX_NAME, indexName)
        put(FoodTable.BRAND, brand)
        put(FoodTable.VARIETY, variety)
        put(FoodTable.EXTRA_DESC, extraDescription)
        put(FoodTable.NAME, basicName)
        put(FoodTable.NOTES, notes)
        put(FoodTable.USDA_INDEX, usdaIndex)
        put(FoodTable.NUTTAB_INDEX, nuttabIndex)
        put(FoodTable.DATA_SOURCE, dataSource)
        put(FoodTable.DATA_NOTES, dataNotes)
        put(FoodTable.DENSITY, density)
        put(FoodTable.SEARCH_RELEVANCE, relevanceOffset)
        put(FoodTable.CATEGORY, category)
    }

    fun getServingData() = servings.mapNotNull {
        // if unit invalid, skip this entry
        val unit = unitWithAbbrOrNull(it.quantity.unit) ?: return@mapNotNull null
        RowData(ServingTable).apply {
            put(ServingTable.ID, MacrosEntity.NO_ID)
            put(ServingTable.CREATE_TIME, created)
            put(ServingTable.MODIFY_TIME, modified)
            put(ServingTable.FOOD_ID, id)
            put(ServingTable.QUANTITY, it.quantity.value)
            put(ServingTable.QUANTITY_UNIT, unit.abbr)
            put(ServingTable.IS_DEFAULT, it.isDefault)
            put(ServingTable.NAME, it.name)
            put(ServingTable.NOTES, it.notes)
        }
    }

    fun getNutrientValueData(): Map<Nutrient, RowData<FoodNutrientValue>> {
        return buildMap {
            for ((name, quantity) in nutrients) {
                // if nutrient or unit are invalid, skip this entry
                val nutrient = nutrientWithNameOrNull(name) ?: continue
                val unit = unitWithAbbrOrNull(quantity.unit) ?: continue
                this[nutrient] = RowData(FoodNutrientValueTable).apply {
                    put(FoodNutrientValueTable.ID, MacrosEntity.NO_ID)
                    put(FoodNutrientValueTable.CREATE_TIME, created)
                    put(FoodNutrientValueTable.MODIFY_TIME, modified)
                    put(FoodNutrientValueTable.VALUE, quantity.value)
                    put(FoodNutrientValueTable.UNIT_ID, unit.id)
                    put(FoodNutrientValueTable.NUTRIENT_ID, nutrient.id)
                    put(FoodNutrientValueTable.FOOD_ID, id)
                    put(FoodNutrientValueTable.CONSTRAINT_SPEC, quantity.constraintSpec)
                }
            }
        }
    }
}

