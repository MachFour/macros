package com.machfour.macros.data;

import com.machfour.macros.validation.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ColumnImpl<T> implements Column<T> {
    private final String name;
    private final MacrosType<T> type;
    private final boolean editable;
    private final boolean nullable;
    private final Supplier<T> defaultValue;

    public ColumnImpl(String str, MacrosType<T> t, boolean editable, boolean nullable, Supplier<T> defaultValue) {
        this.name = str;
        this.type = t;
        this.editable = editable;
        this.nullable = nullable;
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean isUserEditable() {
        return editable;
    }

    @Override
    public String sqlName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public MacrosType<T> type() {
        return type;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public DataContainer<T> defaultData() {
        return new DataContainer<>(type(), defaultValue.get());
    }

    @Override
    public List<Validation> getValidations() {
        // TODO
        return new ArrayList<>();
    }
}
