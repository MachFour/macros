package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Columns;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;
import com.sun.istack.internal.NotNull;

public class Serving extends MacrosEntity<Serving> {

    private Food food;
    private QuantityUnit quantityUnit;

    public Serving(ColumnData<Serving> data, boolean isFromDb) {
        super(data, isFromDb);
        food = null;
        quantityUnit = null;
    }

    @Override
    public Table<Serving> getTable() {
        return Tables.ServingTable.getInstance();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Serving && super.equals(o);
    }

    public Food getFood() {
        return food;
    }

    public void setFood(@NotNull Food f) {
        assert (food == null);
        assert (getFoodId().equals(f.getId()));
        food = f;
    }

    @NotNull
    public Long getQuantityUnitId() {
        return getTypedDataForColumn(Columns.Serving.QUANTITY_UNIT);
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(@NotNull QuantityUnit q) {
        assert (getQuantityUnit() == null);
        assert (getQuantityUnitId().equals(q.getId()));
        quantityUnit = q;
    }

    @NotNull
    public Long getFoodId() {
        return getTypedDataForColumn(Columns.Serving.FOOD_ID);
    }

    @NotNull
    public String getName() {
        return getTypedDataForColumn(Columns.Serving.NAME);
    }

    @NotNull
    public Double getQuantity() {
        return getTypedDataForColumn(Columns.Serving.QUANTITY);
    }

    @NotNull
    public Boolean isDefault() {
        return getTypedDataForColumn(Columns.Serving.IS_DEFAULT);
    }
}
