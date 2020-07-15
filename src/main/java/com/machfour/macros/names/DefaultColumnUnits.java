package com.machfour.macros.names;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.*;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.machfour.macros.core.Schema.NutritionDataTable.CALCIUM;
import static com.machfour.macros.core.Schema.NutritionDataTable.CALORIES;
import static com.machfour.macros.core.Schema.NutritionDataTable.IRON;
import static com.machfour.macros.core.Schema.NutritionDataTable.KILOJOULES;
import static com.machfour.macros.core.Schema.NutritionDataTable.OMEGA_3_FAT;
import static com.machfour.macros.core.Schema.NutritionDataTable.OMEGA_6_FAT;
import static com.machfour.macros.core.Schema.NutritionDataTable.POTASSIUM;
import static com.machfour.macros.core.Schema.NutritionDataTable.SODIUM;

public class DefaultColumnUnits implements ColumnUnits {
    private static final Set<Column<NutritionData, Double>> MILLIGRAMS_COLS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    SODIUM
                    , CALCIUM
                    , POTASSIUM
                    , IRON
                    , OMEGA_3_FAT
                    , OMEGA_6_FAT
            )));

    private static final Map<Column<NutritionData, Double>, Unit> UNIT_MAP;
    static {
        Map<Column<NutritionData, Double>, Unit> unitMap
                = new HashMap<>(NutritionData.NUTRIENT_COLUMNS.size(), 1.0f);
        // add all the columns and units
        for (Column<NutritionData, Double> col : NutritionData.NUTRIENT_COLUMNS) {
            if (MILLIGRAMS_COLS.contains(col)) {
                unitMap.put(col, QtyUnits.MILLIGRAMS);
            } else if (col.equals(CALORIES)) {
                unitMap.put(col, EnergyUnit.CALORIES);
            } else if (col.equals(KILOJOULES)) {
                unitMap.put(col, EnergyUnit.KILOJOULES);
            } else {
                unitMap.put(col, QtyUnits.GRAMS);
            }
        }
        UNIT_MAP = Collections.unmodifiableMap(unitMap);
    }

    /*
     * Singleton pattern
     */
    private DefaultColumnUnits() {}
    private static DefaultColumnUnits INSTANCE;
    public static DefaultColumnUnits getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultColumnUnits();
        }
        return INSTANCE;
    }

    @NotNull
    @Override
    public Unit getUnit(@NotNull Column<NutritionData, Double> col) {
        if (UNIT_MAP.containsKey(col)) {
            Unit u = UNIT_MAP.get(col);
            assert u != null;
            return u;
        } else {
            throw new IllegalArgumentException("No such nutrient column: " + col.getSqlName());
        }
    }
    @NotNull
    @Override
    public Collection<Column<NutritionData, Double>> columnsWithUnits() {
        return Collections.unmodifiableCollection(UNIT_MAP.keySet());
    }
}
