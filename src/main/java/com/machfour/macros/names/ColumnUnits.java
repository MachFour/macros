package com.machfour.macros.names;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Unit;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ColumnUnits {
    @NotNull
    Unit getUnit(@NotNull Column<NutritionData, Double> col);

    @NotNull
    Collection<Column<NutritionData, Double>> availableColumns();
}
