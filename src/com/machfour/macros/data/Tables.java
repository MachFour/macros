package com.machfour.macros.data;

import com.machfour.macros.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.data.Columns.*;

public class Tables {
    private Tables() {
    }

    public static abstract class BaseTable<T extends MacrosPersistable> implements Table<T> {
        private final String name;
        private final Map<String, Column<T, ?>> columnsByName;
        private final Column<T, Long> idColumn;
        private final Column<T, Long> createTimeColumn;
        private final Column<T, Long> modifyTimeColumn;

        BaseTable(String tblName, List<Column<T, ?>> cols, Column<T, Long> id, Column<T, Long> createTime, Column<T, Long> modTime) {
            name = tblName;
            columnsByName = makeNameMap(cols);
            idColumn = id;
            createTimeColumn = createTime;
            modifyTimeColumn = modTime;
        }

        @Override
        public Column<T, Long> getIdColumn() {
            return idColumn;
        }

        @Override
        public Column<T, Long> getCreateTimeColumn() {
            return createTimeColumn;
        }

        @Override
        public Column<T, Long> getModifyTimeColumn() {
            return modifyTimeColumn;
        }



        @Override
        public String name() {
            return name;
        }

        @Override
        public List<Column<T, ?>> columns() {
            return new ArrayList<>(columnsByName.values());
        }

        @Override
        public Map<String, Column<T, ?>> columnsByName() {
            return new HashMap<>(columnsByName);
        }

        @Override
        public abstract T construct(ColumnData<T> dataMap, boolean isFromDb);

        @Override
        public Column<T, ?> columnForName(String name) {
            return columnsByName.getOrDefault(name, null);
        }

        private Map<String, Column<T, ?>> makeNameMap(List<Column<T, ?>> cols) {
            Map<String, Column<T, ?>> colsByName = new HashMap<>(cols.size());
            for (Column<T, ?> c : cols) {
                colsByName.put(c.toString(), c);
            }
            return colsByName;
        }
    }

    public final static class QuantityUnitTable extends BaseTable<QuantityUnit> {
        private static final String TABLE_NAME = "QuantityUnitTable";
        private static final QuantityUnitTable INSTANCE = new QuantityUnitTable();

        QuantityUnitTable() {
            super(TABLE_NAME, QuantityUnitCol.COLUMNS, QuantityUnitCol.ID, QuantityUnitCol.CREATE_TIME, QuantityUnitCol.MODIFY_TIME);
        }

        public static QuantityUnitTable instance() {
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
            super(TABLE_NAME, FoodCol.COLUMNS, FoodCol.ID, FoodCol.CREATE_TIME, FoodCol.MODIFY_TIME);
        }

        public static FoodTable instance() {
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
            super(TABLE_NAME, ServingCol.COLUMNS, ServingCol.ID, ServingCol.CREATE_TIME, ServingCol.MODIFY_TIME);
        }

        public static ServingTable instance() {
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
            super(TABLE_NAME, FoodPortionCol.COLUMNS, FoodPortionCol.ID, FoodPortionCol.CREATE_TIME, FoodPortionCol.MODIFY_TIME);
        }

        public static FoodPortionTable instance() {
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
            super(TABLE_NAME, MealCol.COLUMNS, MealCol.ID, MealCol.CREATE_TIME, MealCol.MODIFY_TIME);
        }

        public static MealTable instance() {
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
            super(TABLE_NAME, FoodCategoryCol.COLUMNS, FoodCategoryCol.ID, FoodCategoryCol.CREATE_TIME, FoodCategoryCol.MODIFY_TIME);
        }

        public static FoodCategoryTable instance() {
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
            super(TABLE_NAME, MealDescriptionCol.COLUMNS, MealDescriptionCol.ID, MealDescriptionCol.CREATE_TIME, MealDescriptionCol.MODIFY_TIME);
        }

        public static MealDescriptionTable instance() {
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
            super(TABLE_NAME, IngredientCol.COLUMNS, IngredientCol.ID, IngredientCol.CREATE_TIME, IngredientCol.MODIFY_TIME);
        }

        public static IngredientTable instance() {
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
            super(TABLE_NAME, RegularMealCol.COLUMNS, RegularMealCol.ID, RegularMealCol.CREATE_TIME, RegularMealCol.MODIFY_TIME);
        }

        public static RegularMealTable instance() {
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
            super(TABLE_NAME, NutritionDataCol.COLUMNS, NutritionDataCol.ID, NutritionDataCol.CREATE_TIME, NutritionDataCol.MODIFY_TIME);
        }

        public static NutritionDataTable instance() {
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
            super(TABLE_NAME, FoodAttributeCol.COLUMNS, FoodAttributeCol.ID, FoodAttributeCol.CREATE_TIME, FoodAttributeCol.MODIFY_TIME);
        }

        public static FoodAttributeTable instance() {
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
            super(TABLE_NAME, AttributeMapCol.COLUMNS, AttributeMapCol.ID, AttributeMapCol.CREATE_TIME, AttributeMapCol.MODIFY_TIME);
        }

        public static AttributeMapTable instance() {
            return INSTANCE;
        }

        public AttributeMap construct(ColumnData<AttributeMap> dataMap, boolean isFromDb) {
            return new AttributeMap(dataMap, isFromDb);
        }
    }
}
