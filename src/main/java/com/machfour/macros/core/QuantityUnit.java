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
        gramsData.put(Schema.QuantityUnitTable.ID, 1L);
        gramsData.put(Schema.QuantityUnitTable.NAME, "grams");
        gramsData.put(Schema.QuantityUnitTable.ABBREVIATION, "g");
        gramsData.put(Schema.QuantityUnitTable.METRIC_EQUIVALENT, 1.0);
        gramsData.put(Schema.QuantityUnitTable.IS_VOLUME_UNIT, false);
        GRAMS = new QuantityUnit(gramsData, ObjectSource.DATABASE);

        ColumnData<QuantityUnit> milsData = new ColumnData<>(Schema.QuantityUnitTable.instance());
        milsData.put(Schema.QuantityUnitTable.ID, 2L);
        gramsData.put(Schema.QuantityUnitTable.NAME, "millilitres");
        gramsData.put(Schema.QuantityUnitTable.ABBREVIATION, "ml");
        gramsData.put(Schema.QuantityUnitTable.METRIC_EQUIVALENT, 1.0);
        gramsData.put(Schema.QuantityUnitTable.IS_VOLUME_UNIT, true);
        MILLILITRES = new QuantityUnit(milsData, ObjectSource.DATABASE);

        ColumnData<QuantityUnit> mgData = new ColumnData<>(Schema.QuantityUnitTable.instance());
        mgData.put(Schema.QuantityUnitTable.ID, 3L);
        mgData.put(Schema.QuantityUnitTable.NAME, "milligrams");
        mgData.put(Schema.QuantityUnitTable.ABBREVIATION, "mg");
        mgData.put(Schema.QuantityUnitTable.METRIC_EQUIVALENT, 0.001);
        mgData.put(Schema.QuantityUnitTable.IS_VOLUME_UNIT, false);
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
        return getData(Schema.QuantityUnitTable.NAME);
    }

    @NotNull
    public String getAbbreviation() {
        return getData(Schema.QuantityUnitTable.ABBREVIATION);
    }

    @NotNull
    public Double metricEquivalent() {
        return getData(Schema.QuantityUnitTable.METRIC_EQUIVALENT);
    }

    @NotNull
    public Boolean isVolumeUnit() {
        return getData(Schema.QuantityUnitTable.IS_VOLUME_UNIT);
    }
}
