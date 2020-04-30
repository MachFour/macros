package com.machfour.macros.core;

import java.util.*;

abstract class BaseTable<M> implements Table<M> {
    private final String name;
    private final Factory<M> factory;
    private final List<Column<M, ?>> columns;
    private final List<Column.Fk<M, ?, ?>> fkColumns;
    private final List<Column<M, ?>> secondaryKeyCols;
    private final Map<String, Column<M, ?>> columnsByName;
    private final Column<M, Long> idColumn;
    private final Column<M, Long> createTimeColumn;
    private final Column<M, Long> modifyTimeColumn;
    private final Column<M, ?> naturalKeyColumn;

    // first three columns must be ID, create time, modify time
    BaseTable(String name, Factory<M> factory, List<Column<M, ?>> cols) {
        this.name = name;
        this.factory = factory;

        // TODO make this better
        this.idColumn = (Column<M, Long>)cols.get(0);
        this.createTimeColumn = (Column<M, Long>)cols.get(1);
        this.modifyTimeColumn = (Column<M, Long>)cols.get(2);

        assert Schema.ID_COLUMN_NAME.equals(this.idColumn.sqlName());
        assert Schema.CREATE_TIME_COLUMN_NAME.equals(this.createTimeColumn.sqlName());
        assert Schema.MODIFY_TIME_COLUMN_NAME.equals(this.modifyTimeColumn.sqlName());


        this.columns = Collections.unmodifiableList(cols);

        Column<M, ?> naturalKeyColumn = null;
        // make name map and secondary key cols list. Linked hash map keeps insertion order
        Map<String, Column<M, ?>> columnsByName = new LinkedHashMap<>(cols.size(), 1);
        List<Column<M, ?>> secondaryKeyCols = new ArrayList<>(2);
        List<Column.Fk<M, ?, ?>> fkColumns = new ArrayList<>(2);

        int index = 0;
        for (Column<M, ?> c : cols) {
            setTable(c, index);

            columnsByName.put(c.sqlName(), c);
            if (c.inSecondaryKey()) {
                secondaryKeyCols.add(c);
            }
            // record secondary key
            if (c.isUnique() && !c.equals(this.idColumn)) {
                assert naturalKeyColumn == null : "two natural keys defined";
                naturalKeyColumn = c;
            }
            checkAndAddFk(fkColumns, c);
            index++;
        }
        this.columnsByName = Collections.unmodifiableMap(columnsByName);
        this.secondaryKeyCols = Collections.unmodifiableList(secondaryKeyCols);
        this.fkColumns = Collections.unmodifiableList(fkColumns);
        this.naturalKeyColumn = naturalKeyColumn;
    }

    @SuppressWarnings("unchecked")
    // There's no reason for this check to fail, as the parameter M is guaranteed to match,
    // and everything else is a wildcard
    private static <M> void checkAndAddFk(List<Column.Fk<M, ?, ?>> fkCols, Column<M, ?> c) {
        if (c instanceof Column.Fk) {
            Column.Fk<M, ?, ?> fk = (Column.Fk<M, ?, ?>) c;
            fkCols.add(fk);
        }
    }

    // This package only uses ColumnImpl objects so we're good
    @SuppressWarnings("unchecked")
    private void setTable(Column<M, ?> c, int index) {
        assert c instanceof ColumnImpl;
        ColumnImpl<M, ?> impl = (ColumnImpl<M, ?>) c;
        impl.setTable(this);
    }


    @Override
    public Factory<M> getFactory() {
        return factory;
    }
    @Override
    public Column<M, Long> getIdColumn() {
        return idColumn;
    }
    @Override
    public Column<M, Long> getCreateTimeColumn() {
        return createTimeColumn;
    }
    @Override
    public Column<M, Long> getModifyTimeColumn() {
        return modifyTimeColumn;
    }
    @Override
    public String name() {
        return name;
    }
    @Override
    public List<Column<M, ?>> columns() {
        return columns;
    }
    @Override
    public List<Column.Fk<M, ?, ?>> fkColumns() {
        return fkColumns;
    }
    @Override
    public Column<M, ?> getNaturalKeyColumn() {
        return naturalKeyColumn;
    }
    @Override
    public Map<String, Column<M, ?>> columnsByName() {
        return columnsByName;
    }
    @Override
    public List<Column<M, ?>> getSecondaryKeyCols() {
        return secondaryKeyCols;
    }
    @Override
    public Column<M, ?> columnForName(String name) {
        return columnsByName.getOrDefault(name, null);
    }
}
