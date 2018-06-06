package com.machfour.macros.data;

import com.machfour.macros.util.Supplier;
import com.machfour.macros.validation.Validation;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ColumnImpl<M, J> implements Column<M, J> {
    private final String name;
    private final int index;
    private final boolean editable;
    private final boolean nullable;
    private final boolean inSecondaryKey;
    //private final Table<M> table;
    private final MacrosType<J> type;
    private final Supplier<J> defaultValue;

    private ColumnImpl(String name, MacrosType<J> type, //Table<M> table,
                       int index, boolean editable, boolean nullable, boolean inSecondaryKey, @NotNull Supplier<J> defaultValue) {
        this.name = name;
        this.type = type;
        //this.table = table;
        this.index = index;
        this.editable = editable;
        this.nullable = nullable;
        this.inSecondaryKey = inSecondaryKey;
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
    public boolean isNullable() {
        return nullable;
    }
    @Override
    public boolean inSecondaryKey() {
        return inSecondaryKey;
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

    public static final class ForeignKey<M, J, N> implements Column.ForeignKey<M, J, N> {
        private final Column<N, J> parent;
        private final Table<N> parentTable;
        private final Column<M, J> child;

        private ForeignKey(Column<M, J> child, Column<N, J> parent, Table<N> parentTable) {
            this.child = child;
            this.parent = parent;
            this.parentTable = parentTable;
        }
        @Override
        public String sqlName() {
            return child.sqlName();
        }

        @Override
        public J defaultData() {
            return child.defaultData();
        }

        @Override
        public MacrosType<J> getType() {
            return child.getType();
        }
        @Override
        public int index() {
            return child.index();
        }
        @Override
        public boolean isUserEditable() {
            return child.isUserEditable();
        }
        @Override
        public boolean isNullable() {
            return child.isNullable();
        }
        @Override
        public boolean inSecondaryKey() {
            return child.inSecondaryKey();
        }
        @Override
        public List<Validation> getValidations() {
            return child.getValidations();
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
            return child.sqlName() + "(-> " + parentTable.name() + "." + parent.sqlName() + ")";
        }
    }

    public static final class Builder<J> {
        private String name;
        private MacrosType<J> type;
        private int index;
        private boolean editable;
        private boolean nullable;
        private boolean inSecondaryKey;
        private Supplier<J> defaultValue;

        public Builder(String name, MacrosType<J> type, int index) {
            assert (name != null && index >= 0);
            this.name = name;
            this.type = type;
            this.index = index;
            this.editable = true;
            this.nullable = true;
            this.defaultValue = () -> null;
            this.inSecondaryKey = false;
        }
        public Builder<J> notEditable() {
            this.editable = false;
            return this;
        }
        public Builder<J> notNull() {
            this.nullable = false;
            return this;
        }
        public Builder<J> inSecondaryKey() {
            this.inSecondaryKey = true;
            return this;
        }
        public Builder<J> defaultValue(Supplier<J> defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        public Builder<J> defaultValue(J defaultValue) {
            this.defaultValue = () -> defaultValue;
            return this;
        }
        public <M> Column<M, J> build() {
            return new ColumnImpl<>(name, type, index, editable, nullable, inSecondaryKey, defaultValue);
        }
        public <M, N> Column.ForeignKey<M, J, N> buildFk(Column<N, J> parent, Table<N> parentTable) {
            return new ForeignKey<>(build(), parent, parentTable);
        }
    }
}
