package com.machfour.macros.linux;

import com.machfour.macros.core.*;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.storage.StorageUtils;
import com.machfour.macros.util.DataUtils;
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
    // records how much time spent in database transactions
    private Connection connection;
    private long millisInDb;

    private LinuxDatabase(String dbFile) {
        Path dbPath = Paths.get(dbFile);
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());
        SQLiteConfig config = new SQLiteConfig();
        config.enableRecursiveTriggers(true);
        config.enforceForeignKeys(true);
        dataSource.setConfig(config);
        connection = null;
        millisInDb = 0;
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

    // returns persistent connection if there is one, othewise a new temporary one.
    private Connection getConnection() throws SQLException {
        if (connection != null) {
            return connection;
        } else {
            return dataSource.getConnection();
        }
    }

    // caller-managed connection, useful to reduce number of calls to DB
    // caller needs to call closeConnection() after
    @Override
    public void openConnection() throws SQLException {
        if (connection != null) {
            connection = dataSource.getConnection();
        }
    }
    @Override
    public void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
    public long getMillisInDb() {
        return millisInDb;
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
        long startMillis = DataUtils.systemMillis();
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
        long endMillis = DataUtils.systemMillis();
        millisInDb += (endMillis - startMillis);
        return resultList;
    }

    @Override
    protected <M, I, J> Map<I, J> selectColumnMap(Table<M> t, Column<M, I> keyColumn, Column<M, J> valueColumn, Set<I> keys) throws SQLException {
        Map<I, J> resultMap = new HashMap<>(keys.size(), 1);
        // for batch queries
        //List<Column<M, ?>> selectColumns = Arrays.asList(keyColumn, valueColumn);
        List<Column<M, ?>> selectColumns = Collections.singletonList(valueColumn);
        long startMillis = DataUtils.systemMillis();
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
        long endMillis = DataUtils.systemMillis();
        millisInDb += (endMillis - startMillis);
        return resultMap;
    }

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Override
    protected <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, Collection<J> whereValues, boolean distinct) throws SQLException {
        List<I> resultList = new ArrayList<>(0);
        long startMillis = DataUtils.systemMillis();
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
        long endMillis = DataUtils.systemMillis();
        millisInDb += (endMillis - startMillis);
        return resultList;
    }

    // Retrives an object by a key column, and constructs it without any FK object instances.
    // Returns null if no row in the corresponding table had a key with the given value
    @Override
    protected <M, J> Map<J, M> getRawObjectsByKeys(Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException {
        // if the list of keys is empty, every row will be returned
        assert !keys.isEmpty() : "List of keys is empty";
        assert !keyCol.isNullable() && keyCol.isUnique() : "Key column can't be nullable and must be unique";
        Map<J, M> objects = new HashMap<>(keys.size(), 1);
        long startMillis = DataUtils.systemMillis();
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(StorageUtils.selectTemplate(t, t.columns(), keyCol, keys.size(), false))) {
            StorageUtils.bindObjects(p, keys);
            try (ResultSet rs = p.executeQuery()) {
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    ColumnData<M> data = new ColumnData<>(t);
                    for (Column<M, ?> col : t.columns()) {
                        data.putFromRaw(col, rs.getObject(col.sqlName()));
                    }
                    J key = data.get(keyCol);
                    assert (key != null);
                    assert (!objects.containsKey(key)) : "Key " + key + " already in returned objects map!";
                    M newObject = t.getFactory().construct(data, ObjectSource.DATABASE);
                    objects.put(key, newObject);
                }
            }
        }
        long endMillis = DataUtils.systemMillis();
        millisInDb += (endMillis - startMillis);
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
        long startMillis = DataUtils.systemMillis();
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
        long endMillis = DataUtils.systemMillis();
        millisInDb += (endMillis - startMillis);
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
        long startMillis = DataUtils.systemMillis();
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
        long endMillis = DataUtils.systemMillis();
        millisInDb += (endMillis - startMillis);
        return saved;
    }

    public <M extends MacrosPersistable> int removeAll(Table<M> t) throws SQLException {
        long startMillis = DataUtils.systemMillis();
        int removed = 0;
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(StorageUtils.deleteTemplate(t))) {
            removed = p.executeUpdate();
        }
        long endMillis = DataUtils.systemMillis();
        millisInDb += (endMillis - startMillis);
        return removed;
    }

    @Override
    protected <M extends MacrosPersistable<M>> boolean idExistsInTable(Table<M> table, long id) throws SQLException {
        String idCol = table.getIdColumn().sqlName();
        String query = "SELECT COUNT(" + idCol + ") AS count FROM " + table.name() + " WHERE " + idCol + " = " + id;
        boolean exists = false;
        long startMillis = DataUtils.systemMillis();
        try (Connection c = getConnection()) {
            try (Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery(query);
                rs.next();
                exists = rs.getInt("count") == 1;
            }
        }
        long endMillis = DataUtils.systemMillis();
        millisInDb += (endMillis - startMillis);
        return exists;
    }

    @Override
    protected <M extends MacrosPersistable<M>> Map<Long, Boolean> idsExistInTable(Table<M> table, List<Long> ids) throws SQLException {
        Column<M, Long> idCol = table.getIdColumn();
        Map<Long, Boolean> idMap = new HashMap<>(ids.size(), 1);
        long startMillis = DataUtils.systemMillis();
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
        long endMillis = DataUtils.systemMillis();
        millisInDb += (endMillis - startMillis);
        // check for missing IDs
        for (Long id : ids) {
            if (!idMap.keySet().contains(id)) {
                idMap.put(id, false);
            }
        }
        return idMap;
    }




}
