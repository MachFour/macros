package com.machfour.macros.data;

import com.machfour.macros.core.ObjectSource;

import java.util.*;

public abstract class BaseTable<M> implements Table<M> {
    private final String name;
    private final List<Column<M, ?>> columns;
    private final List<Column<M, ?>> secondaryKey;
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
        Map<String, Column<M, ?>> colsByName = new HashMap<>(cols.size(), 1);
        List<Column<M, ?>> secondaryKeyCols = new ArrayList<>(2);
        for (Column<M, ?> c : cols) {
            colsByName.put(c.sqlName(), c);
            if (c.inSecondaryKey()) {
                secondaryKeyCols.add(c);
            }
        }
        columnsByName = Collections.unmodifiableMap(colsByName);
        secondaryKey = Collections.unmodifiableList(secondaryKeyCols);
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
    public Map<String, Column<M, ?>> columnsByName() {
        return columnsByName;
    }
    @Override
    public List<Column<M, ?>> getSecondaryKey() {
        return secondaryKey;
    }
    @Override
    public abstract M construct(ColumnData<M> dataMap, ObjectSource objectSource);
    @Override
    public Column<M, ?> columnForName(String name) {
        return columnsByName.getOrDefault(name, null);
    }
}
