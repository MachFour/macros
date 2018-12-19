package com.machfour.macros.linux;

import com.machfour.macros.core.*;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.storage.DatabaseUtils;

import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static com.machfour.macros.storage.DatabaseUtils.toList;

// data source provided by Xerial library

public class LinuxDatabase extends MacrosDatabase implements MacrosDataSource {
    // singleton
    private static LinuxDatabase instance;
    private static String dbPath = null;
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

    public static LinuxDatabase getInstance(@NotNull String dbFile) {
        if (instance == null || !dbFile.equals(dbPath)) {
            instance = new LinuxDatabase(dbFile);
            dbPath = dbFile;
        }
        return instance;
    }

    // returns persistent connection if there is one, othewise a new temporary one.
    private Connection getConnection() throws SQLException {
        if (connection != null) {
            return connection;
        } else {
            return dataSource.getConnection();
        }
    }

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
            String createSchemaSql = createStatements(Files.readAllLines(Config.INIT_SQL));
            String createTriggersSql = createStatements(Files.readAllLines(Config.TRIG_SQL));
            String initialDataSql = createStatements(Files.readAllLines(Config.DATA_SQL));
            try (Statement s = c.createStatement()) {
                System.out.println("Create schema...");
                s.executeUpdate(createSchemaSql);
                System.out.println("Add triggers...");
                s.executeUpdate(createTriggersSql);
                System.out.println("Add data...");
                s.executeUpdate(initialDataSql);
            }
        }
    }
    @Override
    protected <M extends MacrosPersistable> int deleteById(Long id, Table<M> t) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement s = c.prepareStatement(DatabaseUtils.deleteTemplate(t, t.getIdColumn()))) {
            LinuxDatabaseUtils.bindObjects(s, toList(id));
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
                 PreparedStatement p = c.prepareStatement(DatabaseUtils.selectLikeTemplate(t, t.getIdColumn(), cols))) {
                // have to append the percent sign for LIKE globbing to the actual argument string
                String keywordGlob = keyword + "%";
                List<String> keywordCopies = Collections.nCopies(cols.size(), keywordGlob);
                LinuxDatabaseUtils.bindObjects(p, keywordCopies);
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
        Map<I, J> resultMap = new HashMap<>(keys.size(), 1);
        // for batch queries
        //List<Column<M, ?>> selectColumns = Arrays.asList(keyColumn, valueColumn);
        List<Column<M, ?>> selectColumns = Collections.singletonList(valueColumn);
        try (Connection c = getConnection();
             // should be distinct by default: assert keyColumn.isUnique();
             PreparedStatement p = c.prepareStatement(DatabaseUtils.selectTemplate(t, selectColumns, keyColumn, 1, false))) {
            // do queries one by one so we don't send a huge number of parameters at once
            for (I key : keys) {
                LinuxDatabaseUtils.bindObjects(p, Collections.singletonList(key));
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
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, Collection<J> whereValues, boolean distinct) throws SQLException {
        List<I> resultList = new ArrayList<>(0);
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(DatabaseUtils.selectTemplate(t, selectColumn, whereColumn, whereValues.size(), distinct))) {
            LinuxDatabaseUtils.bindObjects(p, whereValues);
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
    protected <M, J> Map<J, M> getRawObjectsByKeysNoEmpty(Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException {
        // if the list of keys is empty, every row will be returned
        assert !keys.isEmpty() : "List of keys is empty";
        assert !keyCol.isNullable() && keyCol.isUnique() : "Key column can't be nullable and must be unique";
        Map<J, M> objects = new HashMap<>(keys.size(), 1);
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(DatabaseUtils.selectTemplate(t, t.columns(), keyCol, keys.size(), false))) {
            LinuxDatabaseUtils.bindObjects(p, keys);
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
        return objects;
    }

    @Override
    // returns a map of objects by ID
    // TODO make protected
    public <M> Map<Long, M> getAllRawObjects(Table<M> t) throws SQLException {
        Map<Long, M> objects = new HashMap<>();
        try (Connection c = getConnection();
             Statement p = c.createStatement();) {
            try (ResultSet rs = p.executeQuery("SELECT * FROM " + t.name())) {
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    ColumnData<M> data = new ColumnData<>(t);
                    for (Column<M, ?> col : t.columns()) {
                        data.putFromRaw(col, rs.getObject(col.sqlName()));
                    }
                    Long id = data.get(t.getIdColumn());
                    assert (!objects.containsKey(id)) : "ID " + id + " already in returned objects map!";
                    M newObject = t.getFactory().construct(data, ObjectSource.DATABASE);
                    objects.put(id, newObject);
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
            String statement = DatabaseUtils.insertTemplate(table, columnsToInsert);
            try (PreparedStatement p = c.prepareStatement(statement)) {
                for (ColumnData<M> row : objectData) {
                    LinuxDatabaseUtils.bindData(p, row, columnsToInsert);
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
    public <M extends MacrosPersistable<M>> int updateObjects(Collection<M> objects) throws SQLException {
        if (objects.isEmpty()) {
            return 0;
        }

        int saved = 0;
        Table<M> table = objects.iterator().next().getTable();
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(DatabaseUtils.updateTemplate(table, table.columns(), table.getIdColumn()))) {
                for (M object : objects) {
                    LinuxDatabaseUtils.bindData(p, object.getAllData(), table.columns(), object.getId());
                    saved += p.executeUpdate();
                    p.clearParameters();
                }
            }
            c.commit();
            c.setAutoCommit(true);
        }
        return saved;
    }

    @Override
    public <M extends MacrosPersistable> int clearTable(Table<M> t) throws SQLException {
        int removed;
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(DatabaseUtils.deleteTemplate(t))) {
            removed = p.executeUpdate();
        }
        return removed;
    }

    @Override
    protected <M extends MacrosPersistable<M>> boolean idExistsInTable(Table<M> table, long id) throws SQLException {
        String idCol = table.getIdColumn().sqlName();
        String query = "SELECT COUNT(" + idCol + ") AS count FROM " + table.name() + " WHERE " + idCol + " = " + id;
        boolean exists;
        try (Connection c = getConnection()) {
            try (Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery(query);
                rs.next();
                exists = rs.getInt("count") == 1;
            }
        }
        return exists;
    }

    @Override
    protected <M extends MacrosPersistable<M>> Map<Long, Boolean> idsExistInTable(Table<M> table, List<Long> ids) throws SQLException {
        Column<M, Long> idCol = table.getIdColumn();
        Map<Long, Boolean> idMap = new HashMap<>(ids.size(), 1);
        try (Connection c = getConnection()) {
            try (PreparedStatement p = c.prepareStatement(DatabaseUtils.selectTemplate(table, idCol, idCol, ids.size()))) {
                LinuxDatabaseUtils.bindObjects(p, ids);
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
