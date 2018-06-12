package com.machfour.macros.linux;

import com.machfour.macros.core.*;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.storage.StorageUtils;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static com.machfour.macros.storage.StorageUtils.toList;

// data source provided by Xerial library

public class LinuxDatabase extends MacrosDatabase implements MacrosDataSource {
    // singleton
    private static LinuxDatabase INSTANCE;
    private final SQLiteDataSource dataSource;

    private LinuxDatabase(String dbFile) {
        Path dbPath = Paths.get(dbFile);
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());
        SQLiteConfig config = new SQLiteConfig();
        config.enableRecursiveTriggers(true);
        config.enforceForeignKeys(true);
        dataSource.setConfig(config);
    }

    public static MacrosDatabase getInstance() {
        return getInstance(Config.DB_LOCATION);
    }

    public static LinuxDatabase getInstance(String dbFile) {
        if (INSTANCE == null) {
            INSTANCE = new LinuxDatabase(dbFile);
        }
        return INSTANCE;
    }

    private static void runStatements(Connection c, List<String> sqlStatements) throws SQLException {
        try (Statement s = c.createStatement()) {
            for (String sql : sqlStatements) {
                s.executeUpdate(sql);
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void initDb() throws SQLException, IOException {
        try (Connection c = getConnection()) {
            List<String> initStatements = new ArrayList<>(3);
            initStatements.add(createStatements(Files.readAllLines(Config.INIT_SQL)));
            initStatements.add(createStatements(Files.readAllLines(Config.TRIG_SQL)));
            initStatements.add(createStatements(Files.readAllLines(Config.DATA_SQL)));
            runStatements(c, initStatements);
        }
    }
    @Override
    protected <M extends MacrosPersistable> int deleteById(Long id, Table<M> t) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement s = c.prepareStatement(StorageUtils.deleteTemplate(t, t.getIdColumn()))) {
            StorageUtils.bindObjects(s, toList(id));
            s.executeUpdate();
            return 1;
        }
    }

    @Override
    @NotNull
    protected <M extends MacrosPersistable> List<Long> prefixSearch(
            Table<M> t, List<Column<M, String>> cols, String keyword) throws SQLException {
        List<Long> resultList = new ArrayList<>(0);
        if (!cols.isEmpty()) {
            // TODO copy-pasted from SelectColumn... probably needs refactoring
            try (Connection c = getConnection();
                 PreparedStatement p = c.prepareStatement(StorageUtils.selectLikeTemplate(t, t.getIdColumn(), cols))) {
                // have to append the percent sign for LIKE globbing to the actual argument string
                String keywordGlob = keyword + "%";
                List<String> keywordCopies = Collections.nCopies(cols.size(), keywordGlob);
                StorageUtils.bindObjects(p, keywordCopies);
                try (ResultSet rs = p.executeQuery()) {
                    for (rs.next(); !rs.isAfterLast(); rs.next()) {
                        resultList.add(rs.getLong(1));
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    protected <M, I, J> Map<I, J> selectColumnMap(Table<M> t, Column<M, I> keyColumn, Column<M, J> valueColumn, Set<I> keys) throws SQLException {
        Map<I, J> resultMap = new HashMap<>();
        // for batch queries
        //List<Column<M, ?>> selectColumns = Arrays.asList(keyColumn, valueColumn);
        List<Column<M, ?>> selectColumns = Collections.singletonList(valueColumn);
        try (Connection c = getConnection();
             // should be distinct by default: assert keyColumn.isUnique();
             PreparedStatement p = c.prepareStatement(StorageUtils.selectTemplate(t, selectColumns, keyColumn, 1, false))) {
            // do queries one by one so we don't send a huge number of parameters at once
            for (I key : keys) {
                StorageUtils.bindObjects(p, Collections.singletonList(key));
                try (ResultSet rs = p.executeQuery()) {
                    for (rs.next(); !rs.isAfterLast(); rs.next()) {
                        //I key = keyColumn.getType().fromRaw(rs.getObject(keyColumn.sqlName()));
                        J value = valueColumn.getType().fromRaw(rs.getObject(valueColumn.sqlName()));
                        assert !resultMap.containsKey(key) : "Two rows in the DB contained the same data in the key column!";
                        resultMap.put(key, value);
                    }
                }
                p.clearParameters();
            }
        }
        return resultMap;
    }

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Override
    protected <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, List<J> whereValues, boolean distinct) throws SQLException {
        List<I> resultList = new ArrayList<>(0);
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(StorageUtils.selectTemplate(t, selectColumn, whereColumn, whereValues.size(), distinct))) {
            StorageUtils.bindObjects(p, whereValues);
            try (ResultSet rs = p.executeQuery()) {
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    Object resultValue = rs.getObject(selectColumn.sqlName());
                    resultList.add(selectColumn.getType().fromRaw(resultValue));
                }
            }
        }
        return resultList;
    }

    // Retrives an object by a key column, and constructs it without any FK object instances.
    // Returns null if no row in the corresponding table had a key with the given value
    @Override
    @NotNull
    protected <M, J> List<M> getRawObjectsByKeys(Table<M> t, Column<M, J> keyCol, List<J> keys) throws SQLException {
        // if the list of keys is empty, every row will be returned
        assert !keys.isEmpty() : "List of keys is empty";
        List<M> objects = new ArrayList<>(keys.size());
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(StorageUtils.selectTemplate(t, t.columns(), keyCol, keys.size(), false))) {
            StorageUtils.bindObjects(p, keys);
            try (ResultSet rs = p.executeQuery()) {
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    ColumnData<M> data = new ColumnData<>(t);
                    for (Column<M, ?> col : t.columns()) {
                        data.putFromRaw(col, rs.getObject(col.sqlName()));
                    }
                    M newObject = t.getFactory().construct(data, ObjectSource.DATABASE);
                    assert (newObject != null);
                    objects.add(newObject);
                }
            }
        }
        return objects;
    }

    @Override
    protected <M extends MacrosPersistable<M>> int insertObjectData(@NotNull List<ColumnData<M>> objectData, boolean withId) throws SQLException {
        if (objectData.isEmpty()) {
            return 0;
        }
        int saved = 0;
        Table<M> table = objectData.get(0).getTable();
        List<Column<M, ?>> columnsToInsert = table.columns();
        if (!withId) {
            columnsToInsert = new ArrayList<>(table.columns());
            columnsToInsert.remove(table.getIdColumn());
        } // else inserting for the first time, but it has an ID that we want to keep intact
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            String statement = StorageUtils.insertTemplate(table, columnsToInsert);
            try (PreparedStatement p = c.prepareStatement(statement)) {
                for (ColumnData<M> row : objectData) {
                    StorageUtils.bindData(p, row, columnsToInsert);
                    saved += p.executeUpdate();
                    p.clearParameters();
                }
            }
            c.commit();
            c.setAutoCommit(true);
        }
        return saved;
    }

    // Note that if the id is not found in the database, nothing will be inserted
    @Override
    public <M extends MacrosPersistable<M>> int updateObjects(@NotNull List<M> objects) throws SQLException {
        if (objects.isEmpty()) {
            return 0;
        }

        int saved = 0;
        Table<M> table = objects.get(0).getTable();
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(StorageUtils.updateTemplate(table, table.columns(), table.getIdColumn()))) {
                for (M object : objects) {
                    StorageUtils.bindData(p, object.getAllData(), table.columns(), object.getId());
                    saved += p.executeUpdate();
                    p.clearParameters();
                }
            }
            c.commit();
            c.setAutoCommit(true);
        }
        return saved;
    }

    public <M extends MacrosPersistable> int removeAll(Table<M> t) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(StorageUtils.deleteTemplate(t))) {
            return p.executeUpdate();
        }
    }

    @Override
    protected <M extends MacrosPersistable<M>> boolean idExistsInTable(Table<M> table, long id) throws SQLException {
        String idCol = table.getIdColumn().sqlName();
        String query = "SELECT COUNT(" + idCol + ") AS count FROM " + table.name() + " WHERE " + idCol + " = " + id;
        try (Connection c = getConnection()) {
            try (Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery(query);
                rs.next();
                return rs.getInt("count") == 1;
            }
        }
    }
    @Override
    protected <M extends MacrosPersistable<M>> Map<Long, Boolean> idsExistInTable(Table<M> table, List<Long> ids) throws SQLException {
        Column<M, Long> idCol = table.getIdColumn();
        Map<Long, Boolean> idMap = new HashMap<>(ids.size(), 1);
        try (Connection c = getConnection()) {
            try (PreparedStatement p = c.prepareStatement(StorageUtils.selectTemplate(table, idCol, idCol, ids.size()))) {
                StorageUtils.bindObjects(p, ids);
                ResultSet rs = p.executeQuery();
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    Long id = rs.getLong(idCol.sqlName());
                    idMap.put(id, true);
                }
                rs.next();
            }
        }
        // check for missing IDs
        for (Long id : ids) {
            if (!idMap.keySet().contains(id)) {
                idMap.put(id, false);
            }
        }
        return idMap;
    }




}
