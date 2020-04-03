package com.machfour.macros.names;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.NutritionData;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;

public class EnglishColumnNames implements ColumnNamer {
    // shortest name for each nutrient printed in default mode
    public static final Map<Column<NutritionData, Double>, String> briefNames;
    // longer name for nutrients printed in verbose mode
    public static final Map<Column<NutritionData, Double>, String> longerNames;
    // full name for each nutrient
    public static final Map<Column<NutritionData, Double>, String> prettyNames;

    static {
        Map<Column<NutritionData, Double>, String> _briefNames = new HashMap<>();
        _briefNames.put(CALORIES, "Cals");
        _briefNames.put(PROTEIN, "Prot");
        _briefNames.put(FAT, "Fat");
        _briefNames.put(CARBOHYDRATE, "Carb");
        _briefNames.put(QUANTITY, "Qty");
        briefNames = Collections.unmodifiableMap(_briefNames);

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
        longerNames = Collections.unmodifiableMap(_longerNames);

        Map<Column<NutritionData, Double>, String> _prettyNames = new HashMap<>();
        _prettyNames.put(KILOJOULES, "Kilojoules");
        _prettyNames.put(CALORIES, "Calories");
        _prettyNames.put(PROTEIN, "Protein");
        _prettyNames.put(FAT, "Fat");
        _prettyNames.put(SATURATED_FAT, "Saturated Fat");
        _prettyNames.put(CARBOHYDRATE, "Carbohydrate");
        _prettyNames.put(SUGAR, "Sugars");
        _prettyNames.put(FIBRE, "Fibre");
        _prettyNames.put(SODIUM, "Sodium");
        _prettyNames.put(CALCIUM, "Calcium");
        _prettyNames.put(QUANTITY, "Quantity");
        prettyNames = Collections.unmodifiableMap(_prettyNames);
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
    public String getName(Column<NutritionData, Double> col) {
        if (prettyNames.containsKey(col)) {
            String name = prettyNames.get(col);
            assert name != null;
            return name;
        } else {
            throw new IllegalArgumentException("No such nutrient column: " + col.sqlName());
        }
    }

    @NotNull
    @Override
    public Collection<Column<NutritionData, Double>> availableColumns() {
        return Collections.unmodifiableCollection(prettyNames.keySet());
    }
}
