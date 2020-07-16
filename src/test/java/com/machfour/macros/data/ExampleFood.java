package com.machfour.macros.data;

import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Schema;
import com.machfour.macros.objects.*;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;
import static com.machfour.macros.core.Schema.FoodTable.*;

public class ExampleFood {
    private ExampleFood() {}

    /*
     * Food with no nutrition data
     */
    public static final Food food1;
    /*
     * Food with nutrition data that has nonunit density
     */
    public static final Food food2;


    static {
        food1 = init1();
        food2 = init2();
    }

    private static Food init1() {
        ColumnData<Food> data = new ColumnData<>(Schema.FoodTable.instance());
        data.put(INDEX_NAME, "food1");
        data.put(BRAND, "Max's");
        data.put(VARIETY, "really good");
        data.put(NAME, "food");
        data.put(VARIETY_AFTER_NAME, false);
        data.put(NOTES, "notes");
        data.put(CATEGORY, "dairy");
        data.put(FOOD_TYPE, FoodType.PRIMARY.getName());
        data.put(USDA_INDEX, null);
        data.put(NUTTAB_INDEX, null);
        return Food.factory().construct(data, ObjectSource.IMPORT);
    }

    private static Food init2() {
        ColumnData<Food> fData = new ColumnData<>(Food.table());
        ColumnData<NutritionData> nData = new ColumnData<>(NutritionData.table());
        fData.put(INDEX_NAME, "generic-oil");
        fData.put(NAME, "Generic Oil");
        fData.put(CATEGORY, "oils");
        fData.put(FOOD_TYPE, FoodType.PRIMARY.getName());
        nData.put(KILOJOULES, 3400.0);
        nData.put(CARBOHYDRATE, 0.0);
        nData.put(FAT, 92.0);
        nData.put(SATURATED_FAT, 12.0);
        nData.put(SUGAR, 0.0);
        nData.put(SODIUM, 0.0);
        nData.put(POLYUNSATURATED_FAT, 23.0);
        nData.put(MONOUNSATURATED_FAT, 56.0);
        nData.put(WATER, 0.0);
        nData.put(FIBRE, 0.0);
        nData.put(CALCIUM, 34.0);
        nData.put(IRON, 10.0);
        nData.put(DATA_SOURCE, "Test");
        nData.put(DENSITY, 0.92);
        nData.put(QUANTITY, 100.0);
        nData.put(QUANTITY_UNIT, QtyUnits.MILLILITRES.getAbbr());
        NutritionData nd = NutritionData.table().construct(nData, ObjectSource.USER_NEW);
        Food f = Food.factory().construct(fData, ObjectSource.IMPORT);
        f.setNutritionData(nd);
        return f;
    }

}
