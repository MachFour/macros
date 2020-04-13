package com.machfour.macros.names;

/*
 * Interface to provide user-readable names (strings) for nutrition data columns
 */

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.NutritionData;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ColumnNamer {
    @NotNull
    String getNutrientName(Column<NutritionData, Double> col);

    @NotNull
    String getName(Column<?, ?> col);
}
