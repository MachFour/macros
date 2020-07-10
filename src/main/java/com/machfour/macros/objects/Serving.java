package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;

public class Serving extends MacrosEntityImpl<Serving> implements PortionMeasurement {

    private Food food;
    private QtyUnit qtyUnit;

    protected Serving(ColumnData<Serving> data, ObjectSource objectSource) {
        super(data, objectSource);
        food = null;
        qtyUnit = null;
    }
    @Override
    public Table<Serving> getTable() {
        return table();
    }
    public static Table<Serving> table() {
        return Schema.ServingTable.instance();
    }
    @Override
    public Factory<Serving> getFactory() {
        return factory();
    }
    public static Factory<Serving> factory() {
        return Serving::new;
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
    public String getQtyUnitAbbr() {
        return getData(Schema.ServingTable.QUANTITY_UNIT);
    }

    public QtyUnit getQtyUnit() {
        return qtyUnit;
    }

    public void setQtyUnit(@NotNull QtyUnit q) {
        assert getQtyUnit() == null && foreignKeyMatches(this, Schema.ServingTable.QUANTITY_UNIT, q);
        qtyUnit = q;
    }

    public long getFoodId() {
        return getData(Schema.ServingTable.FOOD_ID);
    }

    @NotNull
    @Override
    public String getName() {
        return getData(Schema.ServingTable.NAME);
    }

    public double getQuantity() {
        return getData(Schema.ServingTable.QUANTITY);
    }

    public boolean isDefault() {
        return getData(Schema.ServingTable.IS_DEFAULT);
    }

    // Measurement functions
    @Override
    public double unitMultiplier() {
        return getQuantity();
    }

    @Override
    public QtyUnit baseUnit() {
        return getQtyUnit();
    }

    @Override
    public boolean isVolumeMeasurement() {
        return getQtyUnit().isVolumeUnit();
    }

    @Override
    public boolean isServing() {
        return true;
    }
}
