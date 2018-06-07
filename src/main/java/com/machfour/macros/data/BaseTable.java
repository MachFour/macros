package com.machfour.macros.data;

import com.machfour.macros.core.ObjectSource;

import java.util.*;

public abstract class BaseTable<M> implements Table<M> {
    private final String name;
    private final List<Column<M, ?>> columns;
    // can't define this as a List<Column.Fk<M, ?, ?>> due to generics issues
    private final List<Column<M, ?>> fkColumns;
    private final List<Column<M, ?>> secondaryKeyCols;
    private final Map<String, Column<M, ?>> columnsByName;
    private final Column<M, Long> idColumn;
    private final Column<M, Long> createTimeColumn;
    private final Column<M, Long> modifyTimeColumn;

    BaseTable(String tblName, Column<M, Long> id, Column<M, Long> createTime, Column<M, Long> modTime, List<Column<M, ?>> otherCols) {
        name = tblName;
        idColumn = id;
        createTimeColumn = createTime;
        modifyTimeColumn = modTime;

        List<Column<M, ?>> cols = new ArrayList<>(otherCols.size() + 3);
        cols.add(id);
        cols.add(createTime);
        cols.add(modTime);
        cols.addAll(otherCols);
        columns = Collections.unmodifiableList(cols);

        // make name map and secondary key cols list
        Map<String, Column<M, ?>> columnsByName = new HashMap<>(cols.size(), 1);
        List<Column<M, ?>> secondaryKeyCols = new ArrayList<>(2);
        List<Column<M, ?>> fkColumns = new ArrayList<>(2);

        for (Column<M, ?> c : cols) {
            columnsByName.put(c.sqlName(), c);
            if (c.inSecondaryKey()) {
                secondaryKeyCols.add(c);
            }
            if (c instanceof Column.Fk) {
                fkColumns.add(c);
            }
        }
        this.columnsByName = Collections.unmodifiableMap(columnsByName);
        this.secondaryKeyCols = Collections.unmodifiableList(secondaryKeyCols);
        this.fkColumns = Collections.unmodifiableList(fkColumns);
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
    public List<Column<M, ?>> fkColumns() {
        return fkColumns;
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
    public abstract M construct(ColumnData<M> dataMap, ObjectSource objectSource);
    @Override
    public Column<M, ?> columnForName(String name) {
        return columnsByName.getOrDefault(name, null);
    }
}
