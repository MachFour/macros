package com.machfour.macros.data;

import com.machfour.macros.validation.Validation;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by max on 4/11/17.
 */
// TODO do we need 'extends MacrosPersistable' on M?
public interface Column<M, T extends MacrosType<J>, J> {
    static <M, T extends MacrosType<J>, J> Column<M, T, J> column(
            String str, T t, boolean userEditable, boolean nullable) {
        return column(str, t, userEditable, nullable, (Supplier<J>) () -> null);
    }

    static <M, T extends MacrosType<J>, J> Column<M, T, J> column(
            String str, T t, boolean userEditable, boolean nullable, @Nullable J defaultValue) {
        return new ColumnImpl<>(str, t, userEditable, nullable, () -> defaultValue);
    }

    // dynamic default value
    static <M, T extends MacrosType<J>, J> Column<M, T, J> column(
            String str, T t, boolean userEditable, boolean nullable, @NotNull Supplier<J> defaultValue) {
        return new ColumnImpl<>(str, t, userEditable, nullable, defaultValue);
    }

    String sqlName();

    T type();

    boolean isUserEditable();

    boolean isNullable();

    DataContainer defaultData();

    List<Validation> getValidations();

}
