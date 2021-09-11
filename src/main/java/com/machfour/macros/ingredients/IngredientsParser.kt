package com.machfour.macros.ingredients

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.reflect.TypeToken
import com.machfour.macros.core.FoodType
import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.entities.CompositeFood
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Ingredient
import com.machfour.macros.queries.*
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.IngredientTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.validation.SchemaViolation
import java.io.IOException
import java.io.Reader
import java.sql.SQLException

/*
 * Ingredient file format:
 * [
 * {
 *      "name": "<name>",                           (mandatory)
 *      "index_name": "<index name>",               (mandatory)
 *      "variety": "<variety>",                     (optional)
 *      "notes": "<notes>",                         (optional)
 *      "ingredients": [
 *          "index_name": "<ingredient index name>" (mandatory)
 *          "quantity": <quantity (float)>,         (mandatory)
 *          "quantity_unit": "<unit abbr>",         (mandatory)
 *          "notes": "<notes>",                     (optional)
 *       ]
 * },
 * {
 *      ...  <second composite food>
 * },
 * ...  <more composite foods>
 * ]
 *
 */
@Throws(IOException::class)
fun deserialiseIngredientsJson(json: Reader?): Collection<CompositeFoodSpec> {
    // this creates an anonymous subclass of typetoken?
    val collectionType = object : TypeToken<Collection<CompositeFoodSpec>>(){}.type
    val builder = GsonBuilder()
    // if PointAdapter didn't check for nulls in its read/write methods, you should instead use
    // builder.registerTypeAdapter(Point.class, new PointAdapter().nullSafe());
    builder.registerTypeAdapter(CompositeFoodSpec::class.java, CompositeFoodAdapter().nullSafe())
    val parser = builder.create()
    return try {
        parser.fromJson(json, collectionType)
    } catch (e: JsonIOException) {
        throw IOException(e)
    }
}

private fun processIngredientSpec(spec: IngredientSpec, composite: Food, ingredientMap: Map<String, Long>): Ingredient {
    // get food from index name
    // get quantity
    // get quantity unit
    // get notes
    // create Ingredients Object
    if (!ingredientMap.containsKey(spec.indexName)) {
        throw RuntimeException(String.format("No food found in ingredientMap with index name %s", spec.indexName))
    }
    val ingredientId = ingredientMap[spec.indexName]
    val builder = MacrosBuilder(Ingredient.table)
    builder.setField(IngredientTable.PARENT_FOOD_ID, composite.id)
    builder.setField(IngredientTable.FOOD_ID, ingredientId)
    builder.setField(IngredientTable.SERVING_ID, null) // TODO
    builder.setField(IngredientTable.QUANTITY_UNIT, spec.unit)
    builder.setField(IngredientTable.NOTES, spec.notes)
    builder.setField(IngredientTable.QUANTITY, spec.quantity)
    if (builder.hasAnyInvalidFields) {
        throw SchemaViolation(builder.allErrors)
        // throw SchemaViolation
    }
    return builder.build()
}

private fun extractIngredientIndexNames(allSpecs: Collection<CompositeFoodSpec>): Set<String> {
    // say there are an average of 4 ingredients per composite food
    val indexNames: MutableSet<String> = HashSet(4 * allSpecs.size)
    for (cSpec in allSpecs) {
        for ((indexName) in cSpec.getIngredients()) {
            indexNames.add(indexName)
        }
    }
    return indexNames
}

// creates a composite food and ingredients objects from the given spec
// NOTE that no IDs are ever created for the objects
private fun processCompositeFoodSpec(spec: CompositeFoodSpec, indexNameMap: Map<String, Long>): CompositeFood {
    val builder = MacrosBuilder(Food.table)
    builder.setField(FoodTable.INDEX_NAME, spec.indexName)
    builder.setField(FoodTable.NAME, spec.name)
    builder.setField(FoodTable.VARIETY, spec.variety)
    builder.setField(FoodTable.NOTES, spec.notes)
    builder.setField(FoodTable.CATEGORY, "recipes") //TODO
    // setting this means that Food.factory().construct() will create a CompositeFood
    builder.setField(FoodTable.FOOD_TYPE, FoodType.COMPOSITE.niceName)
    if (builder.hasAnyInvalidFields) {
        throw SchemaViolation(builder.allErrors)
        // throw SchemaViolation
    }
    val composite = builder.build() as CompositeFood
    // create the ingredients
    for (iSpec in spec.getIngredients()) {
        // TODO remember that composite has no id!!
        val i = processIngredientSpec(iSpec, composite, indexNameMap)
        composite.addIngredient(i)
    }
    return composite // NOTE ingredients have not yet been saved into the DB!
}

// Creates objects corresponding to the parsed composite food specs.
// THE INGREDIENTS CANNOT BE SAVED INTO THE DATABASE AS IS, because they do not have the proper foreign keys set up
// to save the object tree correctly, use the method saveCompositeFoods(compositeFoods, ds)
@Throws(SQLException::class)
fun createCompositeFoods(
    parseResult: Collection<CompositeFoodSpec>,
    ds: SqlDatabase
): List<CompositeFood> {
    val indexNames = extractIngredientIndexNames(parseResult)
    // for invalid index names, the map won't have an entry
    val indexNameMap = getFoodIdsByIndexName(ds, indexNames)
    val results: MutableList<CompositeFood> = ArrayList(parseResult.size)
    for (spec in parseResult) {
        results.add(processCompositeFoodSpec(spec, indexNameMap))
    }
    val ingredientFoods = getFoodsById(ds, indexNameMap.values)

    // go through and create object links so that we can have a proper object tree without having to save to DB first
    for (cf in results) {
        for (i in cf.ingredients) {
            i.initCompositeFood(cf)
            i.initFoodAndNd(ingredientFoods.getValue(i.foodId))
        }
    }
    return results
}

private fun addCompositeFoodId(newIngredients: List<Ingredient>, id: Long): List<Ingredient> {
    val ingredientsWithId: MutableList<Ingredient> = ArrayList(newIngredients.size)
    for (i in newIngredients) {
        val builder = MacrosBuilder(i)
        builder.setField(IngredientTable.PARENT_FOOD_ID, id)
        ingredientsWithId.add(builder.build())
    }
    return ingredientsWithId
}

// saves a composite food and all its ingredients into the database
@Throws(SQLException::class)
private fun saveCompositeFood(cf: CompositeFood, ds: SqlDatabase) {
    try {
        ds.openConnection()
        // If inserting ingredients fails, we want to be able to roll back the whole thing.
        ds.beginTransaction()

        // First save the food and then retrieve it from the database, to get the ID
        saveObject(ds, cf)
        val saved = getFoodByIndexName(ds, cf.indexName)
            ?: throw SQLException("Could not retrieved saved composite food")
        val id = saved.id

        // Now we can edit the ingredients to have the ID
        // TODO use completeFk function
        val newIngredients = addCompositeFoodId(cf.ingredients, id)
        // here we go!
        insertObjects(ds, newIngredients, false)

        // TODO nutrition data object to go along with it, if quantity is known
        //MacrosBuilder<NutrientData> nData = new MacrosBuilder<>(NutrientData.table);
        //nData.setField(Schema.NutrientDataTable.DATA_SOURCE, "recipe");
        //nData.setField(Schema.NutrientDataTable.FOOD_ID, id);
        //nData.setField(Schema.NutrientDataTable.QUANTITY ,";
        ds.endTransaction()
    } finally {
        ds.closeConnection()
    }
}

// returns list of index names of foods that were created
@Throws(SQLException::class, IOException::class)
fun readRecipes(json: Reader, ds: SqlDatabase): List<CompositeFood> {
    val ingredientSpecs = deserialiseIngredientsJson(json)
    return createCompositeFoods(ingredientSpecs, ds)
}

@Throws(SQLException::class)
fun saveRecipes(compositeFoods: Collection<CompositeFood>, ds: SqlDatabase) {
    // TODO save all the composite foods and recreate them in one go
    // Then, save the ingredients at the same time.
    for (cf in compositeFoods) {
        saveCompositeFood(cf, ds)
    }
}