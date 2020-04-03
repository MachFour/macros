package com.machfour.macros.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */
public class EnergyUnit implements Unit {
    public static final EnergyUnit KILOJOULES;
    public static final EnergyUnit CALORIES;

    private static final List<EnergyUnit> INBUILT;

    // maps lower case abbreviations to objects
    private static final Map<String, EnergyUnit> ABBREVIATION_MAP;

    static {
        KILOJOULES = new EnergyUnit("Kilojoules", "kJ");
        CALORIES = new EnergyUnit("Calories", "kcal");

        INBUILT = Collections.unmodifiableList(Arrays.asList(KILOJOULES, CALORIES));

        Map<String, EnergyUnit> abbreviationMap = new HashMap<>(INBUILT.size(), 1.0f);
        for (EnergyUnit eu : INBUILT) {
            abbreviationMap.put(eu.abbr().toLowerCase(), eu);
        }
        ABBREVIATION_MAP = Collections.unmodifiableMap(abbreviationMap);
    }

    // Full name of this Unit
    @NotNull
    private final String name;

    // Abbreviation: must be unique among different Units.
    @NotNull
    private final String abbr;

    private EnergyUnit(@NotNull String name, @NotNull String abbr) {
        this.name = name;
        this.abbr = abbr;
    }

    /*
     * Case insensitive matching of abbreviation
     */
    @Nullable
    public static EnergyUnit fromAbbreviation(@NotNull String abbr, boolean throwIfNotFound) {
        String abbrLower = abbr.toLowerCase();
        if (ABBREVIATION_MAP.containsKey(abbrLower)) {
            return ABBREVIATION_MAP.get(abbrLower);
        } else if (throwIfNotFound) {
            throw new IllegalArgumentException("No EnergyUnit exists with abbreviation '" + abbr + "'");
        } else {
            return null;
        }
    }
    @Override
    @NotNull
    public String toString() {
        return String.format("%s (%s)", name(), abbr());
    }

    @NotNull
    @Override
    public String name() {
        return name;
    }

    @NotNull
    @Override
    public String abbr() {
        return abbr;
    }
}
