package com.machfour.macros.data;

import com.machfour.macros.util.Supplier;
import com.machfour.macros.validation.Validation;

import java.util.ArrayList;
import java.util.List;

public class ColumnImpl<M, T extends MacrosType<J>, J> implements Column<M, T, J> {
    private final String name;
    private final T type;
    private final int index;
    private final boolean editable;
    private final boolean nullable;
    private final Supplier<J> defaultValue;

    public ColumnImpl(String str, T t, int index, boolean editable, boolean nullable, Supplier<J> defaultValue) {
        this.name = str;
        this.type = t;
        this.index = index;
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
    public int index() {
        return index;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public T type() {
        return type;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public J defaultData() {
        return defaultValue.get();
    }

    @Override
    public List<Validation> getValidations() {
        // TODO
        return new ArrayList<>();
    }
}
