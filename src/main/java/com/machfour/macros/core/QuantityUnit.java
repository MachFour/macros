package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Schema;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.Arrays;
import java.util.List;

public class QuantityUnit extends MacrosEntity<QuantityUnit> {

    public static final QuantityUnit GRAMS;
    public static final QuantityUnit MILLILITRES;
    public static final QuantityUnit MILLIGRAMS;
    public static final List<QuantityUnit> INBUILT;

    static {
        ColumnData<QuantityUnit> gramsData = new ColumnData<>(Schema.QuantityUnitTable.instance());
        gramsData.putData(Schema.QuantityUnitTable.ID, 1L);
        gramsData.putData(Schema.QuantityUnitTable.NAME, "grams");
        gramsData.putData(Schema.QuantityUnitTable.ABBREVIATION, "g");
        gramsData.putData(Schema.QuantityUnitTable.METRIC_EQUIVALENT, 1.0);
        gramsData.putData(Schema.QuantityUnitTable.IS_VOLUME_UNIT, false);
        GRAMS = new QuantityUnit(gramsData, ObjectSource.DATABASE);

        ColumnData<QuantityUnit> milsData = new ColumnData<>(Schema.QuantityUnitTable.instance());
        milsData.putData(Schema.QuantityUnitTable.ID, 2L);
        gramsData.putData(Schema.QuantityUnitTable.NAME, "millilitres");
        gramsData.putData(Schema.QuantityUnitTable.ABBREVIATION, "ml");
        gramsData.putData(Schema.QuantityUnitTable.METRIC_EQUIVALENT, 1.0);
        gramsData.putData(Schema.QuantityUnitTable.IS_VOLUME_UNIT, true);
        MILLILITRES = new QuantityUnit(milsData, ObjectSource.DATABASE);

        ColumnData<QuantityUnit> mgData = new ColumnData<>(Schema.QuantityUnitTable.instance());
        mgData.putData(Schema.QuantityUnitTable.ID, 3L);
        mgData.putData(Schema.QuantityUnitTable.NAME, "milligrams");
        mgData.putData(Schema.QuantityUnitTable.ABBREVIATION, "mg");
        mgData.putData(Schema.QuantityUnitTable.METRIC_EQUIVALENT, 0.001);
        mgData.putData(Schema.QuantityUnitTable.IS_VOLUME_UNIT, false);
        MILLIGRAMS = new QuantityUnit(gramsData, ObjectSource.DATABASE);

        INBUILT = Arrays.asList(GRAMS, MILLIGRAMS, MILLILITRES);
    }

    @Nullable
    public static QuantityUnit getInbuiltByAbbreviation(String abbeviation) {
        QuantityUnit found = null;
        for (QuantityUnit q : INBUILT) {
            if (q.getAbbreviation().equals(abbeviation)) {
                found = q;
                break;
            }
        }
        return found;
    }

    public QuantityUnit(ColumnData<QuantityUnit> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<QuantityUnit> getTable() {
        return Schema.QuantityUnitTable.instance();
    }

    @NotNull
    public String getName() {
        return getTypedDataForColumn(Schema.QuantityUnitTable.NAME);
    }

    @NotNull
    public String getAbbreviation() {
        return getTypedDataForColumn(Schema.QuantityUnitTable.ABBREVIATION);
    }

    @NotNull
    public Double metricEquivalent() {
        return getTypedDataForColumn(Schema.QuantityUnitTable.METRIC_EQUIVALENT);
    }

    @NotNull
    public Boolean isVolumeUnit() {
        return getTypedDataForColumn(Schema.QuantityUnitTable.IS_VOLUME_UNIT);
    }
}
