package com.machfour.macros.objects;

import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Class of inbuilt quantity units
// These definitions need to be outside the QtyUnit class itself,
// to avoid static initialisation problems between the QtyUnit and QtyUnitTable classes
public class QtyUnits {
    public static final QtyUnit GRAMS;
    public static final QtyUnit MILLILITRES;
    public static final QtyUnit MILLIGRAMS;
    public static final List<QtyUnit> INBUILT;
    private static final Map<String, QtyUnit> ABBREVIATION_MAP;
    private static final Map<Long, QtyUnit> ID_MAP;

    static {
        {
            ColumnData<QtyUnit> gramsData = new ColumnData<>(QtyUnit.table());
            gramsData.put(Schema.QtyUnitTable.ID, 1L);
            gramsData.put(Schema.QtyUnitTable.NAME, "grams");
            gramsData.put(Schema.QtyUnitTable.ABBREVIATION, "g");
            gramsData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 1.0);
            gramsData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, false);
            GRAMS = QtyUnit.factory().construct(gramsData, ObjectSource.INBUILT);
        }
        {
            ColumnData<QtyUnit> milsData = new ColumnData<>(QtyUnit.table());
            milsData.put(Schema.QtyUnitTable.ID, 2L);
            milsData.put(Schema.QtyUnitTable.NAME, "millilitres");
            milsData.put(Schema.QtyUnitTable.ABBREVIATION, "ml");
            milsData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 1.0);
            milsData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, true);
            MILLILITRES = QtyUnit.factory().construct(milsData, ObjectSource.INBUILT);
        }
        {
            ColumnData<QtyUnit> mgData = new ColumnData<>(QtyUnit.table());
            mgData.put(Schema.QtyUnitTable.ID, 3L);
            mgData.put(Schema.QtyUnitTable.NAME, "milligrams");
            mgData.put(Schema.QtyUnitTable.ABBREVIATION, "mg");
            mgData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 0.001);
            mgData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, false);
            MILLIGRAMS = QtyUnit.factory().construct(mgData, ObjectSource.INBUILT);
        }

        INBUILT = Arrays.asList(QtyUnits.GRAMS, QtyUnits.MILLIGRAMS, QtyUnits.MILLILITRES);

        ABBREVIATION_MAP = new HashMap<>(QtyUnits.INBUILT.size(), 1.0f);
        ID_MAP = new HashMap<>(QtyUnits.INBUILT.size(), 1.0f);

        for (QtyUnit u : QtyUnits.INBUILT) {
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
            throw new IllegalArgumentException("No QtyUnit exists with abbreviation '" + abbreviation + "'");
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
}
