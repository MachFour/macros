package com.machfour.macros.storage;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.Table;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MacrosDatabase implements MacrosDataSource {

    // caller-managed connection, useful to reduce number of calls to DB
    // caller needs to call closeConnection() after. Use with begin and end transaction
    @Override
    public abstract void openConnection() throws SQLException;
    @Override
    public abstract void closeConnection() throws SQLException;

    // By default, database functions will autocommit.
    // These functions can be used to temporarily disable autocommit and are useful to group multiple operations together
    @Override
    public abstract void beginTransaction() throws SQLException;
    @Override
    public abstract void endTransaction() throws SQLException;

    public abstract void initDb() throws SQLException, IOException;

    @Override
    public abstract <M> int deleteById(Long id, Table<M> t) throws SQLException;


    @NotNull
    @Override
    public abstract <M> List<Long> stringSearch(Table<M> t, List<Column<M, String>> cols, String keyword, boolean globBefore, boolean globAfter) throws SQLException;

    @Override
    @NotNull
    public abstract <M, I, J> Map<I, J> selectColumnMap(Table<M> t, Column<M, I> keyColumn, Column<M, J> valueColumn, Set<I> keys) throws SQLException;

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @NotNull
    @Override
    public abstract <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selected, Column<M, J> where, Collection<J> whereValues, boolean distinct) throws SQLException;

    // does DELETE FROM (t) WHERE (whereColumn) = (whereValue)
    // or DELETE FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Override
    public abstract <M, J> int deleteByColumn(Table<M> t, Column<M, J> whereColumn, Collection<J> whereValues) throws SQLException;

    // Retrives an object by a key column, and constructs it without any FK object instances.
    // Returns null if no row in the corresponding table had a key with the given value
    // The collection of keys must not be empty; an assertion error is thrown if so
    // TODO get rid of noEmpty
    @NotNull
    @Override
    public abstract <M, J> Map<J, M> getRawObjectsByKeysNoEmpty(Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException;

    // TODO get rid of noEmpty
    @NotNull
    @Override
    public abstract <M, J> Map<J, Long> getIdsByKeysNoEmpty(Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException;

    // returns map of all objects in table, by ID
    // TODO make protected -- but it's useful for CSV export
    @NotNull
    @Override
    public abstract <M> Map<Long, M> getAllRawObjects(Table<M> t) throws SQLException;

    @Override
    public abstract <M extends MacrosEntity<M>> int insertObjectData(@NotNull List<ColumnData<M>> objectData, boolean withId) throws SQLException;

    // Note that if the id is not found in the database, nothing will be inserted
    @Override
    public abstract <M extends MacrosEntity<M>> int updateObjects(Collection<? extends M> objects) throws SQLException;

    @Override
    public abstract <M extends MacrosEntity<M>> boolean idExistsInTable(Table<M> table, long id) throws SQLException;

    @Override
    public abstract <M extends MacrosEntity<M>> Map<Long, Boolean> idsExistInTable(Table<M> table, List<Long> ids) throws SQLException;

    @Override
    public abstract <M> int clearTable(Table<M> t) throws SQLException;


}
