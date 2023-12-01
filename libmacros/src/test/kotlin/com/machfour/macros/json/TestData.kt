package com.machfour.macros.json

val apple = JsonFood(
    basicName = "apple",
    brand = null,
    variety = "pink lady",
    extraDescription = null,
    indexName = "apple-pink",
    notes = null,
    category = "fruit and vegetables",
    usdaIndex = null,
    nuttabIndex = "06D10433",
    dataSource = "NUTTAB",
    dataNotes = null,
    density = null,
    relevanceOffset = 0,
    servings = setOf(
        JsonServing(
            name = "small",
            quantity = 80.0,
            quantityUnit = "g",
            isDefault = false,
            notes = "about the size of a mandarin",
        ),
        JsonServing(
            name = "large",
            quantity = 120.0,
            quantityUnit = "g",
            isDefault = false,
            notes = "about the size of a tennis ball",
        )
    ),
    nutrients = mapOf(
        "quantity" to JsonNutrientValue(value = 100.0, unit = "g"),
        "energy" to JsonNutrientValue(value = 247.0, unit = "kJ"),
        "protein" to JsonNutrientValue(value = 0.3, unit = "g"),
        "fat" to JsonNutrientValue(value = 0.4, unit = "g"),
        "saturated_fat" to JsonNutrientValue(value = 0.0, unit = "g"),
        "carbohydrate" to JsonNutrientValue(value = 12.4, unit = "g"),
        "sugar" to JsonNutrientValue(value = 11.9, unit = "g"),
        "fibre" to JsonNutrientValue(value = 2.4, unit = "g"),
        "sodium" to JsonNutrientValue(value = 0.0, unit = "mg"),
    )
)

// JSON data is permuted slightly from default field order,
// just to make sure we're not exactly string matching
val appleJSON = """
    {
        "basic_name": "apple",
        "variety": "pink lady",
        "category": "fruit and vegetables",
        "index_name": "apple-pink",
        "data_source": "NUTTAB",
        "nuttab_index": "06D10433",
        "servings": [
            {
                "name": "large",
                "quantity": 120.0,
                "quantity_unit": "g",
                "notes": "about the size of a tennis ball"
            },
            {
                "name": "small",
                "quantity": 80.0,
                "quantity_unit": "g",
                "notes": "about the size of a mandarin"
            }
        ],
        "nutrients": {
            "quantity": { 
                "value": 100.0, 
                "unit": "g"
            },
            "energy": {
                "value": 247.0,
                "unit": "kJ"
            },
            "protein": {
                "value": 0.3,
                "unit": "g"
            },
            "fat": { "value": 0.4, "unit": "g" },
            "saturated_fat": { "value": 0.0, "unit": "g" },
            "carbohydrate": { "value": 12.4, "unit": "g" },
            "sugar": { "value": 11.9, "unit": "g" },
            "fibre": { "value": 2.4, "unit": "g" },
            "sodium": { "value": 0.0, "unit": "mg" }
        }
    }
""".trimIndent()

val carrotCakes = JsonFood(
    id = -100,
    created = 0,
    modified = 0,
    basicName = "cakes",
    brand = "Noshu",
    variety = "iced carrot",
    extraDescription = null,
    indexName = "noshu-carrot-cakes",
    notes = "sugar free",
    category = "desserts",
    usdaIndex = null,
    nuttabIndex = null,
    dataSource = "Label",
    dataNotes = "7.4g xylitol, 6.7g erythritol, 5.2g glycerol, 6.7g polydextrose",
    density = null,
    relevanceOffset = 0,
    servings = setOf(
        JsonServing(
            name = "slice",
            quantity = 75.0,
            quantityUnit = "g",
            isDefault = true,
            notes = null,
        )
    ),
    nutrients = mapOf(
        "quantity" to JsonNutrientValue(
            id = -100,
            created = 0,
            modified = 0,
            value = 100.0,
            unit = "g",
            constraintSpec = 0,
        ),
        "energy" to JsonNutrientValue(value = 1262.0, unit = "kJ"),
        "protein" to JsonNutrientValue(value = 3.0, unit = "g"),
        "fat" to JsonNutrientValue(value = 18.4, unit = "g"),
        "saturated_fat" to JsonNutrientValue(value = 5.9, unit = "g"),
        "carbohydrate" to JsonNutrientValue(value = 14.4, unit = "g"),
        "sugar" to JsonNutrientValue(value = 3.8, unit = "g"),
        "fibre" to JsonNutrientValue(value = 19.4, unit = "g"),
        "sodium" to JsonNutrientValue(value = 284.0, unit = "mg"),
        "sugar_alcohol" to JsonNutrientValue(value = 19.3, unit = "g"),
    )
)

// JSON data is permuted slightly from default field order,
// just to make sure we're not exactly string matching
val carrotCakesJSON = """
    {
        "basic_name": "cakes",
        "index_name": "noshu-carrot-cakes",
        "brand": "Noshu",
        "variety": "iced carrot",
        "category": "desserts",
        "notes": "sugar free",
        "data_source": "Label",
        "data_notes": "7.4g xylitol, 6.7g erythritol, 5.2g glycerol, 6.7g polydextrose",
        "servings": [
            {
                "name": "slice",
                "quantity": 75.0,
                "quantity_unit": "g",
                "is_default": true
            }
        ],
        "nutrients": {
            "quantity": {
                "value": 100.0,
                "unit": "g"
            },
            "energy": {
                "value": 1262.0,
                "unit": "kJ"
            },
            "protein": {
                "value": 3.0,
                "unit": "g"
            },
            "fat": {
                "value": 18.4,
                "unit": "g"
            },
            "saturated_fat": {
                "value": 5.9,
                "unit": "g"
            },
            "carbohydrate": {
                "value": 14.4,
                "unit": "g"
            },
            "sugar": {
                "value": 3.8,
                "unit": "g"
            },
            "sugar_alcohol": {
                "value": 19.3,
                "unit": "g"
            },
            "sodium": {
                "value": 284.0,
                "unit": "mg"
            },
            "fibre": {
                "value": 19.4,
                "unit": "g"
            }
        }
    }
""".trimIndent()

val deserializedTestFoods = listOf(apple, carrotCakes)
val serializedTestFoods = listOf(appleJSON, carrotCakesJSON)
