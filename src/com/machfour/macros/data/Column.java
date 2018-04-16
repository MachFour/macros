package com.machfour.macros.data;

import com.machfour.macros.validation.Validation;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by max on 4/11/17.
 */
public interface Column<T> {
    static <T> Column<T> column(String str, MacrosType<T> t, boolean userEditable, boolean nullable) {
        return column(str, t, userEditable, nullable, (Supplier<T>) () -> null);
    }

    static <T> Column<T> column(String str, MacrosType<T> t, boolean userEditable, boolean nullable, @Nullable T defaultValue) {
        return new ColumnImpl<>(str, t, userEditable, nullable, () -> defaultValue);
    }

    // dynamic default value
    static <T> Column<T> column(String str, MacrosType<T> t, boolean userEditable, boolean nullable, @NotNull Supplier<T> defaultValue) {
        return new ColumnImpl<>(str, t, userEditable, nullable, defaultValue);
    }

    String sqlName();

    MacrosType<T> type();

    boolean isUserEditable();

    boolean isNullable();

    DataContainer defaultData();

    List<Validation> getValidations();

}
