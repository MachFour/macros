package com.machfour.macros.json

import com.machfour.macros.core.*
import com.machfour.macros.entities.*
import com.machfour.macros.foodname.FoodDescription
import com.machfour.macros.foodname.indexNamePrototype
import com.machfour.macros.json.JsonNutrientValue.Companion.toJsonNutrientValue
import com.machfour.macros.nutrients.BasicNutrientData
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.sql.rowdata.servingToRowData
import kotlinx.serialization.*

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class JsonFood(
    override val id: EntityId = MacrosEntity.NO_ID,
    override val created: Instant = 0,
    override val modified: Instant = 0,

    @Transient
    override val source: ObjectSource = ObjectSource.JSON,

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

    @SerialName("category")
    override val categoryName: String,

    @SerialName("usda_index")
    override val usdaIndex: Int? = null,

    @SerialName("nuttab_index")
    override val nuttabIndex: String? = null,

    // Source of the nutrition data, e.g. "Label", "Website", "Calculated"
    @SerialName("data_source")
    override val dataSource: String? = null,

    // Notes regarding the nutrient data, e.g. any inaccuracies or
    @SerialName("data_notes")
    override val dataNotes: String? = null,

    override val density: Double? = null,

    @SerialName("food_type")
    override val foodType: FoodType = FoodType.PRIMARY,

    // Allows setting a 'relevance score' for the food, e.g. to rank it higher in search
    // results and predictions. This can be added to other offsets from e.g. food type or category.
    @SerialName("relevance_offset")
    override val relevanceOffsetValue: Int = 0,

    // A set is used so that unordered comparisons can be done
    override val servings: List<JsonServing> = emptyList(),
    @SerialName("nutrient_data")
    override val nutrientData: JsonNutrientData,
    val ingredients: List<JsonIngredient> = emptyList(),
): JsonEntity(), IFood<JsonNutrientValue>, FoodDescription {
    companion object {
        private val Food.jsonServings: List<JsonServing>
            get() = servings.map { JsonServing(it) }

        private fun BasicNutrientData<FoodNutrientValue>.jsonNutrientData(): JsonNutrientData {
            val incompleteDataNutrients = HashSet<INutrient>()
            val nutrients = buildSet {
                for ((n, nv) in nutrientValues()) {
                    add(nv.toJsonNutrientValue())
                    if (hasIncompleteData(n)) {
                        incompleteDataNutrients.add(n)
                    }
                }
            }
            return JsonNutrientData(
                perQuantity = JsonQuantity(perQuantity),
                foodDensity = foodDensity,
                nutrients = nutrients,
                incompleteData = incompleteDataNutrients
            )
        }
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
        categoryName = f.categoryName,
        dataSource = f.dataSource,
        dataNotes = f.dataNotes,
        density = f.density,
        foodType = f.foodType,
        usdaIndex = f.usdaIndex,
        nuttabIndex = f.nuttabIndex,
        relevanceOffsetValue = f.relevanceOffset.value,
        servings = f.jsonServings,
        nutrientData = f.nutrientData.jsonNutrientData(),
    )

    override val createTime: Instant
        get() = created

    override val modifyTime: Instant
        get() = modified

    override val extraDesc: String?
        get() = extraDescription

    @Transient
    override lateinit var category: FoodCategory

    fun getServingData() = servings.map { servingToRowData(it) }

    override val relevanceOffset: SearchRelevance
        get() = SearchRelevance.fromValue(relevanceOffsetValue)

    override val searchRelevance: SearchRelevance
        get() = foodType.baseSearchRelevance + relevanceOffset

    fun getNutrientValueData(): Map<INutrient, RowData<FoodNutrientValue>> {
        val rowDataTemplate = RowData(FoodNutrientValueTable).apply {
            put(FoodNutrientValueTable.ID, MacrosEntity.NO_ID)
            put(FoodNutrientValueTable.CREATE_TIME, created)
            put(FoodNutrientValueTable.MODIFY_TIME, modified)
            put(FoodNutrientValueTable.FOOD_ID, id)
        }
        return buildMap {
            this[QUANTITY] = rowDataTemplate.copy().apply {
                put(FoodNutrientValueTable.VALUE, quantity.amount)
                put(FoodNutrientValueTable.UNIT_ID, quantity.unit.id)
                put(FoodNutrientValueTable.NUTRIENT_ID, QUANTITY.id)
                put(FoodNutrientValueTable.CONSTRAINT_SPEC, quantity.constraintSpec)
            }
            for (value in nutrientData.nutrients) {
                // if nutrient or unit are invalid, skip this entry
                this[value.nutrient] = rowDataTemplate.copy().apply {
                    put(FoodNutrientValueTable.VALUE, value.amount)
                    put(FoodNutrientValueTable.UNIT_ID, value.unit.id)
                    put(FoodNutrientValueTable.NUTRIENT_ID, value.nutrient.id)
                    put(FoodNutrientValueTable.CONSTRAINT_SPEC, value.constraintSpec)
                }
            }
        }
    }
}

