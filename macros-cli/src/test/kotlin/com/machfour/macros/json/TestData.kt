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
    servings = listOf(
        JsonServing(
            name = "small",
            quantity = JsonQuantity(value = 80.0, unit = "g"),
            isDefault = false,
            notes = "about the size of a mandarin",
        ),
        JsonServing(
            name = "large",
            quantity = JsonQuantity(value = 120.0, unit = "g"),
            isDefault = false,
            notes = "about the size of a tennis ball",
        )
    ),
    nutrients = mapOf(
        "quantity" to JsonQuantity(value = 100.0, unit = "g"),
        "energy" to JsonQuantity(value = 247.0, unit = "kJ"),
        "protein" to JsonQuantity(value = 0.3, unit = "g"),
        "fat" to JsonQuantity(value = 0.4, unit = "g"),
        "saturated_fat" to JsonQuantity(value = 0.0, unit = "g"),
        "carbohydrate" to JsonQuantity(value = 12.4, unit = "g"),
        "sugar" to JsonQuantity(value = 11.9, unit = "g"),
        "fibre" to JsonQuantity(value = 2.4, unit = "g"),
        "sodium" to JsonQuantity(value = 0.0, unit = "mg"),
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
                "name": "small",
                "quantity": { "value": 80.0, "unit": "g" },
                "notes": "about the size of a mandarin"
            },
            {
                "name": "large",
                "quantity": { "value": 120.0, "unit": "g" },
                "notes": "about the size of a tennis ball"
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
    density = null,
    relevanceOffset = 0,
    servings = listOf(
        JsonServing(
            name = "slice",
            quantity = JsonQuantity(value = 75.0, unit = "g"),
            isDefault = true,
            notes = null,
        )
    ),
    nutrients = mapOf(
        "quantity" to JsonQuantity(
            id = -100,
            created = 0,
            modified = 0,
            value = 100.0,
            unit = "g",
            constraintSpec = 0,
        ),
        "energy" to JsonQuantity(value = 1262.0, unit = "kJ"),
        "protein" to JsonQuantity(value = 3.0, unit = "g"),
        "fat" to JsonQuantity(value = 18.4, unit = "g"),
        "saturated_fat" to JsonQuantity(value = 5.9, unit = "g"),
        "carbohydrate" to JsonQuantity(value = 14.4, unit = "g"),
        "sugar" to JsonQuantity(value = 3.8, unit = "g"),
        "fibre" to JsonQuantity(value = 19.4, unit = "g"),
        "sodium" to JsonQuantity(value = 284.0, unit = "mg"),
        "sugar_alcohol" to JsonQuantity(value = 19.3, unit = "g"),
        "xylitol" to JsonQuantity(value = 7.4, unit = "g"),
        "erythritol" to JsonQuantity(value = 6.7, unit = "g"),
        "glycerol" to JsonQuantity(value = 5.2, unit = "g"),
        "polydextrose" to JsonQuantity(value = 6.7, unit = "g"),
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
        "servings": [
            {
                "name": "slice",
                "quantity": { "value": 75.0, "unit": "g" },
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
            "xylitol": {
                "value": 7.4,
                "unit": "g"
            },
            "erythritol": {
                "value": 6.7,
                "unit": "g"
            },
            "glycerol": {
                "value": 5.2,
                "unit": "g"
            },
            "polydextrose": {
                "value": 6.7,
                "unit": "g"
            },
        }
    }
""".trimIndent()

val deserializedTestFoods = listOf(apple, carrotCakes)
val serializedTestFoods = listOf(appleJSON, carrotCakesJSON)
