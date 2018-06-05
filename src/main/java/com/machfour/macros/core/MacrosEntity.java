package com.machfour.macros.core;

import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.validation.SchemaViolation;
import com.machfour.macros.validation.ValidationError;
import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * parent class for all macros persistable objects
 */

public abstract class MacrosEntity<M extends MacrosPersistable> implements MacrosPersistable<M> {
    private final ColumnData<M> dataMap;
    // whether this object was created from a database instance or whether it was created by the
    // application (e.g. by a 'new object' action initiated by the user)
    private final ObjectSource objectSource;

    MacrosEntity(ColumnData<M> data, ObjectSource objectSource) {
        Map<Column<M, ?>, ValidationError> errors = checkMappings(data);
        if (!errors.isEmpty()) {
            throw new SchemaViolation(errors);
        }
        this.dataMap = data;
        this.objectSource = objectSource;
        checkObjectSource();
    }

    public static <M> M construct(Table<M> table, ColumnData<M> data, ObjectSource objectSource) {
        return table.construct(data, objectSource);
    }

    // ensures that the presence of the ID is consistent with the semantics of the objectSource
    // see ObjectSource
    private void checkObjectSource() {
        switch (objectSource) {
            case IMPORT:
            case USER_NEW:
            case COMPUTED:
                assert !hasId() : "Object should not have an ID";
                break;
            case USER_EDIT:
            case RESTORE:
            case DATABASE:
                assert hasId() : "Object should have an ID";
                break;
            default:
                assert false : "Unrecognised objectSource: " + objectSource;
        }

    }
    /*
       Checks that:
       * Non null constraints as defined by the columns are upheld
       If any violations are found, the affected column as well as an enum value describing the violation are recorded
       in a map, which is returned at the end, after all columns have been processed.
       Returns a list of columns whose non-null constraints have been violated, or an empty list otherwise
       Note that if the assertion passes, then dataMap has the correct columns as keys
     */
    private Map<Column<M, ?>, ValidationError> checkMappings(ColumnData<M> dataMap) {
        List<Column<M, ?>> required = getColumns();
        Map<Column<M, ?>, ValidationError> badMappings = new HashMap<>(required.size());
        for (Column<M, ?> col : required) {
            if (dataMap.unboxColumn(col) == null && !col.isNullable()) {
                badMappings.put(col, ValidationError.NON_NULL);
            }
        }
        return badMappings;
    }



    // this also works for import (without IDs) because both columns are NO_ID
    protected static <M extends MacrosPersistable, J, N extends MacrosPersistable> boolean foreignKeyMatches(
            MacrosEntity<M> childObj, Column.ForeignKey<M, J, N> childCol, MacrosEntity<N> parentObj) {
        J childData = childObj.getTypedDataForColumn(childCol);
        J parentData = parentObj.getTypedDataForColumn(childCol.getParentColumn());
        return Objects.equals(childData, parentData);

    }
    /*
    // used by child classes to create default instance
    protected MacrosEntity() {
        this(MacrosPersistable.NO_ID, getCurrentTimeStamp(), getCurrentTimeStamp(), false);
    }
    */
    @NotNull
    public Long getId() {
        return getTypedDataForColumn(getTable().getIdColumn());
    }

    public Long getCreateTime() {
        return getTypedDataForColumn(getTable().getCreateTimeColumn());
    }

    public Long getModifyTime() {
        return getTypedDataForColumn(getTable().getModifyTimeColumn());
    }

    @Override
    public boolean isFromDb() {
        return objectSource == ObjectSource.DATABASE;
    }

    @Override
    public ObjectSource getObjectSource() {
        return objectSource;
    }

    @Override
    public int hashCode() {
        //return getAllData().hashCode() + (isFromDb ? 1 : 0);
        return getAllData().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MacrosEntity
            //&& isFromDb == ((MacrosEntity) o).isFromDb
            && dataMap.equals(((MacrosEntity) o).dataMap);
    }

    public boolean equalsWithoutMetadata(MacrosPersistable<M> o) {
        if (o == null) {
            return false;
        }
        List<Column<M, ?>> columnsToCheck = new ArrayList<>(getColumns());
        columnsToCheck.remove(getTable().getIdColumn());
        columnsToCheck.remove(getTable().getCreateTimeColumn());
        columnsToCheck.remove(getTable().getModifyTimeColumn());
        return ColumnData.columnsAreEqual(dataMap, o.getAllData(), columnsToCheck);

    }

    @Override
    public <J> J getTypedDataForColumn(Column<M, J> c) {
        return dataMap.unboxColumn(c);
    }

    @Override
    public boolean hasData(Column<M, ?> c) {
        return dataMap.hasData(c);
    }

    // returns immutable copy of data map
    @Override
    public ColumnData<M> getAllData() {
        return new ColumnData<>(dataMap, true);
    }

    public List<Column<M, ?>> getColumns() {
        return getTable().columns();
    }

    public Map<String, Column<M, ?>> getColumnsByName() {
        return getTable().columnsByName();
    }

    public abstract Table<M> getTable();

    @Override
    public String toString() {
        return getTable().name() + " object, " + dataMap.toString() + " (from " + objectSource + ")";
    }
}
