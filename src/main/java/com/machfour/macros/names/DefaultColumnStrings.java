package com.machfour.macros.names;

public class DefaultColumnStrings extends ColumnStringsImpl {
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
    private DefaultColumnStrings() {
        super(makeColumnNamer(), makeColumnUnits(), makeUnitNamer());
    }

    private static DefaultColumnStrings INSTANCE;
    public static DefaultColumnStrings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultColumnStrings();
        }
        return INSTANCE;
    }

}
