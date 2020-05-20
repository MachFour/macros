package com.machfour.macros.core;

import com.machfour.macros.core.datatype.MacrosType;
import com.machfour.macros.util.Supplier;
import com.machfour.macros.validation.Validation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class ColumnImpl<M, J> implements Column<M, J> {
    private final String name;
    private final boolean editable;
    private final boolean nullable;
    private final boolean inSecondaryKey;
    private final boolean unique;
    private final MacrosType<J> type;
    private final Supplier<J> defaultValue;

    // These are set later, when added to Table
    private Table<M> table;
    private int index;

    private ColumnImpl(String name, MacrosType<J> type, @NotNull Supplier<J> defaultValue,
            boolean editable, boolean nullable, boolean inSecondaryKey, boolean unique) {
        this.name = name;
        this.type = type;
        this.editable = editable;
        this.nullable = nullable;
        this.inSecondaryKey = inSecondaryKey;
        this.unique = unique;
        this.defaultValue = defaultValue;

        this.table = null; // unset
        this.index = -1; // unset
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
    public Table<M> getTable() {
        return table;
    }

    // These methods are used by BaseTable

    void setTable(@NotNull Table<M> table) {
        assert this.table == null : "Table already set";
        this.table = table;
    }

    void setIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        assert this.index == -1 : "Index already set";
        this.index = index;
    }


    @Override
    public String toString() {
        return name;
    }
    @Override
    public boolean isNullable() {
        return nullable;
    }
    @Override
    public boolean inSecondaryKey() {
        return inSecondaryKey;
    }
    @Override
    public boolean isUnique() {
        return unique;
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

    @Override
    public MacrosType<J> getType() {
        return type;
    }

    static final class Fk<M, J, N> extends ColumnImpl<M, J> implements Column.Fk<M, J, N> {
        private final Column<N, J> parent;
        private final Table<N> parentTable;

        private Fk(String name, MacrosType<J> type, @NotNull Supplier<J> defaultValue, boolean editable,
                   boolean nullable, boolean inSecondaryKey, boolean unique, Column<N, J> parent, Table<N> parentTable) {
            super(name, type, defaultValue, editable, nullable, inSecondaryKey, unique);
            this.parent = parent;
            this.parentTable = parentTable;
        }

        @Override
        public Column<N, J> getParentColumn() {
            return parent;
        }
        @Override
        public Table<N> getParentTable() {
            return parentTable;
        }
        @Override
        public String toString() {
            return super.toString() + " (-> " + parentTable.name() + "." + parent.sqlName() + ")";
        }
    }

    static final class Builder<J> {
        private final String name;
        private final MacrosType<J> type;
        private boolean editable;
        private boolean nullable;
        private boolean inSecondaryKey;
        private boolean unique;
        private Supplier<J> defaultValue;

        Builder(String name, MacrosType<J> type) {
            assert (name != null);
            this.name = name;
            this.type = type;
            this.editable = true;
            this.nullable = true;
            this.inSecondaryKey = false;
            this.unique = false;
            this.defaultValue = () -> null;
        }
        Builder<J> notEditable() {
            this.editable = false;
            return this;
        }
        Builder<J> notNull() {
            this.nullable = false;
            return this;
        }
        Builder<J> inSecondaryKey() {
            this.inSecondaryKey = true;
            return this;
        }
        Builder<J> unique() {
            this.unique = true;
            return this;
        }

        Builder<J> defaultsTo(J value) {
            this.defaultValue = () -> value;
            return this;
        }
        private <M> ColumnImpl<M, J> build() {
            return new ColumnImpl<>(name, type, defaultValue, editable, nullable, inSecondaryKey, unique);
        }
        private <M, N> Fk<M, J, N> buildFk(Column<N, J> parent, Table<N> parentTable) {
            // not sure why the constructor call needs type parameters here...
            return new Fk<M, J, N>(name, type, defaultValue, editable, nullable, inSecondaryKey, unique, parent, parentTable);
        }

        private static <M> void addToListAndSetIndex(ColumnImpl<M, ?> newlyCreated, @NotNull List<Column<M, ?>> columns) {
            newlyCreated.setIndex(columns.size());
            columns.add(newlyCreated);
        }

        // sets index
        <M> ColumnImpl<M, J> buildAndAdd(@NotNull List<Column<M, ?>> columnList) {
            ColumnImpl<M, J> builtCol = build();
            addToListAndSetIndex(builtCol, columnList);
            return builtCol;
        }

        <M, N> Fk<M, J, N> buildAndAddFk(Column<N, J> parent, Table<N> parentTable, @NotNull List<Column<M, ?>> columnList) {
            Fk<M, J, N> builtCol = buildFk(parent, parentTable);
            addToListAndSetIndex(builtCol, columnList);
            return builtCol;
        }
    }
}
