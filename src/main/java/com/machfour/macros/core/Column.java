package com.machfour.macros.core;

import com.machfour.macros.validation.Validation;

import java.util.List;

/**
 * Created by max on 4/11/17.
 */
public interface Column<M, J> {
    /*
     * Describes one column referencing another. N is the parent table type
     */
    interface Fk<M, J, N> extends Column<M, J> {
        Column<N, J> getParentColumn();
        Table<N> getParentTable();
    }

    String sqlName();

    J defaultData();
    MacrosType<J> getType();

    // index used to store and look up data in the ColumnMap. Not necessarily the order in the DB
    int index();
    // whether the column should be shown to and editable by users
    boolean isUserEditable();
    // whether the column is allowed to be saved into the DB as null
    boolean isNullable();

    // whether the column can be used as part of an alternative key to identify a row
    // NOTE there can also be other columns in the table needed to form the full secondary key.
    // Also, not all tables may have a secondary key.
    boolean inSecondaryKey();

    boolean isUnique();

    List<Validation> getValidations();


    static <J> ColumnImpl.Builder<J> builder(String name, MacrosType<J> type, int index) {
        return new ColumnImpl.Builder<>(name, type, index);
    }
}
