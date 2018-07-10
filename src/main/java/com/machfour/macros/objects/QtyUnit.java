package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class QtyUnit extends MacrosEntity<QtyUnit> {

    public static final QtyUnit GRAMS;
    public static final QtyUnit MILLILITRES;
    public static final QtyUnit MILLIGRAMS;
    public static final List<QtyUnit> INBUILT;

    static {
        ColumnData<QtyUnit> gramsData = new ColumnData<>(Schema.QtyUnitTable.instance());
        gramsData.put(Schema.QtyUnitTable.ID, 1L);
        gramsData.put(Schema.QtyUnitTable.NAME, "grams");
        gramsData.put(Schema.QtyUnitTable.ABBREVIATION, "g");
        gramsData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 1.0);
        gramsData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, false);
        GRAMS = new QtyUnit(gramsData, ObjectSource.DATABASE);

        ColumnData<QtyUnit> milsData = new ColumnData<>(Schema.QtyUnitTable.instance());
        milsData.put(Schema.QtyUnitTable.ID, 2L);
        milsData.put(Schema.QtyUnitTable.NAME, "millilitres");
        milsData.put(Schema.QtyUnitTable.ABBREVIATION, "ml");
        milsData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 1.0);
        milsData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, true);
        MILLILITRES = new QtyUnit(milsData, ObjectSource.DATABASE);

        ColumnData<QtyUnit> mgData = new ColumnData<>(Schema.QtyUnitTable.instance());
        mgData.put(Schema.QtyUnitTable.ID, 3L);
        mgData.put(Schema.QtyUnitTable.NAME, "milligrams");
        mgData.put(Schema.QtyUnitTable.ABBREVIATION, "mg");
        mgData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 0.001);
        mgData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, false);
        MILLIGRAMS = new QtyUnit(gramsData, ObjectSource.DATABASE);

        INBUILT = Arrays.asList(GRAMS, MILLIGRAMS, MILLILITRES);
    }

    public static QtyUnit fromAbbreviation(String abbreviation, boolean allowNull) {
        QtyUnit found = null;
        for (QtyUnit q : INBUILT) {
            if (q.getAbbr().equals(abbreviation)) {
                found = q;
                break;
            }
        }
        if (found == null && !allowNull) {
            throw new IllegalArgumentException("No QtyUnit exists with abbreviation '" + abbreviation + "'");
        }
        return found;
    }

    public static QtyUnit fromAbbreviation(String abbreviation) {
        return fromAbbreviation(abbreviation, false);
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
    public String getName() {
        return getData(Schema.QtyUnitTable.NAME);
    }

    @NotNull
    public String getAbbr() {
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
}
