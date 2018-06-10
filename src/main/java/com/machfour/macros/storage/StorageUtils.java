package com.machfour.macros.storage;

import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.Table;
import com.machfour.macros.util.StringJoiner;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class StorageUtils {
    private StorageUtils() {
    }

    private static <M> String joinColumns(Iterable<Column<M, ?>> columns, String suffix) {
        return new StringJoiner<>(columns).sep(", ").stringFunc(Column::sqlName).suffix(suffix).join();
    }

    private static <M> String joinColumns(Iterable<Column<M, ?>> columns) {
        return joinColumns(columns, "");
    }

    private static String makeQuestionMarks(int howMany) {
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
        return new StringJoiner<>(questionIterator).sep(", ").join();
    }

    // " WHERE column1 = ?"
    // " WHERE column1 IN (?, ?, ?, ...?)"
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
                return " WHERE " + whereColumn.sqlName() + " IN (" + makeQuestionMarks(nValues) + ")";
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
                    bracketedWhereClauses.add("(" + c.sqlName() + " LIKE ? )");
                }
                return new StringJoiner<>(bracketedWhereClauses).sep(" OR ").join();
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
        return new StringJoiner<>(words).join();
    }

    // columns must be a subset of table.columns()
    public static <M> String insertTemplate(Table<M> t, List<Column<M, ?>> orderedColumns) {
        String placeholders = makeQuestionMarks(orderedColumns.size());
        return "INSERT INTO " + t.name() + " (" + joinColumns(orderedColumns) + ") VALUES ( " + placeholders + ")";
    }

    public static <M, J> String updateTemplate(Table<M> t, List<Column<M, ?>> orderedColumns, Column<M, J> keyCol) {
        return "UPDATE " + t.name() + " SET " + joinColumns(orderedColumns, "= ?") + makeWhereString(keyCol, 1);
    }


    public static <M> void bindData(PreparedStatement p, ColumnData<M> values, List<Column<M, ?>> orderedColumns, Object... extras) throws SQLException {
        int colIndex = 1; // parameters are 1 indexed!
        for (Column<M, ?> col : orderedColumns) {
            // Internally, setObject() relies on a ladder of instanceof checks
            p.setObject(colIndex, values.getAsRaw(col));
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

    public static <E> void bindObjects(PreparedStatement p, List<E> objects) throws SQLException {
        bindObjects(p, 1, objects);
    }

    private static void bindObjects(PreparedStatement p, int startIndex, List<?> objects) throws SQLException {
        int colIndex = startIndex;
        for (Object o : objects) {
            p.setObject(colIndex, o);
            colIndex++;
        }
    }


    public static <M extends MacrosPersistable> Map<Long, M> makeIdMap(List<M> objects) {
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
