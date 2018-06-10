package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

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
        milsData.put(Schema.QuantityUnitTable.NAME, "millilitres");
        milsData.put(Schema.QuantityUnitTable.ABBREVIATION, "ml");
        milsData.put(Schema.QuantityUnitTable.METRIC_EQUIVALENT, 1.0);
        milsData.put(Schema.QuantityUnitTable.IS_VOLUME_UNIT, true);
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

    public static QuantityUnit fromAbbreviation(String abbreviation, boolean allowNull) {
        QuantityUnit found = null;
        for (QuantityUnit q : INBUILT) {
            if (q.getAbbreviation().equals(abbreviation)) {
                found = q;
                break;
            }
        }
        if (found == null && !allowNull) {
            throw new IllegalArgumentException("No QuantityUnit exists with abbreviation '" + abbreviation + "'");
        }
        return found;
    }

    public static QuantityUnit fromAbbreviation(String abbreviation) {
        return fromAbbreviation(abbreviation, false);
    }

    private QuantityUnit(ColumnData<QuantityUnit> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<QuantityUnit> getTable() {
        return Schema.QuantityUnitTable.instance();
    }

    public static Factory<QuantityUnit> factory() {
        return QuantityUnit::new;
    }
    @Override
    public Factory<QuantityUnit> getFactory() {
        return factory();
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
