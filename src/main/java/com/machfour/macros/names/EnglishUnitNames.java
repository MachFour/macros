package com.machfour.macros.names;

import com.machfour.macros.objects.EnergyUnit;
import com.machfour.macros.objects.QtyUnits;
import com.machfour.macros.objects.Unit;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EnglishUnitNames implements UnitNamer {

    private static final List<Unit> AVAILABLE_UNITS = Collections.unmodifiableList(Arrays.asList(
          QtyUnits.GRAMS
        , QtyUnits.MILLIGRAMS
        , QtyUnits.MILLILITRES
        , EnergyUnit.KILOJOULES
        , EnergyUnit.CALORIES
    ));

    /*
     * Singleton pattern
     */
    private EnglishUnitNames() {}
    private static EnglishUnitNames INSTANCE;
    public static EnglishUnitNames getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EnglishUnitNames();
        }
        return INSTANCE;
    }

    @Override
    @NotNull
    public String getName(@NotNull Unit unit) {
        return unit.name();
    }

    @Override
    @NotNull
    public String getAbbr(@NotNull Unit unit) {
        return unit.abbr();
    }

    @Override
    @NotNull
    public List<Unit> availableUnits() {
        // immutable
        return AVAILABLE_UNITS;
    }
}
