package com.machfour.macros.storage;

import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.MacrosType;
import com.machfour.macros.data.Table;
import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

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
                return hasNext() ? '?' : null;
            }
        };
        return join(sep, questionIterator);
    }

    private static void checkValidColumns(Table<?> t, List<Column<?>> orderedColumns) {
        assert t.columns().containsAll(orderedColumns) : "Extraneous columns given";
    }

    static String selectTemplate(Table<?> t, List<Column<?>> orderedColumns, Column<?> keyCol) {
        checkValidColumns(t, orderedColumns);
        return "SELECT " + join(",", orderedColumns) + " FROM " + t.name() + " WHERE " + keyCol.sqlName() + " = ?";
    }

    // columns must be a subset of table.columns()
    static String insertTemplate(Table<?> t, List<Column<?>> orderedColumns) {
        checkValidColumns(t, orderedColumns);
        String placeholders = makeQuestionMarks(", ", orderedColumns.size());
        return "INSERT INTO " + t.name() + " (" + join(", ", orderedColumns) + ") VALUES ( " + placeholders + ")";
    }

    static String updateTemplate(Table<?> t, List<Column<?>> orderedColumns, Column<?> keyCol) {
        checkValidColumns(t, orderedColumns);
        return "UPDATE " + t.name() + " SET " + join(",", orderedColumns, "= ?") + " WHERE " + keyCol.sqlName() + " = ?";
    }

    static void bindData(PreparedStatement p, ColumnData values, List<Column<?>> orderedColumns, Object... extras) throws SQLException {
        int colIndex = 0;
        for (Column<?> col : orderedColumns) {
            // Internally, setObject() relies on a ladder of instanceof checks
            p.setObject(colIndex, values.unboxColumn(col));
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

    static void bindObjects(PreparedStatement p, Object... objects) throws SQLException {
        bindObjects(p, 0, objects);
    }

    static void addRawToColumnData(ColumnData<?> c, Column<?> col, Object data) {
        // TODO do any conversion if necessary (e.g. datestamp)
        MacrosType<?> type = col.type();
        if (type.equals(BOOLEAN)) {
            c.putDataUnchecked(col, data, BOOLEAN);
        } else if (type.equals(ID)) {
            c.putDataUnchecked(col, data, ID);
        } else if (type.equals(INTEGER)) {
            c.putDataUnchecked(col, data, INTEGER);
        } else if (type.equals(REAL)) {
            c.putDataUnchecked(col, data, REAL);
        } else if (type.equals(TEXT)) {
            c.putDataUnchecked(col, data, TEXT);
        } else if (type.equals(TIMESTAMP)) {
            c.putDataUnchecked(col, data, TIMESTAMP);
        } else if (type.equals(DATESTAMP)) {
            c.putDataUnchecked(col, DateStamp.fromIso8601String(data.toString()), DATESTAMP);
        } else {
            assert false : "Invalid type";
        }
    }
}
