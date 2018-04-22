package com.machfour.macros.storage;

import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.data.*;
import com.sun.istack.internal.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

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

    // " WHERE column1 = ?"
    // " WHERE column1 IN (?, ?, ?, ...?)"
    // if nkeys is <= 0, no where string will be formed, so all objects will be returned.
    private static String makeWhereString(Column<?, ?, ?> whereColumn, int nValues) {
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

    static <M> String deleteTemplate(Table<M> table, Column<M, ?, ?> whereColumn) {
        return deleteTemplate(table) + makeWhereString(whereColumn, 1);

    }
    // delete all!
    static <M> String deleteTemplate(Table<M> table) {
        return "DELETE FROM " + table.name();
    }

    // " WHERE (likeColumn[0] LIKE likeValue[0]) OR (likeColumn[1] LIKE likeValue[1]) OR ..."
    private static <M> String makeWhereLikeString(List<Column<M, Types.Text, String>> likeColumns) {
        switch (likeColumns.size()) {
            case 0:
                return "";
            case 1:
                return " WHERE " + likeColumns.get(0).sqlName() + " LIKE ?";
            default:
                List<String> bracketedWhereClauses = new ArrayList<>(likeColumns.size());
                for (Column<?, Types.Text, String> c : likeColumns) {
                    bracketedWhereClauses.add("(" + c.sqlName() + " LIKE ? )");
                }
                return join(" OR ", bracketedWhereClauses);
        }
    }

    static <M, T extends MacrosType<J>, J> String selectLikeTemplate(
            Table<M> t, Column<M, ?, ?> selectColumn, List<Column<M, Types.Text, String>> likeColumns) {
        return selectTemplate(t, toList(selectColumn), makeWhereLikeString(likeColumns), false);
    }

    static <M, T extends MacrosType<J>, J> String selectTemplate(
            Table<M> t, Column<M, ?, ?> selectColumn, Column<M, ?, ?> whereColumn, int nValues, boolean distinct) {
        return selectTemplate(t, toList(selectColumn), whereColumn, nValues, distinct);
    }

    private static <M, T extends MacrosType<J>, J> String selectTemplate(
            Table<M> t, List<Column<M, ?, ?>> orderedColumns, String whereString, boolean distinct) {
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

    static <M, T extends MacrosType<J>, J> String selectTemplate(
            Table<M> t, List<Column<M, ?, ?>> orderedColumns, Column<M, T, J> whereColumn, int nValues, boolean distinct) {
        return selectTemplate(t, orderedColumns, makeWhereString(whereColumn, nValues), distinct);
    }

    // columns must be a subset of table.columns()
    static <M> String insertTemplate(Table<M> t, List<Column<M, ?, ?>> orderedColumns) {
        String placeholders = makeQuestionMarks(", ", orderedColumns.size());
        return "INSERT INTO " + t.name() + " (" + join(", ", orderedColumns) + ") VALUES ( " + placeholders + ")";
    }

    static <M, T extends MacrosType<J>, J> String updateTemplate(Table<M> t, List<Column<M, ?, ?>> orderedColumns, Column<M, T, J> keyCol) {
        return "UPDATE " + t.name() + " SET " + join(",", orderedColumns, "= ?") + makeWhereString(keyCol, 1);
    }

    static <M> void bindData(PreparedStatement p, ColumnData<M> values, List<Column<M, ?, ?>> orderedColumns, Object... extras) throws SQLException {
        int colIndex = 1; // parameters are 1 indexed!
        for (Column<M, ?, ?> col : orderedColumns) {
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
        bindObjects(p, 1, objects);
    }

    private static void bindObjects(PreparedStatement p, int startIndex, List<?> objects) throws SQLException {
        int colIndex = startIndex;
        for (Object o : objects) {
            p.setObject(colIndex, o);
            colIndex++;
        }
    }

    static <M, T extends MacrosType<J>, J> Object columnDataToRaw(ColumnData<M> c, Column<M, T, J> col) {
        MacrosType<J> type = col.type();
        return type.toRaw(c.unboxColumn(col));
    }

    static <M, T extends MacrosType<J>, J> void rawToColumnData(ColumnData<M> c, Column<M, T, J> col, Object data) {
        MacrosType<J> type = col.type();
        c.putData(col, type.fromRaw(data));
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
