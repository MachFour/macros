package com.machfour.macros.storage;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.Table;
import com.machfour.macros.objects.FoodPortion;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MacrosDataSource {
    // Used to create a persistent connection that lasts across calls to the DB.
    // Caller MUST call closeConnection in a finally block
    void openConnection() throws SQLException;
    void closeConnection() throws SQLException;
    // By default, database functions will autocommit.
    // These functions can be used to temporarily disable autocommit and are useful to group multiple operations together
    void beginTransaction() throws SQLException;
    void endTransaction() throws SQLException;

    @NotNull
    <M> List<Long> stringSearch(Table<M> t, List<Column<M, String>> cols, String keyword, boolean globBefore, boolean globAfter) throws SQLException;

    @NotNull
    <M, I, J> List<I> selectColumn(Table<M> t, Column<M, I> selected, Column<M, J> where, Collection<J> whereValues, boolean distinct) throws SQLException;

    @NotNull
    <M, I, J> Map<I, J> selectColumnMap(Table<M> t, Column<M, I> keyColumn, Column<M, J> valueColumn, Set<I> keys) throws SQLException;

    <M extends MacrosEntity<M>> int updateObjects(Collection<? extends M> objects) throws SQLException;

    // does DELETE FROM (t) WHERE (whereColumn) = (whereValue)
    // or DELETE FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    <M, J> int deleteByColumn(Table<M> t, Column<M, J> whereColumn, Collection<J> whereValues) throws SQLException;

    <M> int deleteById(Long id, Table<M> t) throws SQLException;

    <M> int clearTable(Table<M> t) throws SQLException;


    <M extends MacrosEntity<M>> boolean idExistsInTable(Table<M> table, long id) throws SQLException;
    <M extends MacrosEntity<M>> Map<Long, Boolean> idsExistInTable(Table<M> table, List<Long> ids) throws SQLException;

    <M extends MacrosEntity<M>> int insertObjectData(@NotNull List<ColumnData<M>> objectData, boolean withId) throws SQLException;

    @NotNull
    <M, J> Map<J, Long> getIdsByKeysNoEmpty(Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException;
    @NotNull
    <M, J> Map<J, M> getRawObjectsByKeysNoEmpty(Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException;

    @NotNull
    <M> Map<Long, M> getAllRawObjects(Table<M> t) throws SQLException;

}
