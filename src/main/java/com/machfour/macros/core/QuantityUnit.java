package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Columns;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;
import com.sun.istack.internal.NotNull;

import static com.machfour.macros.data.Columns.QuantityUnitCol.*;

public class QuantityUnit extends MacrosEntity<QuantityUnit> {

    public static final QuantityUnit GRAMS;
    public static final QuantityUnit MILLILITRES;
    public static final QuantityUnit MILLIGRAMS;

    static {
        ColumnData<QuantityUnit> gramsData = new ColumnData<>(Tables.QuantityUnitTable.instance());
        gramsData.putData(Columns.QuantityUnitCol.ID, 1L);
        gramsData.putData(NAME, "grams");
        gramsData.putData(ABBREVIATION, "g");
        gramsData.putData(METRIC_EQUIVALENT, 1.0);
        gramsData.putData(IS_VOLUME_UNIT, false);
        GRAMS = new QuantityUnit(gramsData, true);

        ColumnData<QuantityUnit> milsData = new ColumnData<>(Tables.QuantityUnitTable.instance());
        milsData.putData(Columns.QuantityUnitCol.ID, 2L);
        gramsData.putData(NAME, "millilitres");
        gramsData.putData(ABBREVIATION, "ml");
        gramsData.putData(METRIC_EQUIVALENT, 1.0);
        gramsData.putData(IS_VOLUME_UNIT, true);
        MILLILITRES = new QuantityUnit(milsData, true);

        ColumnData<QuantityUnit> mgData = new ColumnData<>(Tables.QuantityUnitTable.instance());
        mgData.putData(Columns.QuantityUnitCol.ID, 3L);
        mgData.putData(NAME, "milligrams");
        mgData.putData(ABBREVIATION, "mg");
        mgData.putData(METRIC_EQUIVALENT, 0.001);
        mgData.putData(IS_VOLUME_UNIT, false);
        MILLIGRAMS = new QuantityUnit(gramsData, true);
    }


    public QuantityUnit(ColumnData<QuantityUnit> data, boolean isFromDb) {
        super(data, isFromDb);
    }

    @Override
    public Table<QuantityUnit> getTable() {
        return Tables.QuantityUnitTable.instance();
    }

    @NotNull
    public String getName() {
        return getTypedDataForColumn(Columns.QuantityUnitCol.NAME);
    }

    @NotNull
    public String getAbbreviation() {
        return getTypedDataForColumn(Columns.QuantityUnitCol.ABBREVIATION);
    }

    @NotNull
    public Double metricEquivalent() {
        return getTypedDataForColumn(Columns.QuantityUnitCol.METRIC_EQUIVALENT);
    }

    @NotNull
    public Boolean isVolumeUnit() {
        return getTypedDataForColumn(Columns.QuantityUnitCol.IS_VOLUME_UNIT);
    }
}