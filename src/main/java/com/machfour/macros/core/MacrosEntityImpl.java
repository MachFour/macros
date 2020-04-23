package com.machfour.macros.core;

import com.machfour.macros.validation.SchemaViolation;
import com.machfour.macros.validation.ValidationError;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * parent class for all macros persistable objects
 */

public abstract class MacrosEntityImpl<M extends MacrosEntity> implements MacrosEntity<M> {
    private final ColumnData<M> dataMap;
    // whether this object was created from a database instance or whether it was created by the
    // application (e.g. by a 'new object' action initiated by the user)
    private final ObjectSource objectSource;
    private final Date createDate;
    private final Date modifyDate;
    // TODO only really need to map to the Natural key value,
    // but there's no convenient way of enforcing the type relationship except by wrapping it in a ColumnData
    private final Map<Column.Fk<M, ?, ?>, ColumnData<?>> fkNaturalKeyMap;

    @Override
    public Map<Column.Fk<M, ?, ?>, ColumnData<?>> getFkNaturalKeyMap() {
        return Collections.unmodifiableMap(fkNaturalKeyMap);
    }

    @Override
    public void copyFkNaturalKeyMap(MacrosEntity<M> from) {
        for (Column.Fk<M, ?, ?> fkCol : from.getFkNaturalKeyMap().keySet()) {
            fkNaturalKeyMap.put(fkCol, from.getFkParentNaturalKey(fkCol));
        }
    }

    // NOTE data passed in is made Immutable as a side effect
    protected MacrosEntityImpl(ColumnData<M> data, ObjectSource objectSource) {
        Map<Column<M, ?>, List<ValidationError>> errors = MacrosBuilder.validate(data);
        if (!errors.isEmpty()) {
            throw new SchemaViolation(errors);
        }
        //this.dataMap = new ColumnData<>(data);
        this.dataMap = data;
        this.dataMap.setImmutable();
        this.objectSource = objectSource;
        this.fkNaturalKeyMap = new HashMap<>();
        this.createDate = new Date(data.get(getTable().getCreateTimeColumn())*1000);
        this.modifyDate = new Date(data.get(getTable().getModifyTimeColumn())*1000);
        checkObjectSource();
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
            case INBUILT:
                assert hasId() : "Object should have an ID";
                break;
            default:
                assert false : "Unrecognised objectSource: " + objectSource;
        }

    }
    // this also works for import (without IDs) because both columns are NO_ID
    protected static <M extends MacrosEntity<M>, J, N extends MacrosEntity<N>> boolean foreignKeyMatches(
            MacrosEntityImpl<M> childObj, Column.Fk<M, J, N> childCol, MacrosEntityImpl<N> parentObj) {
        J childData = childObj.getData(childCol);
        J parentData = parentObj.getData(childCol.getParentColumn());
        return MacrosUtils.objectsEquals(childData, parentData);
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

    @NotNull
    public Long createTime() {
        return getData(getTable().getCreateTimeColumn());
    }

    @NotNull
    public Long modifyTime() {
        return getData(getTable().getModifyTimeColumn());
    }

    @NotNull
    public Date createDate() {
        return createDate;
    }

    @NotNull
    public Date modifyDate() {
        return modifyDate;
    }

    @Override
    @NotNull
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
        return o instanceof MacrosEntityImpl
            //&& isFromDb == ((MacrosEntity) o).isFromDb
            && dataMap.equals(((MacrosEntityImpl) o).dataMap);
    }

    public boolean equalsWithoutMetadata(MacrosEntity<M> o) {
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
        J data = dataMap.get(c);
        assert c.isNullable() || data != null : "null data retrieved from not-nullable column";
        return data;
    }

    @Override
    public boolean hasData(Column<M, ?> c) {
        return dataMap.hasData(c);
    }

    // returns immutable copy of data map
    @Override
    public ColumnData<M> getAllData() {
        return getAllData(true);
    }
    @Override
    public ColumnData<M> getAllData(boolean readOnly) {
        if (readOnly) {
            return dataMap;
        } else {
            return dataMap.copy();
        }
    }

    @Override
    public abstract Table<M> getTable();
    @Override
    public abstract Factory<M> getFactory();


    // NOTE FOR FUTURE REFERENCE: wildcard capture helpers only work if NO OTHER ARGUMENT
    // shares the same parameter as the wildcard being captured.
    @Override
    public <N extends MacrosEntity<N>, J> void setFkParentNaturalKey(Column.Fk<M, ?, N> fkCol, Column<N, J> parentNaturalKey, N parent) {
        setFkParentNaturalKey(fkCol, parentNaturalKey, parent.getData(parentNaturalKey));
    }

    // ... or when only the relevant column data is available, but then it's only limited to single-column secondary keys
    @Override
    public <N, J> void setFkParentNaturalKey(Column.Fk<M, ?, N> col, Column<N, J> parentNaturalKey, J data) {
        assert parentNaturalKey.isUnique();
        List<Column<N, ?>> parentKeyColAsList = Collections.singletonList(parentNaturalKey);
        ColumnData<N> parentSecondaryKey = new ColumnData<>(col.getParentTable(), parentKeyColAsList);
        parentSecondaryKey.put(parentNaturalKey, data);
        fkNaturalKeyMap.put(col, parentSecondaryKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N> ColumnData<N> getFkParentNaturalKey(Column.Fk<M, ?, N> fkCol) {
        // Generics ensure that the table types match
        assert getFkNaturalKeyMap().containsKey(fkCol) : "No FK parent data for column: " + fkCol;
        return (ColumnData<N>) getFkNaturalKeyMap().get(fkCol);
    }

    @Override
    public String toString() {
        return getTable().name() + " object, objectSource: " + objectSource + ", data: " + dataMap.toString();
    }
}
