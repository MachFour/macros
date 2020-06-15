package com.machfour.macros.storage;

import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.datatype.MacrosType;
import com.machfour.macros.core.Table;
import com.machfour.macros.core.datatype.Types;
import com.machfour.macros.util.Function;
import com.machfour.macros.util.MiscUtils;
import com.machfour.macros.util.StringJoiner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
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
            // need the space here in order for the JDBC binding code to work properly
            placeholder = "DATE( ? )";
        } else {
            placeholder = "?";
        }
        return placeholder;
    }

    private static <M> String makePlaceholders(List<Column<M, ?>> columns, Function<Column<M, ?>, String> placeholderFormat) {
        List<String> placeholders = new ArrayList<>(columns.size());
        for (Column<M, ?> c : columns) {
            placeholders.add(placeholderFormat.apply(c));
        }
        return StringJoiner.of(placeholders).sep(", ").join();

    }
    // creates SQL placeholders for the given (ordered) list of columns
    private static <M> String makeInsertPlaceholders(List<Column<M, ?>> columns) {
        return makePlaceholders(columns, (c) -> getSqlPlaceholder(c.getType()));
    }

    // creates SQL placeholders for the given (ordered) list of columns
    private static <M> String makeUpdatePlaceholders(List<Column<M, ?>> columns) {
        return makePlaceholders(columns, (c) -> c.sqlName() + " = " + getSqlPlaceholder(c.getType()));
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

    public static <M> String deleteWhereTemplate(Table<M> table, Column<M, ?> whereColumn, int nValues) {
        return deleteAllTemplate(table) + makeWhereString(whereColumn, nValues);

    }
    // delete all!
    public static <M> String deleteAllTemplate(Table<M> table) {
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
        return selectTemplate(t, MiscUtils.toList(selectColumn), makeWhereLikeString(likeColumns), false);
    }

    public static <M> String selectTemplate(Table<M> t, Column<M, ?> selectColumn, Column<M, ?> whereColumn, int nValues, boolean distinct) {
        return selectTemplate(t, MiscUtils.toList(selectColumn), whereColumn, nValues, distinct);
    }
    public static <M> String selectTemplate(Table<M> t, Column<M, ?> selectColumn, Column<M, ?> whereColumn, int nValues) {
        return selectTemplate(t, MiscUtils.toList(selectColumn), whereColumn, nValues, false);
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

    public static <M extends MacrosEntity<M>> Map<Long, M> makeIdMap(Collection<M> objects) {
        Map<Long, M> idMap = new HashMap<>(objects.size(), 1);
        for (M m : objects) {
            assert !idMap.containsKey(m.getId()) : "Two objects had the same ID";
            idMap.put(m.getId(), m);
        }
        return idMap;
    }

    public static String createStatements(Reader r) throws IOException {
        return createStatements(r, " ");
    }

    // Returns a list of strings such that each one is a complete SQL statement in the SQL
    // file represented by reader r.

    // IMPORTANT DETAIL:
    // It's not enough to do this just by splitting on semicolons, since there are things
    // like nested statements with 'END;' and strings may contain semicolons.
    // So the split token is the semicolon followed by two blank lines, i.e. statements
    // in the SQL file must be terminated by a semicolon immediately followed by \n\n

    // XXX Possible bug if the newline character is different on different platforms.
    public static List<String> createSplitStatements(Reader r) throws IOException {
        String[] splitStatements = createStatements(r, "\n").split(";\n\n");
        // replace remaining newline characters by spaces and add the semicolon back
        List<String> statements = new ArrayList<>(splitStatements.length);
        for (String statement : splitStatements) {
            statements.add(statement.replaceAll("\n", " ") + ";");
        }
        return statements;
    }

    public static String createStatements(Reader r, String linesep) throws IOException {
        List<String> trimmedAndDecommented = new ArrayList<>(32);
        try (BufferedReader reader = new BufferedReader(r)) {
            // steps: remove all comment lines, trim, join, split on semicolon
            while (reader.ready()) {
                String line = reader.readLine();
                int commentIndex = line.indexOf("--");
                if (commentIndex == 0) {
                    // skip comment lines completely
                    continue;
                } else if (commentIndex != -1) {
                    line = line.substring(0, commentIndex);
                }
                //line = line.trim();
                // if line was only space but not completely blank, then ignore
                // need to keep track of blank lines so Android can separate SQL statements
                line = line.replaceAll("\\s+", " ");
                if (!line.equals(" ")) {
                    trimmedAndDecommented.add(line);
                }
            }
        }
        return StringJoiner.of(trimmedAndDecommented).sep(linesep).join();
    }

    public static <M, J> void rethrowAsSqlException(Object rawValue, Column<M, J> col) throws SQLException {
        throw new SQLException(String.format("Could not convert value '%s' for column %s.%s (type %s)",
                rawValue, col.getTable(), col.sqlName(), col.getType()));
    }
}
