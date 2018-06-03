package com.machfour.macros.data;

import com.machfour.macros.util.Supplier;
import com.machfour.macros.validation.Validation;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.List;

/**
 * Created by max on 4/11/17.
 */
public interface Column<M, T extends MacrosType<J>, J> {
    static <M, T extends MacrosType<J>, J> Column<M, T, J> column(
            int index, String str, T t, boolean userEditable, boolean nullable) {
        return column(index, str, t, userEditable, nullable, (Supplier<J>) () -> null);
    }

    static <M, T extends MacrosType<J>, J> Column<M, T, J> column(
            int index, String str, T t, boolean userEditable, boolean nullable, @Nullable J defaultValue) {
        return new ColumnImpl<>(str, t, index, userEditable, nullable, () -> defaultValue);
    }

    // dynamic default value
    static <M, T extends MacrosType<J>, J> Column<M, T, J> column(
            int index, String str, T t, boolean userEditable, boolean nullable, @NotNull Supplier<J> defaultValue) {
        return new ColumnImpl<>(str, t, index, userEditable, nullable, defaultValue);
    }

    String sqlName();

    T type();

    // index used to store and look up data in the ColumnMap. Not necessarily the order in the DB
    int index();

    boolean isUserEditable();

    boolean isNullable();

    J defaultData();

    List<Validation> getValidations();

}
