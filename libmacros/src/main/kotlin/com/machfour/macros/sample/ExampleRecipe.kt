package com.machfour.macros.sample

import com.machfour.macros.core.FoodType
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.id
import com.machfour.macros.entities.CompositeFood
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Ingredient
import com.machfour.macros.nutrients.*
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.IngredientTable
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.CALORIES
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLIGRAMS

val exampleIngredientFood1: Food by lazy {
    initIngredientFood1()
}

val exampleIngredientFood2: Food by lazy {
    initIngredientFood2()
}

val exampleIngredient1: Ingredient by lazy {
    initIngredient1()
}

val exampleIngredient2: Ingredient by lazy {
    initIngredient2()
}

val exampleRecipe1: CompositeFood by lazy {
    initRecipe1()
}

private fun initIngredient1(): Ingredient {
    val i: Ingredient = Ingredient.factory.construct(RowData(IngredientTable).apply {
        put(IngredientTable.ID, 1.id)
        put(IngredientTable.FOOD_ID, 1.id)
        put(IngredientTable.PARENT_FOOD_ID, 3.id)
        put(IngredientTable.SERVING_ID, null) // TODO
        put(IngredientTable.QUANTITY, 100.0)
        put(IngredientTable.QUANTITY_UNIT, "g")
        put(IngredientTable.NOTES, "notes i1")

    }, ObjectSource.TEST)

    i.initFoodAndNd(exampleIngredientFood1)

    return i
}

private fun initIngredient2(): Ingredient {
    val i: Ingredient = Ingredient.factory.construct(RowData(IngredientTable).apply {
        put(IngredientTable.ID, 2.id)
        put(IngredientTable.FOOD_ID, 2.id)
        put(IngredientTable.PARENT_FOOD_ID, 3.id)
        put(IngredientTable.QUANTITY, 10.0)
        put(IngredientTable.QUANTITY_UNIT, "g")
        put(IngredientTable.NOTES, "notes i2")

    }, ObjectSource.TEST)

    i.initFoodAndNd(exampleIngredientFood2)
    return i
}

private fun initIngredientFood1(): Food {
    val f = Food.factory.construct(RowData(FoodTable).apply {
        put(FoodTable.ID, 1.id)
        put(FoodTable.INDEX_NAME, "ingredient-1")
        put(FoodTable.BRAND, "brand 1")
        put(FoodTable.VARIETY, "variety 1")
        put(FoodTable.NAME, "ingredient 1")
        put(FoodTable.NOTES, "notes 1")
        put(FoodTable.DATA_NOTES, "data notes 1")
        put(FoodTable.DATA_SOURCE, "data source 1")
        put(FoodTable.CATEGORY, "category 1")
        put(FoodTable.FOOD_TYPE, FoodType.PRIMARY.niceName)
        put(FoodTable.USDA_INDEX, null)
        put(FoodTable.NUTTAB_INDEX, null)
    }, ObjectSource.TEST)

    listOf(
        FoodNutrientValue.makeComputedValue(300.0, ENERGY, CALORIES),
        FoodNutrientValue.makeComputedValue(10.0, PROTEIN, GRAMS),
        FoodNutrientValue.makeComputedValue(40.0, CARBOHYDRATE, GRAMS),
        FoodNutrientValue.makeComputedValue(50.0, FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(21.0, SATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(3.0, SUGAR, GRAMS),
        FoodNutrientValue.makeComputedValue(31.0, POLYUNSATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(11.0, MONOUNSATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(5.0, FIBRE, GRAMS),
        FoodNutrientValue.makeComputedValue(32.0, SODIUM, MILLIGRAMS),
        FoodNutrientValue.makeComputedValue(100.0, QUANTITY, GRAMS),
    ).onEach { f.addNutrientValue(it) }

    return f
}

private fun initIngredientFood2(): Food {
    val f = Food.factory.construct(RowData(FoodTable).apply {
        put(FoodTable.ID, 2.id)
        put(FoodTable.INDEX_NAME, "ingredient-2")
        put(FoodTable.BRAND, "brand 2")
        put(FoodTable.VARIETY, "variety 2")
        put(FoodTable.NAME, "ingredient 2")
        put(FoodTable.NOTES, "notes 2")
        put(FoodTable.DATA_NOTES, "data notes 2")
        put(FoodTable.DATA_SOURCE, "data source 2")
        put(FoodTable.CATEGORY, "category 2")
        put(FoodTable.FOOD_TYPE, FoodType.PRIMARY.niceName)
    }, ObjectSource.TEST)

    listOf(
        FoodNutrientValue.makeComputedValue(500.0, ENERGY, CALORIES),
        FoodNutrientValue.makeComputedValue(20.0, PROTEIN, GRAMS),
        FoodNutrientValue.makeComputedValue(30.0, CARBOHYDRATE, GRAMS),
        FoodNutrientValue.makeComputedValue(10.0, FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(5.0, SATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(15.0, SUGAR, GRAMS),
        FoodNutrientValue.makeComputedValue(5.0, POLYUNSATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(5.0, MONOUNSATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(5.0, FIBRE, GRAMS),
        FoodNutrientValue.makeComputedValue(100.0, QUANTITY, GRAMS),
    ).onEach { f.addNutrientValue(it) }

    return f
}

private fun initRecipe1(): CompositeFood {
    val cf = Food.factory.construct(RowData(FoodTable).apply {
        put(FoodTable.ID, 3.id)
        put(FoodTable.INDEX_NAME, "recipe-1")
        put(FoodTable.VARIETY, "a variety")
        put(FoodTable.NAME, "recipe 1")
        put(FoodTable.NOTES, "notes")
        put(FoodTable.DATA_NOTES, "ingredient 1 data notes")
        put(FoodTable.DATA_SOURCE, "ingredient 1 data source")
        put(FoodTable.CATEGORY, "ingredient 1 category")
        put(FoodTable.FOOD_TYPE, FoodType.COMPOSITE.niceName)
        put(FoodTable.USDA_INDEX, null)
        put(FoodTable.NUTTAB_INDEX, null)
    }, ObjectSource.TEST)  as CompositeFood

    cf.addIngredient(exampleIngredient1)
    cf.addIngredient(exampleIngredient2)

    exampleIngredient1.initCompositeFood(cf)
    exampleIngredient2.initCompositeFood(cf)

    return cf
}

