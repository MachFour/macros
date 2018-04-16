package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Columns;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;
import com.sun.istack.internal.NotNull;

import static com.machfour.macros.data.Columns.QuantityUnit.*;

public class QuantityUnit extends MacrosEntity<QuantityUnit> {

    public static final QuantityUnit GRAMS;
    public static final QuantityUnit MILLILITRES;
    public static final QuantityUnit MILLIGRAMS;

    static {
        ColumnData<QuantityUnit> gramsData = new ColumnData<>(Tables.QuantityUnitTable.getInstance());
        gramsData.putData(Columns.Base.ID, 1L);
        gramsData.putData(NAME, "grams");
        gramsData.putData(ABBREVIATION, "g");
        gramsData.putData(METRIC_EQUIVALENT, 1.0);
        gramsData.putData(IS_VOLUME_UNIT, false);
        GRAMS = new QuantityUnit(gramsData, true);

        ColumnData<QuantityUnit> milsData = new ColumnData<>(Tables.QuantityUnitTable.getInstance());
        milsData.putData(Columns.Base.ID, 2L);
        gramsData.putData(NAME, "millilitres");
        gramsData.putData(ABBREVIATION, "ml");
        gramsData.putData(METRIC_EQUIVALENT, 1.0);
        gramsData.putData(IS_VOLUME_UNIT, true);
        MILLILITRES = new QuantityUnit(milsData, true);

        ColumnData<QuantityUnit> mgData = new ColumnData<>(Tables.QuantityUnitTable.getInstance());
        mgData.putData(Columns.Base.ID, 3L);
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
        return Tables.QuantityUnitTable.getInstance();
    }

    @NotNull
    public String getName() {
        return getTypedDataForColumn(Columns.QuantityUnit.NAME);
    }

    @NotNull
    public String getAbbreviation() {
        return getTypedDataForColumn(Columns.QuantityUnit.ABBREVIATION);
    }

    @NotNull
    public Double metricEquivalent() {
        return getTypedDataForColumn(Columns.QuantityUnit.METRIC_EQUIVALENT);
    }

    @NotNull
    public Boolean isVolumeUnit() {
        return getTypedDataForColumn(Columns.QuantityUnit.IS_VOLUME_UNIT);
    }
}
