package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class QtyUnit extends MacrosEntity<QtyUnit> implements Measurement<QtyUnit> {

    public static final QtyUnit GRAMS;
    public static final QtyUnit MILLILITRES;
    public static final QtyUnit MILLIGRAMS;
    public static final List<QtyUnit> INBUILT;

    static {
        {
            ColumnData<QtyUnit> gramsData = new ColumnData<>(Schema.QtyUnitTable.instance());
            gramsData.put(Schema.QtyUnitTable.ID, 1L);
            gramsData.put(Schema.QtyUnitTable.NAME, "grams");
            gramsData.put(Schema.QtyUnitTable.ABBREVIATION, "g");
            gramsData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 1.0);
            gramsData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, false);
            GRAMS = new QtyUnit(gramsData, ObjectSource.INBUILT);
        }
        {
            ColumnData<QtyUnit> milsData = new ColumnData<>(Schema.QtyUnitTable.instance());
            milsData.put(Schema.QtyUnitTable.ID, 2L);
            milsData.put(Schema.QtyUnitTable.NAME, "millilitres");
            milsData.put(Schema.QtyUnitTable.ABBREVIATION, "ml");
            milsData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 1.0);
            milsData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, true);
            MILLILITRES = new QtyUnit(milsData, ObjectSource.INBUILT);
        }
        {
            ColumnData<QtyUnit> mgData = new ColumnData<>(Schema.QtyUnitTable.instance());
            mgData.put(Schema.QtyUnitTable.ID, 3L);
            mgData.put(Schema.QtyUnitTable.NAME, "milligrams");
            mgData.put(Schema.QtyUnitTable.ABBREVIATION, "mg");
            mgData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 0.001);
            mgData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, false);
            MILLIGRAMS = new QtyUnit(mgData, ObjectSource.INBUILT);
        }
        INBUILT = Arrays.asList(GRAMS, MILLIGRAMS, MILLILITRES);
    }

    private static QtyUnit fromAbbreviation(String abbreviation, boolean throwIfNotFound) {
        QtyUnit found = null;
        for (QtyUnit q : INBUILT) {
            if (q.abbr().equals(abbreviation)) {
                found = q;
                break;
            }
        }
        if (found == null && throwIfNotFound) {
            throw new IllegalArgumentException("No QtyUnit exists with abbreviation '" + abbreviation + "'");
        }
        return found;
    }
    private static QtyUnit fromId(long id, boolean throwIfNotFound) {
        QtyUnit found = null;
        for (QtyUnit q : INBUILT) {
            if (q.getId() == id) {
                found = q;
                break;
            }
        }
        if (found == null && throwIfNotFound) {
            throw new IllegalArgumentException("No QtyUnit exists with ID '" + id + "'");
        }
        return found;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name(), abbr());
    }

    public static @Nullable QtyUnit fromAbbreviation(String abbreviation) {
        return fromAbbreviation(abbreviation, false);
    }

    public static @Nullable QtyUnit fromId(long id) {
        return fromId(id, false);
    }


    private QtyUnit(ColumnData<QtyUnit> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<QtyUnit> getTable() {
        return Schema.QtyUnitTable.instance();
    }

    public static Factory<QtyUnit> factory() {
        return QtyUnit::new;
    }
    @Override
    public Factory<QtyUnit> getFactory() {
        return factory();
    }

    @NotNull
    public String name() {
        return getData(Schema.QtyUnitTable.NAME);
    }

    @NotNull
    public String abbr() {
        return getData(Schema.QtyUnitTable.ABBREVIATION);
    }

    @NotNull
    public Double metricEquivalent() {
        return getData(Schema.QtyUnitTable.METRIC_EQUIVALENT);
    }

    @NotNull
    public Boolean isVolumeUnit() {
        return getData(Schema.QtyUnitTable.IS_VOLUME_UNIT);
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
