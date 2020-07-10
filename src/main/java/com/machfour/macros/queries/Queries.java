package com.machfour.macros.queries;


import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Table;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.MiscUtils;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queries {
    private Queries() {}

    @NotNull
    public static <M> List<Long> prefixSearch(MacrosDataSource ds,
            Table<M> t, List<Column<M, String>> cols, String keyword) throws SQLException {
        return stringSearch(ds, t, cols, keyword, false, true);
    }

    @NotNull
    public static <M> List<Long> substringSearch(MacrosDataSource ds,
            Table<M> t, List<Column<M, String>> cols, String keyword) throws SQLException {
        return stringSearch(ds, t, cols, keyword, true, true);
    }
    @NotNull
    public static <M> List<Long> exactStringSearch(MacrosDataSource ds,
            Table<M> t, List<Column<M, String>> cols, String keyword) throws SQLException {
        return stringSearch(ds, t, cols, keyword, false, false);
    }

    @NotNull
    public static <M> List<Long> stringSearch(MacrosDataSource ds,
            Table<M> t, List<Column<M, String>> cols, String keyword, boolean globBefore, boolean globAfter) throws SQLException {
        return ds.stringSearch(t, cols, keyword, globBefore, globAfter);
    }

    // Convenience method (default arguments)
    @NotNull
    public static <M, I, J> List<I> selectColumn(MacrosDataSource ds,
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, J whereValue) throws SQLException {
        return selectColumn(ds, t, selectColumn, whereColumn, MiscUtils.toList(whereValue), false);
    }

    // Convenience method (default arguments)
    @NotNull
    public static <M, I, J> List<I> selectColumn(MacrosDataSource ds,
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, Collection<J> whereValues) throws SQLException {
        return selectColumn(ds, t, selectColumn, whereColumn, whereValues, false);
    }


    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @NotNull
    public static <M, I, J> List<I> selectColumn(MacrosDataSource ds,
            Table<M> t, Column<M, I> selected, Column<M, J> where, Collection<J> whereValues, boolean distinct) throws SQLException {
        return ds.selectColumn(t, selected, where, whereValues, distinct);
    }



    /* These functions save the objects given to them into the database, via INSERT or UPDATE.
     * The caller should ensure that objects with an id of null correspond to new entries
     * (INSERTs) into the database, while those with a non-null id correspond to UPDATES of existing
     * rows in the table.
     *
     * Any data that originated from the user should already have been validated
     */
    // Do we really need the list methods? The user will probably only edit one object at a time
    // except for deleting a bunch of foodPortions from one meal, or servings from a food
    public static <M extends MacrosEntity<M>> int insertObjects(MacrosDataSource ds, Collection<? extends M> objects, boolean withId) throws SQLException {
        List<ColumnData<M>> objectData = new ArrayList<>(objects.size());
        for (M object: objects)  {
            objectData.add(object.getAllData());
        }
        return ds.insertObjectData(objectData, withId);
    }

    public static <M extends MacrosEntity<M>> int updateObjects(MacrosDataSource ds, Collection<? extends M> objects) throws SQLException {
        return ds.updateObjects(objects);
    }

    public static <M extends MacrosEntity<M>> int deleteObject(MacrosDataSource ds, @NotNull M o) throws SQLException {
        return ds.deleteById(o.getId(), o.getTable());
    }

    // TODO make this the general one
    public static <M extends MacrosEntity<M>> int deleteObjects(MacrosDataSource ds, @NotNull List<M> objects) throws SQLException {
        int deleted = 0;
        if (!objects.isEmpty()) {
            Table<M> t = objects.get(0).getTable();
            for (M object : objects) {
                if (object != null) {
                    deleted += ds.deleteById(object.getId(), t);
                }
            }
        }
        return deleted;
    }

    // deletes objects with the given ID from
    public static <M extends MacrosEntity<M>> int deleteObjectsById(MacrosDataSource ds, Table<M> table, @NotNull List<Long> ids) throws SQLException {
        return ds.deleteByColumn(table, table.getIdColumn(), ids);
    }


    // returns number of objects saved correctly (i.e. 0 or 1)
    // NB: not (yet) possible to return the ID of the saved objec with SQLite JDBC
    public static <M extends MacrosEntity<M>> int saveObject(MacrosDataSource ds, @NotNull M o) throws SQLException {
        return saveObjects(ds, MiscUtils.toList(o), o.getObjectSource());
    }

    public static <M extends MacrosEntity<M>> int saveObjects(MacrosDataSource ds, Collection<? extends M> objects, ObjectSource objectSource) throws SQLException {
        switch (objectSource) {
            case IMPORT:
                // TODO have overwrite mode; split import into new insert and updates
                /* fall through */
            case USER_NEW:
                return insertObjects(ds, objects, false);
            case DB_EDIT:
                return updateObjects(ds, objects);
            case DATABASE:
                // it's unchanged we don't need to do anything at all!
                return 1;
            case RESTORE:
                // will have ID. Assume database has been cleared?
                return insertObjects(ds, objects, true);
            case COMPUTED:
                // don't want to save these ones either
                assert false : "Why save a computed object?";
                return 0;
            default:
                assert (false) : "Unrecognised object source: " + objectSource;
                return 0;
        }
    }

    private static <M extends MacrosEntity<M>> boolean isInDatabase(MacrosDataSource ds, @NotNull M o) throws SQLException {
        if (o.getId() != MacrosEntity.NO_ID) {
            return ds.idExistsInTable(o.getTable(), o.getId());
        } else {
            List<Column<M, ?>> secondaryKey = o.getTable().getSecondaryKeyCols();
            if (secondaryKey.isEmpty()) {
                // no way to know except by ID...
            }
            // TODO
            return false;
        }
    }

}
