package com.machfour.macros.names;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Unit;

import org.jetbrains.annotations.NotNull;

public abstract class NutritionDataStringsImpl implements NutritionDataStrings {
    private final ColumnNamer columnNames;
    private final ColumnUnits columnUnits;
    private final UnitNamer unitNames;

    protected NutritionDataStringsImpl(
            @NotNull ColumnNamer columnNames,
            @NotNull ColumnUnits columnUnits,
            @NotNull UnitNamer unitNames) {
        this.columnNames = columnNames;
        this.columnUnits = columnUnits;
        this.unitNames = unitNames;
    }

    @NotNull
    @Override
    public Unit getUnit(@NotNull Column<NutritionData, Double> col) {
        return columnUnits.getUnit(col);
    }

    @NotNull
    @Override
    public String getName(Column<NutritionData, Double> col) {
        return columnNames.getName(col);
    }
    @NotNull
    @Override
    public String getUnitName(Column<NutritionData, Double> col) {
        return unitNames.getName(getUnit(col));
    }
    @NotNull
    @Override
    public String getUnitAbbr(Column<NutritionData, Double> col) {
        return unitNames.getAbbr(getUnit(col));
    }
}
