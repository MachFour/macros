package com.machfour.macros.ingredients;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class IngredientSpec {
    @NotNull
    final String indexName;
    @NotNull
    final Double quantity;
    @NotNull
    final String quantityUnit;
    @Nullable
    final String notes;

    IngredientSpec(@NotNull String indexName, @NotNull Double quantity, @NotNull String quantityUnit, @Nullable String notes) {
        this.indexName = indexName;
        this.quantity = quantity;
        this.quantityUnit = quantityUnit;
        this.notes = notes;
    }

    @Override
    public String toString() {
        return String.format("{ indexName: %s, quantity: %.2f, quantityUnit: %s, notes: %s }",
                indexName, quantity, quantityUnit, notes);
    }
}
