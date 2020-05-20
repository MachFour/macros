package com.machfour.macros.names;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.Schema.FoodTable;
import com.machfour.macros.core.Schema.NutritionDataTable;
import com.machfour.macros.objects.NutritionData;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;

public class EnglishColumnNames implements ColumnNamer {
    // shortest name for each nutrient printed in default mode
    public static final Map<Column<NutritionData, Double>, String> briefNutrientNames;
    // longer name for nutrients printed in verbose mode
    public static final Map<Column<NutritionData, Double>, String> longerNutrientNames;
    // full name for each nutrient
    private static final Map<Column<NutritionData, Double>, String> prettyNutrientNames;

    private static final Map<Column<?, ?>, String> columnNames;

    static {
        Map<Column<NutritionData, Double>, String> _briefNames = new HashMap<>();
        _briefNames.put(CALORIES, "Cals");
        _briefNames.put(PROTEIN, "Prot");
        _briefNames.put(FAT, "Fat");
        _briefNames.put(CARBOHYDRATE, "Carb");
        _briefNames.put(QUANTITY, "Qty");
        briefNutrientNames = Collections.unmodifiableMap(_briefNames);
    }

    static {
        Map<Column<NutritionData, Double>, String> _longerNames = new HashMap<>();
        _longerNames.put(CALORIES, "Cals");
        _longerNames.put(PROTEIN, "Protæn");
        _longerNames.put(FAT, "Fat");
        _longerNames.put(SATURATED_FAT, "SatFat");
        _longerNames.put(CARBOHYDRATE, "Carbs");
        _longerNames.put(SUGAR, "Sugar");
        _longerNames.put(FIBRE, "Fibre");
        _longerNames.put(SODIUM, "Sodium");
        _longerNames.put(CALCIUM, "Ca");
        _longerNames.put(QUANTITY, "Qty");
        longerNutrientNames = Collections.unmodifiableMap(_longerNames);
    }

    static {
        prettyNutrientNames = new HashMap<>();
        prettyNutrientNames.put(KILOJOULES, "Kilojoules");
        prettyNutrientNames.put(CALORIES, "Calories");
        prettyNutrientNames.put(PROTEIN, "Protein");
        prettyNutrientNames.put(FAT, "Fat");
        prettyNutrientNames.put(SATURATED_FAT, "Saturated Fat");
        prettyNutrientNames.put(CARBOHYDRATE, "Carbohydrate");
        prettyNutrientNames.put(SUGAR, "Sugars");
        prettyNutrientNames.put(FIBRE, "Fibre");
        prettyNutrientNames.put(SODIUM, "Sodium");
        prettyNutrientNames.put(CALCIUM, "Calcium");
        prettyNutrientNames.put(QUANTITY, "Quantity");
        prettyNutrientNames.put(IRON, "Iron");
        prettyNutrientNames.put(STARCH, "Starch");
        prettyNutrientNames.put(CARBOHYDRATE_BY_DIFF, "Carbohydrate by difference");
        prettyNutrientNames.put(OMEGA_3_FAT, "Omega 3");
        prettyNutrientNames.put(OMEGA_6_FAT, "Omega 6");
        prettyNutrientNames.put(MONOUNSATURATED_FAT, "Monounsaturated Fat");
        prettyNutrientNames.put(POLYUNSATURATED_FAT, "Polyunsaturated Fat");
        prettyNutrientNames.put(SUGAR_ALCOHOL, "Sugar Alcohol");
        prettyNutrientNames.put(WATER, "Water");
        prettyNutrientNames.put(POTASSIUM, "Potassium");
        prettyNutrientNames.put(SALT, "Salt");
        prettyNutrientNames.put(ALCOHOL, "Alcohol");
    }

    static {
        columnNames = new HashMap<>();
        // Food Table
        columnNames.put(FoodTable.ID, "ID");
        columnNames.put(FoodTable.CREATE_TIME, "Creation time");
        columnNames.put(FoodTable.MODIFY_TIME, "Last modified");
        columnNames.put(FoodTable.INDEX_NAME, "Index name");
        columnNames.put(FoodTable.BRAND, "Brand");
        columnNames.put(FoodTable.VARIETY, "Variety");
        columnNames.put(FoodTable.VARIETY_AFTER_NAME, "Variety after name");
        columnNames.put(FoodTable.NAME, "Name");
        columnNames.put(FoodTable.NOTES, "Notes");
        columnNames.put(FoodTable.FOOD_TYPE, "Food Type");
        columnNames.put(FoodTable.USDA_INDEX, "USDA DB index");
        columnNames.put(FoodTable.NUTTAB_INDEX, "NUTTAB DB index");
        columnNames.put(FoodTable.CATEGORY, "Category");

        // NutritionData Table
        columnNames.put(NutritionDataTable.ID, "ID");
        columnNames.put(NutritionDataTable.CREATE_TIME, "Creation time");
        columnNames.put(NutritionDataTable.MODIFY_TIME, "Last modified");
        columnNames.put(NutritionDataTable.DATA_SOURCE, "Data source");
        columnNames.put(NutritionDataTable.DENSITY, "Density");
        columnNames.put(NutritionDataTable.FOOD_ID, "Food ID");
        columnNames.put(NutritionDataTable.QUANTITY_UNIT, "Quantity Unit");
        columnNames.putAll(prettyNutrientNames);
    }

    //Singleton pattern
    private EnglishColumnNames() {}
    private static EnglishColumnNames INSTANCE;
    public static EnglishColumnNames getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EnglishColumnNames();
        }
        return INSTANCE;
    }

    @NotNull
    @Override
    public String getName(Column<?, ?> col) {
        // TODO put ALLLLLLLLLLL columns in here
        String name;
        if (columnNames.containsKey(col)) {
            name = columnNames.get(col);
        } else if (prettyNutrientNames.containsKey(col)) {
            name = prettyNutrientNames.get(col);
        } else {
            throw new UnsupportedOperationException("Name for " + col + " not yet added, sorry!");
        }
        assert name != null;
        return name;

    }

    @NotNull
    @Override
    public String getNutrientName(Column<NutritionData, Double> col) {
        if (prettyNutrientNames.containsKey(col)) {
            String name = prettyNutrientNames.get(col);
            assert name != null;
            return name;
        } else {
            throw new IllegalArgumentException("No such nutrient column: " + col);
        }
    }

}
