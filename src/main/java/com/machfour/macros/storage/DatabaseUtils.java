package com.machfour.macros.storage;

import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.MacrosType;
import com.machfour.macros.core.Table;
import com.machfour.macros.core.Types;
import com.machfour.macros.util.StringJoiner;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DatabaseUtils {
    private DatabaseUtils() {
    }

    private static <M> String joinColumns(Iterable<Column<M, ?>> columns, String suffix) {
        return StringJoiner.of(columns).sep(", ").stringFunc(Column::sqlName).suffix(suffix).join();
    }

    private static <M> String joinColumns(Iterable<Column<M, ?>> columns) {
        return joinColumns(columns, "");
    }

    // usually, an SQL placeholder is just a question mark. But, for example a DateStamp
    // needs to be entered as DATE('date_string'), so that logic is implemented here
    private static String getSqlPlaceholder(MacrosType<?> columnType) {
        String placeholder;
        // for now, DateStamp is the only special one
        if (columnType.equals(Types.DATESTAMP)) {
            // need the space here in order for the binding to work properly
            placeholder = "DATE( ? )";
        } else {
            placeholder = "?";
        }
        return placeholder;
    }

    // creates SQL placeholders for the given (ordered) list of columns
    private static <M> String makeInsertPlaceholders(List<Column<M, ?>> columns) {
        List<String> placeholders = new ArrayList<>(columns.size());
        for (Column<M, ?> c : columns) {
            placeholders.add(getSqlPlaceholder(c.getType()));
        }
        return StringJoiner.of(placeholders).sep(", ").join();
    }

    // creates SQL placeholders for the given (ordered) list of columns
    private static <M> String makeUpdatePlaceholders(List<Column<M, ?>> columns) {
        List<String> placeholders = new ArrayList<>(columns.size());
        for (Column<M, ?> c : columns) {
            placeholders.add(c.sqlName() + " = " + getSqlPlaceholder(c.getType()));
        }
        return StringJoiner.of(placeholders).sep(", ").join();
    }

    // " WHERE column1 = ?"
    // " WHERE column1 IN (?, ?, ?, ...?)"
    // if nkeys is <= 0, no where string will be formed, so all objects will be returned.
    private static String makeWhereString(Column<?, ?> whereColumn, int nValues) {
        assert nValues >= 0;
        if (nValues == 0) {
            return "";
        }
        String colName = whereColumn.sqlName();
        String placeholder = getSqlPlaceholder(whereColumn.getType());

        //if (whereColumn.type().equals(DATESTAMP)) {
        //    sb.append("DATE(").append(whereColumn.sqlName()).append(")");
        //} else {
        //sb.append(whereColumn.sqlName());
        //}
        if (nValues == 1) {
            return String.format(" WHERE %s = %s", colName, placeholder);
        } else {
            String placeholders = StringJoiner.of(placeholder).sep(", ").copies(nValues).join();
            return String.format(" WHERE %s IN (%s)", colName, placeholders);
        }
    }

    public static <M> String deleteTemplate(Table<M> table, Column<M, ?> whereColumn) {
        return deleteTemplate(table) + makeWhereString(whereColumn, 1);

    }
    // delete all!
    public static <M> String deleteTemplate(Table<M> table) {
        return "DELETE FROM " + table.name();
    }

    // " WHERE (likeColumn[0] LIKE likeValue[0]) OR (likeColumn[1] LIKE likeValue[1]) OR ..."
    private static <M> String makeWhereLikeString(List<Column<M, String>> likeColumns) {
        switch (likeColumns.size()) {
            case 0:
                return "";
            case 1:
                return " WHERE " + likeColumns.get(0).sqlName() + " LIKE ?";
            default:
                List<String> bracketedWhereClauses = new ArrayList<>(likeColumns.size());
                for (Column<?, String> c : likeColumns) {
                    bracketedWhereClauses.add("(" + c.sqlName() + " LIKE ?)");
                }
                return " WHERE " + StringJoiner.of(bracketedWhereClauses).sep(" OR ").join();
        }
    }

    public static <M> String selectLikeTemplate(Table<M> t, Column<M, ?> selectColumn, List<Column<M, String>> likeColumns) {
        return selectTemplate(t, toList(selectColumn), makeWhereLikeString(likeColumns), false);
    }

    public static <M> String selectTemplate(Table<M> t, Column<M, ?> selectColumn, Column<M, ?> whereColumn, int nValues, boolean distinct) {
        return selectTemplate(t, toList(selectColumn), whereColumn, nValues, distinct);
    }
    public static <M> String selectTemplate(Table<M> t, Column<M, ?> selectColumn, Column<M, ?> whereColumn, int nValues) {
        return selectTemplate(t, toList(selectColumn), whereColumn, nValues, false);
    }
    public static <M> String selectTemplate(Table<M> t, List<Column<M, ?>> orderedColumns, Column<M, ?> whereColumn, int nValues, boolean distinct) {
        return selectTemplate(t, orderedColumns, makeWhereString(whereColumn, nValues), distinct);
    }
    public static <M> String selectTemplate(Table<M> t, List<Column<M, ?>> orderedColumns, String whereString, boolean distinct) {
        List<String> words = new ArrayList<>(6);
        words.add("SELECT");
        if (distinct) {
            words.add("DISTINCT");
        }
        words.add(joinColumns(orderedColumns));
        words.add("FROM");
        words.add(t.name());
        words.add(whereString);
        return StringJoiner.of(words).sep(" ").join();
    }

    // columns must be a subset of table.columns()
    public static <M> String insertTemplate(Table<M> t, List<Column<M, ?>> orderedColumns) {
        String placeholders = makeInsertPlaceholders(orderedColumns);
        return String.format("INSERT INTO %s (%s) VALUES (%s)", t.name(), joinColumns(orderedColumns), placeholders);
    }

    public static <M, J> String updateTemplate(Table<M> t, List<Column<M, ?>> orderedColumns, Column<M, J> keyCol) {
        String updateColumnPlaceholders = makeUpdatePlaceholders(orderedColumns);
        // TODO dynamic placeholders
        return String.format("UPDATE %s SET %s %s", t.name(), updateColumnPlaceholders, makeWhereString(keyCol, 1));
    }


    public static <J> String[] makeBindableStrings(Collection<J> objects, MacrosType<J> type) {
        String[] array = new String[objects.size()];
        int index = 0;
        for (J o : objects) {
            array[index++] = type.toSqlString(o);
        }
        return array;
    }

    // for use with Android SQLite implementation
    // The Object array is only allowed to contain String, Long, Double, byte[] and null
    public static <M> Object[] makeBindableObjects(ColumnData<M> data, List<Column<M, ?>> orderedColumns) {
        Object[] array = new String[orderedColumns.size()];
        int index = 0;
        for (Column<M, ?> col : orderedColumns) {
            array[index++] = data.getAsRaw(col);
        }
        return array;
    }

    static <M extends MacrosPersistable> Map<Long, M> makeIdMap(Collection<M> objects) {
        Map<Long, M> idMap = new HashMap<>(objects.size(), 1);
        for (M m : objects) {
            assert !idMap.containsKey(m.getId()) : "Two objects had the same ID";
            idMap.put(m.getId(), m);
        }
        return idMap;
    }

    public static <E> List<E> toList(E e) {
        return Collections.singletonList(e);
    }
}
