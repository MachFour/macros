package com.machfour.macros.data;

import com.machfour.macros.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tables {
    private Tables() {
    }

    public static abstract class BaseTable<T extends MacrosPersistable<T>> implements Table<T> {
        private final String tableName;
        private final Map<String, Column<?>> columnsByName;

        BaseTable(String tableName, Column... extraCols) {
            this.tableName = tableName;
            columnsByName = makeNameMap(Columns.Base.COLUMNS);
            columnsByName.putAll(makeNameMap(extraCols));
        }

        @Override
        public String name() {
            return tableName;
        }

        @Override
        public List<Column<?>> columns() {
            return new ArrayList<>(columnsByName.values());
        }

        @Override
        public Map<String, Column<?>> columnsByName() {
            return new HashMap<>(columnsByName);
        }

        @Override
        public abstract T construct(ColumnData<T> dataMap, boolean isFromDb);

        @Override
        public Column columnForName(String name) {
            return columnsByName.getOrDefault(name, null);
        }

        private Map<String, Column<?>> makeNameMap(Column... cols) {
            Map<String, Column<?>> colsByName = new HashMap<>(cols.length);
            for (Column<?> c : cols) {
                colsByName.put(c.toString(), c);
            }
            return colsByName;
        }
    }

    public final static class QuantityUnitTable extends BaseTable<QuantityUnit> {
        private static final String TABLE_NAME = "QuantityUnitTable";
        private static final QuantityUnitTable INSTANCE = new QuantityUnitTable();

        QuantityUnitTable() {
            super(TABLE_NAME, Columns.QuantityUnit.COLUMNS);
        }

        public static QuantityUnitTable getInstance() {
            return INSTANCE;
        }

        public QuantityUnit construct(ColumnData<QuantityUnit> dataMap, boolean isFromDb) {
            return new QuantityUnit(dataMap, isFromDb);
        }
    }

    public final static class FoodTable extends BaseTable<Food> {
        private static final String TABLE_NAME = "FoodTable";
        private static final FoodTable INSTANCE = new FoodTable();

        FoodTable() {
            super(TABLE_NAME, Columns.Food.COLUMNS);
        }

        public static FoodTable getInstance() {
            return INSTANCE;
        }

        public Food construct(ColumnData<Food> dataMap, boolean isFromDb) {
            return new Food(dataMap, isFromDb);
        }
    }

    public final static class ServingTable extends BaseTable<Serving> {
        private static final String TABLE_NAME = "ServingTable";
        private static final ServingTable INSTANCE = new ServingTable();

        ServingTable() {
            super(TABLE_NAME, Columns.Serving.COLUMNS);
        }

        public static ServingTable getInstance() {
            return INSTANCE;
        }

        public Serving construct(ColumnData<Serving> dataMap, boolean isFromDb) {
            return new Serving(dataMap, isFromDb);
        }
    }

    public final static class FoodPortionTable extends BaseTable<FoodPortion> {
        private static final String TABLE_NAME = "FoodPortionTable";
        private static final FoodPortionTable INSTANCE = new FoodPortionTable();

        FoodPortionTable() {
            super(TABLE_NAME, Columns.FoodPortion.COLUMNS);
        }

        public static FoodPortionTable getInstance() {
            return INSTANCE;
        }

        public FoodPortion construct(ColumnData<FoodPortion> dataMap, boolean isFromDb) {
            return new FoodPortion(dataMap, isFromDb);
        }
    }

    public final static class MealTable extends BaseTable<Meal> {
        private static final String TABLE_NAME = "MealTable";
        private static final MealTable INSTANCE = new MealTable();

        MealTable() {
            super(TABLE_NAME, Columns.Meal.COLUMNS);
        }

        public static MealTable getInstance() {
            return INSTANCE;
        }

        public Meal construct(ColumnData<Meal> dataMap, boolean isFromDb) {
            return new Meal(dataMap, isFromDb);
        }
    }

    public final static class FoodCategoryTable extends BaseTable<FoodCategory> {
        private static final String TABLE_NAME = "FoodCategoryTable";
        private static final FoodCategoryTable INSTANCE = new FoodCategoryTable();

        FoodCategoryTable() {
            super(TABLE_NAME, Columns.FoodCategory.COLUMNS);
        }

        public static FoodCategoryTable getInstance() {
            return INSTANCE;
        }

        public FoodCategory construct(ColumnData<FoodCategory> dataMap, boolean isFromDb) {
            return new FoodCategory(dataMap, isFromDb);
        }
    }

    public final static class MealDescriptionTable extends BaseTable<MealDescription> {
        private static final String TABLE_NAME = "MealDescriptionTable";
        private static final MealDescriptionTable INSTANCE = new MealDescriptionTable();

        MealDescriptionTable() {
            super(TABLE_NAME, Columns.MealDescription.COLUMNS);
        }

        public static MealDescriptionTable getInstance() {
            return INSTANCE;
        }

        public MealDescription construct(ColumnData<MealDescription> dataMap, boolean isFromDb) {
            return new MealDescription(dataMap, isFromDb);
        }
    }

    public final static class IngredientTable extends BaseTable<Ingredient> {
        private static final String TABLE_NAME = "IngredientTable";
        private static final IngredientTable INSTANCE = new IngredientTable();

        IngredientTable() {
            super(TABLE_NAME, Columns.Ingredient.COLUMNS);
        }

        public static IngredientTable getInstance() {
            return INSTANCE;
        }

        public Ingredient construct(ColumnData<Ingredient> dataMap, boolean isFromDb) {
            return new Ingredient(dataMap, isFromDb);
        }
    }

    public final static class RegularMealTable extends BaseTable<RegularMeal> {
        private static final String TABLE_NAME = "RegularMealTable";
        private static final RegularMealTable INSTANCE = new RegularMealTable();

        RegularMealTable() {
            super(TABLE_NAME, Columns.RegularMeal.COLUMNS);
        }

        public static RegularMealTable getInstance() {
            return INSTANCE;
        }

        public RegularMeal construct(ColumnData<RegularMeal> dataMap, boolean isFromDb) {
            return new RegularMeal(dataMap, isFromDb);
        }
    }

    public final static class NutritionDataTable extends BaseTable<NutritionData> {
        private static final String TABLE_NAME = "NutritionDataTable";
        private static final NutritionDataTable INSTANCE = new NutritionDataTable();

        NutritionDataTable() {
            super(TABLE_NAME, Columns.NutritionData.COLUMNS);
        }

        public static NutritionDataTable getInstance() {
            return INSTANCE;
        }

        public NutritionData construct(ColumnData<NutritionData> dataMap, boolean isFromDb) {
            return new NutritionData(dataMap, isFromDb);
        }
    }

    public final static class FoodAttributeTable extends BaseTable<FoodAttribute> {
        private static final String TABLE_NAME = "FoodAttributeTable";
        private static final FoodAttributeTable INSTANCE = new FoodAttributeTable();

        FoodAttributeTable() {
            super(TABLE_NAME, Columns.FoodAttribute.COLUMNS);
        }

        public static FoodAttributeTable getInstance() {
            return INSTANCE;
        }

        public FoodAttribute construct(ColumnData<FoodAttribute> dataMap, boolean isFromDb) {
            return new FoodAttribute(dataMap, isFromDb);
        }
    }

    public final static class AttributeMapTable extends BaseTable<AttributeMap> {
        private static final String TABLE_NAME = "AttributeMapTable";
        private static final AttributeMapTable INSTANCE = new AttributeMapTable();

        AttributeMapTable() {
            super(TABLE_NAME, Columns.AttributeMap.COLUMNS);
        }

        public static AttributeMapTable getInstance() {
            return INSTANCE;
        }

        public AttributeMap construct(ColumnData<AttributeMap> dataMap, boolean isFromDb) {
            return new AttributeMap(dataMap, isFromDb);
        }
    }
}
