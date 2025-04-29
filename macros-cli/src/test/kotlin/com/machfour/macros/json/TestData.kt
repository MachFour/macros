package com.machfour.macros.json

import com.machfour.macros.core.MacrosEntity

val apple = JsonFood(
    basicName = "apple",
    brand = null,
    variety = "pink lady",
    extraDescription = null,
    indexName = "apple-pink",
    notes = null,
    categoryName = "fruit and vegetables",
    usdaIndex = null,
    nuttabIndex = "06D10433",
    dataSource = "NUTTAB",
    dataNotes = null,
    density = null,
    relevanceOffsetValue = 0,
    servings = listOf(
        JsonServing(
            name = "small",
            quantity = JsonQuantity(amount = 80.0, unitAbbr = "g"),
            isDefault = false,
            notes = "about the size of a mandarin",
        ),
        JsonServing(
            name = "large",
            quantity = JsonQuantity(amount = 120.0, unitAbbr = "g"),
            isDefault = false,
            notes = "about the size of a tennis ball",
        )
    ),
    nutrientData = JsonNutrientData(
        perQuantity = JsonQuantity(amount = 100.0, unitAbbr = "g"),
        nutrients = setOf(
            JsonNutrientValue(nutrientName = "energy", amount = 247.0, unitAbbr = "kJ"),
            JsonNutrientValue(nutrientName = "protein", amount = 0.3, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "fat", amount = 0.4, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "saturated_fat", amount = 0.0, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "carbohydrate", amount = 12.4, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "sugar", amount = 11.9, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "fibre", amount = 2.4, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "sodium", amount = 0.0, unitAbbr = "mg"),
        )
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
                "quantity": { "amount": 80.0, "unit": "g" },
                "notes": "about the size of a mandarin"
            },
            {
                "name": "large",
                "quantity": { "amount": 120.0, "unit": "g" },
                "notes": "about the size of a tennis ball"
            }
        ],
        "nutrient_data": {
            "per_quantity": { "amount": 100.0, "unit": "g" },
            "nutrients": [
                { "nutrient": "energy", "amount": 247.0, "unit": "kJ" },
                { "nutrient": "protein", "amount": 0.3, "unit": "g" },
                { "nutrient": "fat",  "amount": 0.4, "unit": "g" },
                { "nutrient": "saturated_fat", "amount": 0.0, "unit": "g" },
                { "nutrient": "carbohydrate", "amount": 12.4, "unit": "g" },
                { "nutrient": "sugar", "amount": 11.9, "unit": "g" },
                { "nutrient": "fibre", "amount": 2.4, "unit": "g" },
                { "nutrient": "sodium", "amount": 0.0, "unit": "mg" }
            ]
        }
    }
""".trimIndent()

val carrotCakes = JsonFood(
    id = MacrosEntity.NO_ID,
    created = 0,
    modified = 0,
    basicName = "cakes",
    brand = "Noshu",
    variety = "iced carrot",
    extraDescription = null,
    indexName = "noshu-carrot-cakes",
    notes = "sugar free",
    categoryName = "desserts",
    usdaIndex = null,
    nuttabIndex = null,
    dataSource = "Label",
    density = null,
    relevanceOffsetValue = 0,
    servings = listOf(
        JsonServing(
            name = "slice",
            quantity = JsonQuantity(amount = 75.0, unitAbbr = "g"),
            isDefault = true,
            notes = null,
        )
    ),
    nutrientData = JsonNutrientData(
        perQuantity = JsonQuantity(
                id = MacrosEntity.NO_ID,
                created = 0,
                modified = 0,
                amount = 100.0,
                unitAbbr = "g",
                constraintSpec = 0,
        ),
        nutrients = setOf(
            JsonNutrientValue(nutrientName = "energy", amount = 1262.0, unitAbbr = "kJ"),
            JsonNutrientValue(nutrientName = "protein", amount = 3.0, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "fat", amount = 18.4, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "saturated_fat", amount = 5.9, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "carbohydrate", amount = 14.4, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "sugar", amount = 3.8, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "fibre", amount = 19.4, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "sodium", amount = 284.0, unitAbbr = "mg"),
            JsonNutrientValue(nutrientName = "sugar_alcohol", amount = 19.3, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "xylitol", amount = 7.4, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "erythritol", amount = 6.7, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "glycerol", amount = 5.2, unitAbbr = "g"),
            JsonNutrientValue(nutrientName = "polydextrose", amount = 6.7, unitAbbr = "g"),
        )
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
                "quantity": { "amount": 75.0, "unit": "g" },
                "is_default": true
            }
        ],
        "nutrient_data": {
            "per_quantity": { "amount": 100.0, "unit": "g" },
            "nutrients": [
                { "nutrient": "energy", "amount": 1262.0, "unit": "kJ" },
                { "nutrient": "protein", "amount": 3.0, "unit": "g" },
                { "nutrient": "fat", "amount": 18.4, "unit": "g" },
                { "nutrient": "saturated_fat", "amount": 5.9, "unit": "g" },
                { "nutrient": "carbohydrate", "amount": 14.4, "unit": "g" },
                { "nutrient": "sugar", "amount": 3.8, "unit": "g" },
                { "nutrient": "sugar_alcohol", "amount": 19.3, "unit": "g" },
                { "nutrient": "sodium", "amount": 284.0, "unit": "mg" },
                { "nutrient": "fibre", "amount": 19.4, "unit": "g" },
                { "nutrient": "xylitol", "amount": 7.4, "unit": "g" },
                { "nutrient": "erythritol", "amount": 6.7, "unit": "g" },
                { "nutrient": "glycerol", "amount": 5.2, "unit": "g" },
                { "nutrient": "polydextrose", "amount": 6.7, "unit": "g" }
            ]
        }
    }
""".trimIndent()

val deserializedTestFoods = listOf(apple, carrotCakes)
val serializedTestFoods = listOf(appleJSON, carrotCakesJSON)
