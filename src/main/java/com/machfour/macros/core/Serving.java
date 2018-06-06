package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Schema;
import com.sun.istack.internal.NotNull;

public class Serving extends MacrosEntity<Serving> {

    private Food food;
    private QuantityUnit quantityUnit;

    public Serving(ColumnData<Serving> data, ObjectSource objectSource) {
        super(data, objectSource);
        food = null;
        quantityUnit = null;
    }

    @Override
    public Table<Serving> getTable() {
        return Schema.ServingTable.instance();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Serving && super.equals(o);
    }

    public Food getFood() {
        return food;
    }

    public void setFood(@NotNull Food f) {
        assert food == null && foreignKeyMatches(this, Schema.ServingTable.FOOD_ID, f);
        food = f;
    }

    @NotNull
    public String getQuantityUnitAbbr() {
        return getData(Schema.ServingTable.QUANTITY_UNIT);
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(@NotNull QuantityUnit q) {
        assert getQuantityUnit() == null && foreignKeyMatches(this, Schema.ServingTable.QUANTITY_UNIT, q);
        quantityUnit = q;
    }

    @NotNull
    public Long getFoodId() {
        return getData(Schema.ServingTable.FOOD_ID);
    }

    @NotNull
    public String getName() {
        return getData(Schema.ServingTable.NAME);
    }

    @NotNull
    public Double getQuantity() {
        return getData(Schema.ServingTable.QUANTITY);
    }

    @NotNull
    public Boolean isDefault() {
        return getData(Schema.ServingTable.IS_DEFAULT);
    }
}
