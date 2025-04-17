package com.machfour.macros.sql.rowdata

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.MacrosSqlEntity
import com.machfour.macros.entities.*
import com.machfour.macros.nutrients.INutrientValue
import com.machfour.macros.schema.*
import com.machfour.macros.units.UnitType.Companion.toFlags

fun servingToRowData(s: IServing): RowData<Serving> {
    if (s is Serving) {
        return s.data
    }
    return RowData(ServingTable).apply {
        put(ServingTable.ID, s.id)
        put(ServingTable.CREATE_TIME, s.createTime)
        put(ServingTable.MODIFY_TIME, s.modifyTime)
        put(ServingTable.NAME, s.name)
        put(ServingTable.NOTES, s.notes)
        put(ServingTable.FOOD_ID, s.foodId)
        put(ServingTable.IS_DEFAULT, s.isDefault)
        put(ServingTable.QUANTITY, s.amount)
        put(ServingTable.QUANTITY_UNIT, s.unit.abbr)
    }
}

fun foodPortionToRowData(fp: IFoodPortion<*>): RowData<FoodPortion> {
    if (fp is FoodPortion) {
        return fp.data
    }
    return RowData(FoodPortionTable).apply {
        put(FoodPortionTable.ID, fp.id)
        put(FoodPortionTable.CREATE_TIME, fp.createTime)
        put(FoodPortionTable.MODIFY_TIME, fp.modifyTime)
        put(FoodPortionTable.QUANTITY, fp.quantity)
        put(FoodPortionTable.QUANTITY_UNIT, fp.qtyUnit.abbr)
        put(FoodPortionTable.NOTES, fp.notes)
        put(FoodPortionTable.SERVING_ID, fp.servingId)
        put(FoodPortionTable.NUTRIENT_MAX_VERSION, fp.maxNutrientVersion)
        put(FoodPortionTable.MEAL_ID, fp.mealId)
        put(FoodPortionTable.FOOD_ID, fp.foodId)
    }
}

fun ingredientToRowData(i: Ingredient): RowData<Ingredient> {
    return RowData(IngredientTable).apply {
        put(IngredientTable.ID, i.id)
        put(IngredientTable.CREATE_TIME, i.createTime)
        put(IngredientTable.MODIFY_TIME, i.modifyTime)
        put(IngredientTable.QUANTITY, i.quantity)
        put(IngredientTable.QUANTITY_UNIT, i.qtyUnit.abbr)
        put(IngredientTable.NUTRIENT_MAX_VERSION, i.maxNutrientVersion)
        put(IngredientTable.SERVING_ID, i.servingId)
        put(IngredientTable.FOOD_ID, i.foodId)
        put(IngredientTable.PARENT_FOOD_ID, i.parentFoodId)
        put(IngredientTable.NOTES, i.notes)
    }
}

fun mealToRowData(m: Meal): RowData<Meal> {
    return RowData(MealTable).apply {
        put(MealTable.ID, m.id)
        put(MealTable.CREATE_TIME, m.createTime)
        put(MealTable.MODIFY_TIME, m.modifyTime)
        put(MealTable.NAME, m.name)
        put(MealTable.NOTES, m.notes)
        put(MealTable.DAY, m.day)
        put(MealTable.START_TIME, m.startTime)
        put(MealTable.DURATION, m.durationSeconds)
    }
}

fun foodToRowData(f: IFood<*>): RowData<Food> {
    if (f is Food) {
        return f.data
    }
    return RowData(FoodTable).apply {
        put(FoodTable.ID, f.id)
        put(FoodTable.CREATE_TIME, f.createTime)
        put(FoodTable.MODIFY_TIME, f.modifyTime)
        put(FoodTable.INDEX_NAME, f.indexName)
        put(FoodTable.BRAND, f.brand)
        put(FoodTable.VARIETY, f.variety)
        put(FoodTable.EXTRA_DESC, f.extraDesc)
        put(FoodTable.NAME, f.basicName)
        put(FoodTable.NOTES, f.notes)
        put(FoodTable.USDA_INDEX, f.usdaIndex)
        put(FoodTable.NUTTAB_INDEX, f.nuttabIndex)
        put(FoodTable.DATA_SOURCE, f.dataSource)
        put(FoodTable.DATA_NOTES, f.dataNotes)
        put(FoodTable.DENSITY, f.density)
        put(FoodTable.SEARCH_RELEVANCE, f.relevanceOffsetValue)
        put(FoodTable.CATEGORY, f.categoryName)
        put(FoodTable.FOOD_TYPE, f.foodType.niceName)
    }
}

fun foodNutrientValueToRowData(nv: INutrientValue): RowData<FoodNutrientValue> {
    if (nv is FoodNutrientValue) {
        return nv.data
    }

    return RowData(FoodNutrientValueTable).apply {
        put(FoodNutrientValueTable.ID, nv.id)
        put(FoodNutrientValueTable.CREATE_TIME, nv.createTime)
        put(FoodNutrientValueTable.MODIFY_TIME, nv.modifyTime)
        put(FoodNutrientValueTable.NUTRIENT_ID, nv.nutrient.id)
        put(FoodNutrientValueTable.VALUE, nv.amount)
        put(FoodNutrientValueTable.UNIT_ID, nv.unit.id)
        put(FoodNutrientValueTable.CONSTRAINT_SPEC, nv.constraintSpec)
        //put(FoodNutrientValueTable.VERSION, FoodNutrientValueTable.VERSION.defaultData)
        //if (nv is FoodNutrientValue) {
        //    put(FoodNutrientValueTable.FOOD_ID, nv.foodId)
        //}
    }
}

fun nutrientToRowData(n: INutrient): RowData<Nutrient> {
    if (n is Nutrient) {
        return n.data
    }

    return RowData(NutrientTable).apply {
        put(NutrientTable.ID, n.id)
        put(NutrientTable.CREATE_TIME, n.createTime)
        put(NutrientTable.MODIFY_TIME, n.modifyTime)
        put(NutrientTable.NAME, n.name)
        put(NutrientTable.INBUILT, n.isInbuilt)
        put(NutrientTable.UNIT_TYPES, n.unitTypes.toFlags())
    }
}

fun <M: MacrosSqlEntity<M>> RowData<M>.removeMetadata() = apply {
    put(table.idColumn, MacrosEntity.NO_ID)
    put(table.createTimeColumn, 0L)
    put(table.modifyTimeColumn, 0L)
}