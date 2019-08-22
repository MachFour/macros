package com.machfour.macros.ingredients;

import com.machfour.macros.core.MacrosUtils;
import com.machfour.macros.objects.Ingredient;
import com.machfour.macros.util.StringJoiner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class CompositeFoodSpec {
    @NotNull
    final String indexName;
    @NotNull
    final String name;
    @Nullable
    final String variety;
    @Nullable
    final String notes;
    private final List<IngredientSpec> ingredients;

    CompositeFoodSpec(@NotNull String indexName, @NotNull String name, @Nullable String variety, @Nullable String notes) {
        this.indexName = indexName;
        this.name = name;
        ingredients = new ArrayList<>();
        this.variety = MacrosUtils.nullableString(variety); // empty string -> null data
        this.notes = MacrosUtils.nullableString(notes); // empty string -> null data
    }

    void addIngredients(Collection<IngredientSpec> ingredientSpecs) {
        ingredients.addAll(ingredientSpecs);
    }
    List<IngredientSpec> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    public String prettyPrint(boolean includeIngredients) {
        StringBuilder pretty = new StringBuilder(String.format("{ indexName: %s, name: %s, variety: %s, notes: %s",
                indexName, name, variety, notes));
        if (includeIngredients) {
            pretty.append(String.format(", ingredients: [%s]", StringJoiner.of(ingredients).sep(", ").join()));
        }
        pretty.append(" }");
        return pretty.toString();
    }

    @Override
    public String toString() {
        return prettyPrint(false);
    }
}
