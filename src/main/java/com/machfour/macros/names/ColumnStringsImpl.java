package com.machfour.macros.names;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Unit;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class ColumnStringsImpl implements ColumnStrings {
    private final ColumnNamer columnNames;
    private final ColumnUnits columnUnits;
    private final UnitNamer unitNames;

    protected ColumnStringsImpl(
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
    public String getNutrientName(@NotNull Column<NutritionData, Double> col) {
        return columnNames.getNutrientName(col);
    }

    @NotNull
    @Override
    public String getName(@NotNull Column<?, ?> col) {
        return columnNames.getName(col);
    }

    @NotNull
    public String getName(@NotNull Unit unit) {
        return unitNames.getName(unit);
    }
    @NotNull
    public String getAbbr(@NotNull Unit unit) {
        return unitNames.getAbbr(unit);
    }


    @NotNull
    @Override
    public String getUnitName(Column<NutritionData, Double> col) {
        return getName(getUnit(col));
    }
    @NotNull
    @Override
    public String getUnitAbbr(Column<NutritionData, Double> col) {
        return getAbbr(getUnit(col));
    }

    @Override
    public @NotNull Collection<Column<NutritionData, Double>> columnsWithUnits() {
        return columnUnits.columnsWithUnits();
    }
    @Override
    public @NotNull Collection<Unit> availableUnits() {
        return unitNames.availableUnits();
    }
}
