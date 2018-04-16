package com.machfour.macros.core;

import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Columns;
import com.machfour.macros.data.Table;
import com.machfour.macros.validation.SchemaViolation;
import com.machfour.macros.validation.ValidationError;
import com.sun.istack.internal.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * parent class for all macros persistable objects
 */

public abstract class MacrosEntity<M extends MacrosPersistable> implements MacrosPersistable<M> {
    private final ColumnData<M> dataMap;
    // whether this object was created from a database instance or whether it was created by the
    // application (e.g. by a 'new object' action initiated by the user)
    private final boolean isFromDb;

    MacrosEntity(ColumnData<M> data, boolean isFromDb) {
        Map<Column<?>, ValidationError> errors = checkMappings(data);
        if (!errors.isEmpty()) {
            throw new SchemaViolation(errors);
        }
        this.dataMap = data;
        this.isFromDb = isFromDb;
    }

    /*
    // just for making metric servings
    protected MacrosEntity(long id) {
        this(id, getCurrentTimeStamp(), getCurrentTimeStamp(), false);
    }

    /*
       Checks that:
       * Non null constraints as defined by the columns are upheld
       If any violations are found, the affected column as well as an enum value describing the violation are recorded
       in a map, which is returned at the end, after all columns have been processed.
       Returns a list of columns whose non-null constraints have been violated, or an empty list otherwise
       Note that if the assertion passes, then dataMap has the correct columns as keys
     */
    private Map<Column<?>, ValidationError> checkMappings(ColumnData<M> dataMap) {
        assert getTable().equals(dataMap.getTable()) : "ColumnData is for a different table";
        List<Column<?>> required = getColumns();
        Map<Column<?>, ValidationError> badMappings = new HashMap<>(required.size());
        for (Column<?> col : required) {
            if (dataMap.unboxColumn(col) == null && !col.isNullable()) {
                badMappings.put(col, ValidationError.NON_NULL);
            }
        }
        return badMappings;
    }

    /*
    // used by child classes to create default instance
    protected MacrosEntity() {
        this(MacrosPersistable.NO_ID, getCurrentTimeStamp(), getCurrentTimeStamp(), false);
    }
    */
    @NotNull
    public Long getId() {
        return getTypedDataForColumn(Columns.Base.ID);
    }

    public Long getCreateTime() {
        return getTypedDataForColumn(Columns.Base.CREATE_TIME);
    }

    public Long getModifyTime() {
        return getTypedDataForColumn(Columns.Base.MODIFY_TIME);
    }

    public boolean isFromDb() {
        return isFromDb;
    }

    @Override
    public int hashCode() {
        return getAllData().hashCode() + (isFromDb ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MacrosEntity
            && isFromDb == ((MacrosEntity) o).isFromDb
            && dataMap.equals(((MacrosEntity) o).dataMap);
    }

    @Override
    public <T> T getTypedDataForColumn(Column<T> c) {
        return dataMap.unboxColumn(c);
    }

    @Override
    public boolean hasData(Column<?> c) {
        return dataMap.hasData(c);
    }

    @Override
    public ColumnData<M> getAllData() {
        return new ColumnData<>(dataMap);
    }

    public List<Column<?>> getColumns() {
        return getTable().columns();
    }

    public Map<String, Column<?>> getColumnsByName() {
        return getTable().columnsByName();
    }

    public abstract Table<M> getTable();

    @Override
    public String toString() {
        return dataMap.toString() + "(" + (isFromDb ? "not " : "") + "from db)";
    }
}
