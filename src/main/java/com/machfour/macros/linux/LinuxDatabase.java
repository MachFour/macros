package com.machfour.macros.linux;

import com.machfour.macros.core.*;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.storage.DatabaseUtils;

import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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

    // returns persistent connection if there is one, otherwise a new temporary one.
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
            throw new IllegalStateException("A persistent connection is open already");
        }
        connection = dataSource.getConnection();
    }

    @Override
    public void closeConnection() throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("A connection hasn't been opened");
        }
        // set instance variable to null first in case exception is thrown?
        // or is this shooting oneself in the foot?
        Connection c = connection;
        connection = null;
        c.close();
    }

    // true if getConnection() returns a temporary connection that needs to be closed immediately after use
    // or a user-managed persistent connection
    private boolean isTempConnection() {
        return connection == null;
    }

    private void closeIfNecessary(Connection c) throws SQLException {
        if (isTempConnection()) {
            c.close();
        }
    }

    @Override
    public void beginTransaction() throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("Connection not open, call openConnection() first");
        }
        connection.setAutoCommit(false);
    }
    @Override
    public void endTransaction() throws SQLException {
        // XXX messy variable access
        if (connection == null) {
            throw new IllegalStateException("Connection not open, call openConnection first() (and beginTransaction())");
        }
        connection.commit();
        connection.setAutoCommit(true);
    }

    public long getMillisInDb() {
        return millisInDb;
    }

    public void initDb() throws SQLException, IOException {
        Connection c = getConnection();
        try (Statement s = c.createStatement();
             Reader init = new FileReader(Config.INIT_SQL);
             Reader trig = new FileReader(Config.TRIG_SQL);
             Reader data = new FileReader(Config.DATA_SQL);
            ) {

            String createSchemaSql = DatabaseUtils.createStatements(init);
            String createTriggersSql = DatabaseUtils.createStatements(trig);
            String initialDataSql = DatabaseUtils.createStatements(data);

            System.out.println("Create schema...");
            s.executeUpdate(createSchemaSql);
            System.out.println("Add triggers...");
            s.executeUpdate(createTriggersSql);
            System.out.println("Add data...");
            s.executeUpdate(initialDataSql);
        } finally {
            closeIfNecessary(c);
        }
    }
    @Override
    protected <M extends MacrosPersistable> int deleteById(Long id, Table<M> t) throws SQLException {
        Connection c = getConnection();
        try (PreparedStatement s = c.prepareStatement(DatabaseUtils.deleteWhereTemplate(t, t.getIdColumn(), 1))) {
            LinuxDatabaseUtils.bindObjects(s, toList(id));
            s.executeUpdate();
            return 1;
        } finally {
            closeIfNecessary(c);
        }

    }

    @Override
    @NotNull

    /*
     * Returns empty list for either blank keyword or column list
     */
    protected <M extends MacrosPersistable> List<Long> stringSearch(Table<M> t, List<Column<M, String>> cols,
            @NotNull String keyword, boolean globBefore, boolean globAfter) throws SQLException {
        List<Long> resultList = new ArrayList<>(0);
        if (!keyword.isEmpty() && !cols.isEmpty()) {
            // TODO copy-pasted from SelectColumn... probably needs refactoring
            Connection c = getConnection();
            try (PreparedStatement p = c.prepareStatement(DatabaseUtils.selectLikeTemplate(t, t.getIdColumn(), cols))) {
                // have to append the percent sign for LIKE globbing to the actual argument string
                String keywordGlob = (globBefore ? "%" : "") + keyword + (globAfter ? "%" : "");
                List<String> keywordCopies = Collections.nCopies(cols.size(), keywordGlob);
                LinuxDatabaseUtils.bindObjects(p, keywordCopies);
                try (ResultSet rs = p.executeQuery()) {
                    for (rs.next(); !rs.isAfterLast(); rs.next()) {
                        resultList.add(rs.getLong(1));
                    }
                }
            } finally {
                closeIfNecessary(c);
            }
        }
        return resultList;
    }

    @Override
    protected <M extends MacrosPersistable, I, J> Map<I, J> selectColumnMap(Table<M> t, Column<M, I> keyColumn,
            Column<M, J> valueColumn, Set<I> keys) throws SQLException {
        Map<I, J> resultMap = new HashMap<>(keys.size(), 1);
        // for batch queries
        //List<Column<M, ?>> selectColumns = Arrays.asList(keyColumn, valueColumn);
        List<Column<M, ?>> selectColumns = Collections.singletonList(valueColumn);
        Connection c = getConnection();
        try (PreparedStatement p = c.prepareStatement(DatabaseUtils.selectTemplate(t, selectColumns, keyColumn, 1, false))) {
             // should be distinct by default: assert keyColumn.isUnique();
            for (I key : keys) {
                // do queries one by one so we don't send a huge number of parameters at once
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
        } finally {
            closeIfNecessary(c);
        }
        return resultMap;
    }

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Override
    protected <M extends MacrosPersistable, I, J> List<I> selectColumn(Table<M> t, Column<M, I> selectColumn,
            Column<M, J> whereColumn, Collection<J> whereValues, boolean distinct) throws SQLException {
        List<I> resultList = new ArrayList<>(0);
        Connection c = getConnection();
        try (PreparedStatement p = c.prepareStatement(DatabaseUtils.selectTemplate(t, selectColumn, whereColumn, whereValues.size(), distinct))) {
            LinuxDatabaseUtils.bindObjects(p, whereValues);
            try (ResultSet rs = p.executeQuery()) {
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    Object resultValue = rs.getObject(selectColumn.sqlName());
                    resultList.add(selectColumn.getType().fromRaw(resultValue));
                }
            }
        } finally {
            closeIfNecessary(c);
        }
        return resultList;
    }

    // Constructs a map of key column value to raw object data (i.e. no object references initialised
    // Keys that do not exist in the database will not be contained in the output map
    // The returned map is never null
    @Override
    protected <M extends MacrosPersistable, J> Map<J, M> getRawObjectsByKeysNoEmpty(
            Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException {
        // if the list of keys is empty, every row will be returned
        assert !keys.isEmpty() : "List of keys is empty";
        assert !keyCol.isNullable() && keyCol.isUnique() : "Key column can't be nullable and must be unique";
        Map<J, M> objects = new HashMap<>(keys.size(), 1);
        Connection c = getConnection();
        try (PreparedStatement p = c.prepareStatement(DatabaseUtils.selectTemplate(t, t.columns(), keyCol, keys.size(), false))) {
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
        } finally {
            closeIfNecessary(c);
        }
        return objects;
    }

    // Constructs a map of key column value to ID
    // Keys that do not exist in the database will not be contained in the output map
    // The returned map is never null
    @Override
    protected <M extends MacrosPersistable, J> Map<J, Long> getIdsByKeysNoEmpty(
            Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException {
        // if the list of keys is empty, every row will be returned
        assert !keys.isEmpty() : "List of keys is empty";
        assert !keyCol.isNullable() && keyCol.isUnique() : "Key column can't be nullable and must be unique";
        List<Column<M, ?>> keyAndId = Arrays.asList(keyCol, t.getIdColumn()); // select the ID column plus the key

        Map<J, Long> idMap = new HashMap<>(keys.size(), 1);
        Connection c = getConnection();
        try (PreparedStatement p = c.prepareStatement(DatabaseUtils.selectTemplate(t, keyAndId, keyCol, keys.size(), false))) {
            LinuxDatabaseUtils.bindObjects(p, keys);
            try (ResultSet rs = p.executeQuery()) {
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    long id = rs.getLong(t.getIdColumn().sqlName());
                    J key = keyCol.getType().cast(rs.getObject(keyCol.sqlName())); //XXX wow
                    assert (key != null);
                    assert (!idMap.containsKey(key)) : "Key " + key + " already in returned map!";
                    idMap.put(key, id);
                }
            }
        } finally {
            closeIfNecessary(c);
        }
        return idMap;
    }

    @Override
    // returns a map of objects by ID
    // TODO make protected
    public <M extends MacrosPersistable> Map<Long, M> getAllRawObjects(Table<M> t) throws SQLException {
        Map<Long, M> objects = new HashMap<>();
        Connection c = getConnection();
        try (ResultSet rs = c.createStatement().executeQuery("SELECT * FROM " + t.name())) {
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
        } finally {
            closeIfNecessary(c);
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
        Connection c = getConnection();
        boolean prevAutoCommit = c.getAutoCommit();
        c.setAutoCommit(false);
        String statement = DatabaseUtils.insertTemplate(table, columnsToInsert);
        try (PreparedStatement p = c.prepareStatement(statement)) {
            for (ColumnData<M> row : objectData) {
                LinuxDatabaseUtils.bindData(p, row, columnsToInsert);
                saved += p.executeUpdate();
                p.clearParameters();
            }
            if (prevAutoCommit) {
                c.commit();
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            String newMessage = e.getMessage() + "\n" +
                    "thrown by insertObjectData() on table " + table.name() + " and object data:\n"
                    + objectData.get(saved).toString();
            throw new SQLException(newMessage, e.getCause());
        } finally {
            closeIfNecessary(c);
        }
        return saved;
    }

    // Note that if the id is not found in the database, nothing will be inserted
    @Override
    public <M extends MacrosPersistable<M>> int updateObjects(Collection<? extends M> objects) throws SQLException {
        if (objects.isEmpty()) {
            return 0;
        }

        int saved = 0;
        Table<M> table = objects.iterator().next().getTable();
        Connection c = getConnection();
        boolean prevAutoCommit = c.getAutoCommit();
        c.setAutoCommit(false);
        try (PreparedStatement p = c.prepareStatement(DatabaseUtils.updateTemplate(table, table.columns(), table.getIdColumn()))) {
            for (M object : objects) {
                LinuxDatabaseUtils.bindData(p, object.getAllData(), table.columns(), object.getId());
                saved += p.executeUpdate();
                p.clearParameters();
            }
            if (prevAutoCommit) {
                c.commit();
                c.setAutoCommit(true);
            }
        } finally {
            closeIfNecessary(c);
        }
        return saved;
    }

    @Override
    public <M extends MacrosPersistable> int clearTable(Table<M> t) throws SQLException {
        int removed;
        Connection c = getConnection();
        try (PreparedStatement p = c.prepareStatement(DatabaseUtils.deleteAllTemplate(t))) {
            removed = p.executeUpdate();
        } finally {
            closeIfNecessary(c);
        }
        return removed;
    }

    @Override
    public <M extends MacrosPersistable, J> int deleteByColumn(Table<M> t, Column<M, J> whereColumn, Collection<J> whereValues) throws SQLException {
        int removed;
        Connection c = getConnection();
        try (PreparedStatement p = c.prepareStatement(DatabaseUtils.deleteWhereTemplate(t, whereColumn, whereValues.size()))) {
            LinuxDatabaseUtils.bindObjects(p, whereValues);
            removed = p.executeUpdate();
        } finally {
            closeIfNecessary(c);
        }
        return removed;
    }

    @Override
    protected <M extends MacrosPersistable<M>> boolean idExistsInTable(Table<M> table, long id) throws SQLException {
        String idCol = table.getIdColumn().sqlName();
        String query = "SELECT COUNT(" + idCol + ") AS count FROM " + table.name() + " WHERE " + idCol + " = " + id;
        boolean exists;
        Connection c = getConnection();
        try (Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery(query);
            rs.next();
            exists = rs.getInt("count") == 1;
        } finally {
            closeIfNecessary(c);
        }
        return exists;
    }

    @Override
    protected <M extends MacrosPersistable<M>> Map<Long, Boolean> idsExistInTable(Table<M> table, List<Long> ids) throws SQLException {
        Column<M, Long> idCol = table.getIdColumn();
        Map<Long, Boolean> idMap = new HashMap<>(ids.size(), 1);
        Connection c = getConnection();
        try (PreparedStatement p = c.prepareStatement(DatabaseUtils.selectTemplate(table, idCol, idCol, ids.size()))) {
            LinuxDatabaseUtils.bindObjects(p, ids);
            ResultSet rs = p.executeQuery();
            for (rs.next(); !rs.isAfterLast(); rs.next()) {
                Long id = rs.getLong(idCol.sqlName());
                idMap.put(id, true);
            }
            rs.next();
        } finally {
            closeIfNecessary(c);
        }
        // check for missing IDs
        for (Long id : ids) {
            if (!idMap.containsKey(id)) {
                idMap.put(id, false);
            }
        }
        return idMap;
    }
}
