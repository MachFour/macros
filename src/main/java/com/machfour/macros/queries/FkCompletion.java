package com.machfour.macros.queries;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.Factory;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.storage.MacrosDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.machfour.macros.util.MiscUtils.toList;

public class FkCompletion {
    // fkColumns by definition contains only the foreign key columns
    protected static <M extends MacrosEntity<M>> boolean fkIdsPresent(M object) {
        boolean idsPresent = true;
        for (Column.Fk<M, ?, ?> fkCol : object.getTable().fkColumns()) {
            // if the FK refers to an ID column and it's not nullable, make sure there's a value
            if (fkCol.getParentColumn().equals(fkCol.getParentTable().getIdColumn()) && !fkCol.isNullable()) {
                idsPresent &= object.hasData(fkCol) && !object.getData(fkCol).equals(MacrosEntity.NO_ID);
            }
        }
        return idsPresent;
    }

    // wildcard capture helper for natural key column type
    public static <M extends MacrosEntity<M>, J, N, I> Map<I, J> completeFkIdColHelper(MacrosDataSource ds,
            Column.Fk<M, J, N> fkColumn, Column<N, I> parentNaturalKeyCol, List<ColumnData<N>> data) throws SQLException {
        assert (parentNaturalKeyCol.isUnique());
        Set<I> uniqueColumnValues = new HashSet<>(data.size());
        for (ColumnData<N> cd : data) {
            uniqueColumnValues.add(cd.get(parentNaturalKeyCol));
        }
        return ds.selectColumnMap(fkColumn.getParentTable(), parentNaturalKeyCol, fkColumn.getParentColumn(), uniqueColumnValues);
    }

    // wildcard capture helper for parent unique column type
    private static <M extends MacrosEntity<M>, J, N> List<M> completeFkCol(MacrosDataSource ds, List<M> objects, Column.Fk<M, J, N> fkCol) throws SQLException {
        List<M> completedObjects = new ArrayList<>(objects.size());
        List<ColumnData<N>> naturalKeyData = new ArrayList<>(objects.size());
        for (M object : objects) {
            // needs to be either imported data, new (from builder), or computed, for Recipe nutrition data
            assert (Arrays.asList(ObjectSource.IMPORT, ObjectSource.USER_NEW, ObjectSource.COMPUTED)
                        .contains(object.getObjectSource())) : "Object is not from import, new or computed";

            assert !object.getFkNaturalKeyMap().isEmpty() : "Object has no FK data maps";
            ColumnData<N> objectNkData = object.getFkParentNaturalKey(fkCol);
            assert objectNkData != null : "Natural key data was null";
            naturalKeyData.add(objectNkData);
        }
        Column<N, ?> parentNaturalKeyCol = fkCol.getParentTable().getNaturalKeyColumn();
        assert (parentNaturalKeyCol != null) : "Table " + fkCol.getParentTable().name() + " has no natural key defined";
        Map<?, J> uniqueKeyToFkParent = completeFkIdColHelper(ds, fkCol, parentNaturalKeyCol, naturalKeyData);
        for (M object : objects) {
            ColumnData<M> newData = object.getAllData().copy();
            // TODO might be able to remove one level of indirection here because the ParentUniqueColData only contains parentNaturalKeyCol
            newData.put(fkCol, uniqueKeyToFkParent.get(object.getFkParentNaturalKey(fkCol).get(parentNaturalKeyCol)));
            M newObject = object.getTable().construct(newData, object.getObjectSource());
            // copy over old FK data to new object
            newObject.copyFkNaturalKeyMap(object);
            completedObjects.add(newObject);
        }
        return completedObjects;
    }

    public static <M extends MacrosEntity<M>> List<M> completeForeignKeys(MacrosDataSource ds, Collection<M> objects, Column.Fk<M, ?, ?> fk)
            throws SQLException {
        return completeForeignKeys(ds, objects, toList(fk));
    }

    // Methods used when saving multiple new objects to the database at once which must be cross-referenced, and
    // IDs are not known at the time of saving.
    // These methods replace a manual database retrieval of objects whose ID is needed. However, there still
    // needs to be a well-ordering of dependencies between the fields of each type of object, so that the first type
    // is inserted without depending on unknown fields/IDs of other types, the second depends only on the first, and so on
    public static <M extends MacrosEntity<M>> List<M> completeForeignKeys(MacrosDataSource ds, Collection<M> objects, List<Column.Fk<M, ?, ?>> which)
            throws SQLException {
        List<M> completedObjects = new ArrayList<>(objects.size());
        if (!objects.isEmpty()) {
            // objects without foreign key data yet (mutable copy of first argument)
            List<M> partiallyCompletedObjects = new ArrayList<>(objects);

            // hack to get correct factory type without passing it explicitly as argument
            Factory<M> factory = partiallyCompletedObjects.get(0).getFactory();

            // cycle through the FK columns.
            for (Column.Fk<M, ?, ?> fkCol: which) {
                partiallyCompletedObjects = completeFkCol(ds, partiallyCompletedObjects, fkCol);
            }
            // Check everything's fine and (not yet implemented) change source to ObjectSource.IMPORT_FK_PRESENT
            for (M object : partiallyCompletedObjects) {
                assert fkIdsPresent(object);
                completedObjects.add(factory.construct(object.getAllData(), object.getObjectSource()));
            }
        }
        return completedObjects;
    }
}
