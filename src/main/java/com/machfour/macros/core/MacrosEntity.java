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

    @Override
    public Map<Column.Fk<M, Long, ?>, ColumnData<?>> getSecondaryFkMap() {
        return Collections.unmodifiableMap(secondaryFkMap);
    }

    private final Map<Column.Fk<M, Long, ?>, ColumnData<?>> secondaryFkMap;

    // NOTE data passed in is made Immutable as a side effect
    MacrosEntity(ColumnData<M> data, ObjectSource objectSource) {
        Map<Column<M, ?>, ValidationError> errors = checkMappings(data);
        if (!errors.isEmpty()) {
            throw new SchemaViolation(errors);
        }
        //this.dataMap = new ColumnData<>(data);
        this.dataMap = data;
        this.dataMap.setImmutable();
        this.objectSource = objectSource;
        this.secondaryFkMap = new HashMap<>();
        checkObjectSource();
    }

    public static <M> M construct(Table<M> table, ColumnData<M> data, ObjectSource objectSource) {
        return table.construct(data, objectSource);
    }

    // ensures that the presence of the ID is consistent with the semantics of the objectSource
    // see ObjectSource class for more documentation
    private void checkObjectSource() {
        switch (objectSource) {
            case IMPORT:
            case USER_NEW:
            case COMPUTED:
                assert !hasId() : "Object should not have an ID";
                break;
            case DB_EDIT:
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
            if (dataMap.get(col) == null && !col.isNullable()) {
                badMappings.put(col, ValidationError.NON_NULL);
            }
        }
        return badMappings;
    }



    // this also works for import (without IDs) because both columns are NO_ID
    protected static <M extends MacrosPersistable<M>, J, N extends MacrosPersistable<N>> boolean foreignKeyMatches(
            MacrosEntity<M> childObj, Column.Fk<M, J, N> childCol, MacrosEntity<N> parentObj) {
        J childData = childObj.getData(childCol);
        J parentData = parentObj.getData(childCol.getParentColumn());
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
        return getData(getTable().getIdColumn());
    }

    public Long getCreateTime() {
        return getData(getTable().getCreateTimeColumn());
    }

    public Long getModifyTime() {
        return getData(getTable().getModifyTimeColumn());
    }

    @Override
    public ObjectSource getObjectSource() {
        return objectSource;
    }

    @Override
    public int hashCode() {
        //return getAllData().hashCode() + getObjectSource().ordinal();
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
        List<Column<M, ?>> columnsToCheck = new ArrayList<>(getTable().columns());
        columnsToCheck.remove(getTable().getIdColumn());
        columnsToCheck.remove(getTable().getCreateTimeColumn());
        columnsToCheck.remove(getTable().getModifyTimeColumn());
        return ColumnData.columnsAreEqual(dataMap, o.getAllData(), columnsToCheck);

    }

    @Override
    public <J> J getData(Column<M, J> c) {
        return dataMap.get(c);
    }

    @Override
    public boolean hasData(Column<M, ?> c) {
        return dataMap.hasData(c);
    }

    // returns immutable copy of data map
    @Override
    public ColumnData<M> getAllData() {
        return dataMap;
    }

    public List<Column<M, ?>> getColumns() {
        return getTable().columns();
    }

    public abstract Table<M> getTable();

    // used when importing objects and we don't know the ID to use
    // Supplying a (non-ID) secondary key is used to identify retrieve the ID once that
    // parent object has been stored in the database.
    public <N extends MacrosPersistable<N>> void setFkParentBy2aryKey(Column.Fk<M, Long, N> col, @NotNull N parent) {
        Table<N> table = parent.getTable();
        assert (!table.getSecondaryKeyCols().isEmpty()) : "Table " + table.name() + " has no secondary key columns";
        ColumnData<N> parentSecondaryKey = parent.getAllData().copy(table.getSecondaryKeyCols());
        secondaryFkMap.put(col, parentSecondaryKey);
    }
    public <N extends MacrosPersistable<N>, J> void setFkParentBy2aryKey(Column.Fk<M, Long, N> col, Table<N> parentTable, Column<N, J> parent2aryKey, J data) {
        List<Column<N, ?>> parentKeyColAsList = Collections.singletonList(parent2aryKey);
        assert (parentTable.getSecondaryKeyCols().equals(parentKeyColAsList))
            : "Column " + parent2aryKey.sqlName() + " must be exactly the secondary key of table " + parentTable.name();
        ColumnData<N> parentSecondaryKey = new ColumnData<>(parentTable, parentKeyColAsList);
        parentSecondaryKey.put(parent2aryKey, data);
        secondaryFkMap.put(col, parentSecondaryKey);
    }

    // used when importing objects and we don't know the ID to use
    // Supplying a (non-ID) secondary key is used to identify retrieve the ID once that
    // parent object has been stored in the database.
    @SuppressWarnings("unchecked")
    public <N extends MacrosPersistable<N>> ColumnData<N> getFkParent2aryData(Column.Fk<M, Long, N> col) {
        ColumnData<?> secondaryFkData = secondaryFkMap.get(col);
        // Generics ensure that the table types match
        return (ColumnData<N>) secondaryFkData;
    }
    // NOTE FOR FUTURE REFERENCE: wildcard capture helpers only work if NO OTHER ARGUMENT
    // shares the same parameter as the wildcard being captured.


    @Override
    public String toString() {
        return getTable().name() + " object, from " + objectSource + " data: " + dataMap.toString();
    }
}
