package com.machfour.macros.names;

public class DefaultNutritionDataStrings extends NutritionDataStringsImpl {
    private static UnitNamer makeUnitNamer() {
        return EnglishUnitNames.getInstance();
    }
    private static ColumnNamer makeColumnNamer() {
        return EnglishColumnNames.getInstance();
    }

    private static ColumnUnits makeColumnUnits() {
        return DefaultColumnUnits.getInstance();
    }

    /*
     * Singleton pattern
     */
    private DefaultNutritionDataStrings() {
        super(makeColumnNamer(), makeColumnUnits(), makeUnitNamer());
    }

    private static DefaultNutritionDataStrings INSTANCE;
    public static DefaultNutritionDataStrings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultNutritionDataStrings();
        }
        return INSTANCE;
    }

}
