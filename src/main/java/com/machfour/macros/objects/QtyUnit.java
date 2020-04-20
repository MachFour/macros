package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;

import static com.machfour.macros.core.Schema.QtyUnitTable.*;

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */
public class QtyUnit extends MacrosEntity<QtyUnit> implements Measurement<QtyUnit>, Unit {

    public static Factory<QtyUnit> factory() {
        return QtyUnit::new;
    }

    public static Table<QtyUnit> table() {
        return Schema.QtyUnitTable.instance();
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name(), abbr());
    }


    private QtyUnit(ColumnData<QtyUnit> data, ObjectSource objectSource) {
        super(data, objectSource);
    }


    @Override
    public Table<QtyUnit> getTable() {
        return table();
    }

    @Override
    public Factory<QtyUnit> getFactory() {
        return factory();
    }

    @NotNull
    @Override
    public String name() {
        return getData(NAME);
    }

    @NotNull
    @Override
    public String abbr() {
        return getData(ABBREVIATION);
    }

    @NotNull
    public Double metricEquivalent() {
        return getData(METRIC_EQUIVALENT);
    }

    @NotNull
    public Boolean isVolumeUnit() {
        return getData(IS_VOLUME_UNIT);
    }

    // Measurement functions
    @Override
    public double unitMultiplier() {
        return 1;
    }

    @Override
    public QtyUnit baseUnit() {
        return this;
    }

    @Override
    public boolean isVolumeMeasurement() {
        return isVolumeUnit();
    }

    @Override
    public boolean isServing() {
        return false;
    }
}
