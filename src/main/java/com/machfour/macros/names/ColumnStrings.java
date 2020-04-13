package com.machfour.macros.names;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Unit;

import org.jetbrains.annotations.NotNull;

/*
 * Interface to provide dynamic names and units (and hence strings) for Nutrition data columns
 * Combines interfaces ColumnNamer, ColumnUnits, UnitNames
 */
public interface ColumnStrings extends ColumnNamer, UnitNamer, ColumnUnits {
    @NotNull
    Unit getUnit(@NotNull Column<NutritionData, Double> col);

    @NotNull
    String getNutrientName(Column<NutritionData, Double> col);

    @NotNull
    String getName(Column<?, ?> col);

    @NotNull
    String getUnitName(Column<NutritionData, Double> col);

    @NotNull
    String getUnitAbbr(Column<NutritionData, Double> col);
}
