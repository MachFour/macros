package com.machfour.macros.core;

import com.machfour.macros.validation.ValidationError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Table<M> {
    String name();

    List<Column<M, ?>> columns();
    // return all FK columns
    List<Column.Fk<M, ?, ?>> fkColumns();

    Map<String, Column<M, ?>> columnsByName();

    Column<M, ?> columnForName(String name);

    Column<M, Long> getIdColumn();

    Column<M, Long> getCreateTimeColumn();

    Column<M, Long> getModifyTimeColumn();

    // returns a list of columns that can be used to identify an individual row,
    // if such a list exists for this table. If not, an empty list is returned.
    List<Column<M, ?>> getSecondaryKeyCols();
    // special case when secondary key has a single column.
    Column<M, ?> getNaturalKeyColumn();

    Factory<M> getFactory();

    default M construct(ColumnData<M> dataMap, ObjectSource objectSource) {
        return getFactory().construct(dataMap, objectSource);
    }

    /*
       Checks that:
       * Non null constraints as defined by the columns are upheld
       If any violations are found, the affected column as well as an enum value describing the violation are recorded
       in a map, which is returned at the end, after all columns have been processed.
       Returns a list of columns whose non-null constraints have been violated, or an empty list otherwise
       Note that if the assertion passes, then dataMap has the correct columns as keys
     */
    default Map<Column<M, ?>, ValidationError> validate(ColumnData<M> dataMap) {
        List<Column<M, ?>> required = columns();
        Map<Column<M, ?>, ValidationError> badMappings = new HashMap<>(required.size());
        for (Column<M, ?> col : required) {
            if (dataMap.get(col) == null && !col.isNullable()) {
                badMappings.put(col, ValidationError.NON_NULL);
            }
        }
        return badMappings;
    }
}
