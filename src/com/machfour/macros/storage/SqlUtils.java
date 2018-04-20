package com.machfour.macros.storage;

import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.MacrosType;
import com.machfour.macros.data.Table;
import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static com.machfour.macros.data.MacrosType.*;

class SqlUtils {
    private SqlUtils() {
    }

    static <E> String join(String sep, Iterable<E> toJoin) {
        return join(sep, toJoin.iterator(), null);
    }

    static <E> String join(String sep, Iterator<E> it) {
        return join(sep, it, null);
    }

    static <E> String join(String sep, Iterable<E> toJoin, @Nullable String elementSuffix) {
        return join(sep, toJoin.iterator(), elementSuffix);
    }

    static <E> String join(String sep, Iterator<E> it, @Nullable String elementSuffix) {
        StringBuilder joined = new StringBuilder();
        if (it.hasNext()) {
            joined.append(it.next());
            if (elementSuffix != null) {
                joined.append(elementSuffix);
            }
            while (it.hasNext()) {
                joined.append(sep);
                joined.append(it.next());
                if (elementSuffix != null) {
                    joined.append(elementSuffix);
                }
            }
        }
        return joined.toString();
    }

    private static String makeQuestionMarks(String sep, int howMany) {
        Iterator<Character> questionIterator = new Iterator<Character>() {
            int current = 0;

            @Override
            public boolean hasNext() {
                return current < howMany;
            }

            @Override
            public Character next() {
                Character c = hasNext() ? '?' : null;
                current++;
                return c;
            }
        };
        return join(sep, questionIterator);
    }

    // if nkeys is <= 0, no where string will be formed, so all objects will be returned.
    private static String makeWhereString(Column<?, ?> whereColumn, int nValues) {
        assert nValues >= 0;
        switch (nValues) {
            case 0:
                return "";
            case 1:
                // TODO is this needed?
                //if (whereColumn.type().equals(DATESTAMP)) {
                //    sb.append("DATE(").append(whereColumn.sqlName()).append(")");
                //} else {
                //sb.append(whereColumn.sqlName());
                //}
                return " WHERE " + whereColumn.sqlName() + " = ?";
            default:
                return " WHERE " + whereColumn.sqlName() + " IN (" + makeQuestionMarks(", ", nValues) + ")";
        }
    }

    // "WHERE (likeColumn[0] LIKE likeValue[0]) OR (likeColumn[1] LIKE likeValue[1]) OR ..."
    private static <M extends MacrosPersistable> String makeWhereLikeString(List<Column<M, String>> likeColumns) {
        switch (likeColumns.size()) {
            case 0:
                return "";
            case 1:
                return " WHERE " + likeColumns.get(0).sqlName() + " LIKE ?";
            default:
                List<String> bracketedWhereClauses = new ArrayList<>(likeColumns.size());
                for (Column<?, String> c : likeColumns) {
                    bracketedWhereClauses.add("(" + c.sqlName() + " LIKE ? )");
                }
                return join(" OR ", bracketedWhereClauses);
        }
    }

    static <M extends MacrosPersistable> String selectLikeTemplate(
            Table<M> t, Column<M, ?> selectColumn, List<Column<M, String>> likeColumns) {
        return selectTemplate(t, toList(selectColumn), makeWhereLikeString(likeColumns), false);
    }

    static <M extends MacrosPersistable> String selectTemplate(
            Table<M> t, Column<M, ?> selectColumn, Column<M, ?> whereColumn, int nValues, boolean distinct) {
        return selectTemplate(t, toList(selectColumn), whereColumn, nValues, distinct);
    }

    private static <M extends MacrosPersistable> String selectTemplate(
            Table<M> t, List<Column<M, ?>> orderedColumns, String whereString, boolean distinct) {
        List<String> words = new ArrayList<>(6);
        words.add("SELECT");
        if (distinct) {
            words.add("DISTINCT");
        }
        words.add(join(", ", orderedColumns));
        words.add("FROM");
        words.add(t.name());
        words.add(whereString);
        return join(" ", words);
    }

    static <M extends MacrosPersistable> String selectTemplate(
            Table<M> t, List<Column<M, ?>> orderedColumns, Column<M, ?> whereColumn, int nValues, boolean distinct) {
        return selectTemplate(t, orderedColumns, makeWhereString(whereColumn, nValues), distinct);
    }

    // columns must be a subset of table.columns()
    static <M extends MacrosPersistable> String insertTemplate(Table<M> t, List<Column<M, ?>> orderedColumns) {
        String placeholders = makeQuestionMarks(", ", orderedColumns.size());
        return "INSERT INTO " + t.name() + " (" + join(", ", orderedColumns) + ") VALUES ( " + placeholders + ")";
    }

    static <M extends MacrosPersistable> String updateTemplate(Table<M> t, List<Column<M, ?>> orderedColumns, Column<M, ?> keyCol) {
        return "UPDATE " + t.name() + " SET " + join(",", orderedColumns, "= ?") + makeWhereString(keyCol, 1);
    }

    static <M extends MacrosPersistable> void bindData(PreparedStatement p, ColumnData<M> values, List<Column<M, ?>> orderedColumns, Object... extras) throws SQLException {
        int colIndex = 0;
        for (Column<M, ?> col : orderedColumns) {
            // Internally, setObject() relies on a ladder of instanceof checks
            p.setObject(colIndex, columnDataToRaw(values, col));
            colIndex++;
        }
        bindObjects(p, colIndex, extras);
    }

    private static void bindObjects(PreparedStatement p, int startIndex, Object... objects) throws SQLException {
        int colIndex = startIndex;
        for (Object o : objects) {
            p.setObject(colIndex, o);
            colIndex++;
        }
    }

    static <E> void bindObjects(PreparedStatement p, List<E> objects) throws SQLException {
        bindObjects(p, 0, objects);
    }

    private static void bindObjects(PreparedStatement p, int startIndex, List<?> objects) throws SQLException {
        int colIndex = startIndex;
        for (Object o : objects) {
            p.setObject(colIndex, o);
            colIndex++;
        }
    }

    static <M extends MacrosPersistable> Object columnDataToRaw(ColumnData<M> c, Column<M, ?> col) {
        MacrosType<?> type = col.type();
        Object data = c.unboxColumn(col);
        if (type.equals(BOOLEAN)) {
            return columnDataToRawHelper(c, typedColumn(col, BOOLEAN));
        } else if (type.equals(ID)) {
            return columnDataToRawHelper(c, typedColumn(col, ID));
        } else if (type.equals(INTEGER)) {
            return columnDataToRawHelper(c, typedColumn(col, INTEGER));
        } else if (type.equals(REAL)) {
            return columnDataToRawHelper(c, typedColumn(col, REAL));
        } else if (type.equals(TEXT)) {
            return columnDataToRawHelper(c, typedColumn(col, TEXT));
        } else if (type.equals(TIMESTAMP)) {
            return columnDataToRawHelper(c, typedColumn(col, TIMESTAMP));
        } else if (type.equals(DATESTAMP)) {
            return columnDataToRawHelper(c, typedColumn(col, DATESTAMP));
        } else {
            assert false : "Invalid type";
            return null;
        }
    }

    static <T> T rawToTyped(Object data, MacrosType<T> type) {
        Object converted;
        if (type.equals(BOOLEAN)) {
            converted = data;
        } else if (type.equals(ID)) {
            converted = data;
        } else if (type.equals(INTEGER)) {
            converted = data;
        } else if (type.equals(REAL)) {
            converted = data;
        } else if (type.equals(TEXT)) {
            converted = data;
        } else if (type.equals(TIMESTAMP)) {
            converted = data;
        } else if (type.equals(DATESTAMP)) {
            converted = DateStamp.fromIso8601String(data.toString());
        } else {
            assert false : "Invalid type";
            converted = null;
        }
        return (T) converted;
    }

    static <T> Object typedToRaw(T data, MacrosType<T> type) {
        Object converted;
        if (type.equals(BOOLEAN)) {
            converted = data;
        } else if (type.equals(ID)) {
            converted = data;
        } else if (type.equals(INTEGER)) {
            converted = data;
        } else if (type.equals(REAL)) {
            converted = data;
        } else if (type.equals(TEXT)) {
            converted = data;
        } else if (type.equals(TIMESTAMP)) {
            converted = data;
        } else if (type.equals(DATESTAMP)) {
            converted = data.toString();
        } else {
            assert false : "Invalid type";
            converted = null;
        }
        return converted;
    }

    // if the assert passes then the cast will be fine
    @SuppressWarnings("unchecked")
    static <M extends MacrosPersistable, T> Column<M, T> typedColumn(Column<M, ?> col, MacrosType<T> type) {
        assert col.type().equals(type) : "Invalid type for column";
        return (Column<M, T>) col;
    }

    // wildcard capture
    private static <M extends MacrosPersistable, T> void rawToColumnDataHelper(ColumnData<M> c, Column<M, T> column, Object data) {
        c.putData(column, rawToTyped(data, column.type()));
    }

    // wildcard capture
    private static <M extends MacrosPersistable, T> Object columnDataToRawHelper(ColumnData<M> c, Column<M, T> column) {
        return typedToRaw(c.unboxColumn(column), column.type());
    }

    static <M extends MacrosPersistable> void rawToColumnData(ColumnData<M> c, Column<M, ?> col, Object data) {
        MacrosType<?> type = col.type();
        if (type.equals(BOOLEAN)) {
            rawToColumnDataHelper(c, typedColumn(col, BOOLEAN), data);
        } else if (type.equals(ID)) {
            rawToColumnDataHelper(c, typedColumn(col, ID), data);
        } else if (type.equals(INTEGER)) {
            rawToColumnDataHelper(c, typedColumn(col, INTEGER), data);
        } else if (type.equals(REAL)) {
            rawToColumnDataHelper(c, typedColumn(col, REAL), data);
        } else if (type.equals(TEXT)) {
            rawToColumnDataHelper(c, typedColumn(col, TEXT), data);
        } else if (type.equals(TIMESTAMP)) {
            rawToColumnDataHelper(c, typedColumn(col, TIMESTAMP), data);
        } else if (type.equals(DATESTAMP)) {
            rawToColumnDataHelper(c, typedColumn(col, DATESTAMP), data);
        } else {
            assert false : "Invalid type";
        }
    }

    static <M extends MacrosPersistable> Map<Long, M> makeIdMap(List<M> objects) {
        Map<Long, M> idMap = new HashMap<>(objects.size(), 1);
        for (M m : objects) {
            idMap.put(m.getId(), m);
        }
        return idMap;
    }

    static <E> List<E> toList(E e) {
        return Collections.singletonList(e);
    }
}
