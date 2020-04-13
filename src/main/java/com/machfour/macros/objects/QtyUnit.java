package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */
public class QtyUnit extends MacrosEntity<QtyUnit> implements Measurement<QtyUnit>, Unit {

    public static final QtyUnit GRAMS;
    public static final QtyUnit MILLILITRES;
    public static final QtyUnit MILLIGRAMS;
    public static final List<QtyUnit> INBUILT;

    private static final Map<String, QtyUnit> ABBREVIATION_MAP;
    private static final Map<Long, QtyUnit> ID_MAP;

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
        ABBREVIATION_MAP = new HashMap<>(INBUILT.size(), 1.0f);
        ID_MAP = new HashMap<>(INBUILT.size(), 1.0f);
        for (QtyUnit u : INBUILT) {
            ABBREVIATION_MAP.put(u.abbr().toLowerCase(), u);
            ID_MAP.put(u.getId(), u);
        }
    }

    /*
     * Case insensitive matching of abbreviation
     */
    public static QtyUnit fromAbbreviation(String abbreviation, boolean throwIfNotFound) {
        String abbr = abbreviation.toLowerCase();
        if (ABBREVIATION_MAP.containsKey(abbr)) {
            return ABBREVIATION_MAP.get(abbr);
        } else if (throwIfNotFound) {
            throw new IllegalArgumentException("No EnergyUnit exists with abbreviation '" + abbreviation + "'");
        } else {
            return null;
        }
    }

    private static QtyUnit fromId(long id, boolean throwIfNotFound) {
        if (ID_MAP.containsKey(id)) {
            return ID_MAP.get(id);
        } else if (throwIfNotFound) {
            throw new IllegalArgumentException("No QtyUnit exists with ID '" + id + "'");
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name(), abbr());
    }

    @Nullable
    public static  QtyUnit fromAbbreviationNoThrow(String abbreviation) {
        return fromAbbreviation(abbreviation, false);
    }

    @NotNull
    public static QtyUnit fromAbbreviation(String abbreviation) {
        return fromAbbreviation(abbreviation, true);
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
    @Override
    public String name() {
        return getData(Schema.QtyUnitTable.NAME);
    }

    @NotNull
    @Override
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
