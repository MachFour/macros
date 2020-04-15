package com.machfour.macros.core;

import com.machfour.macros.validation.ValidationError;

import java.util.*;

public abstract class BaseTable<M> implements Table<M> {
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

    BaseTable(String name, Factory<M> factory,
            Column<M, Long> id, Column<M, Long> createTime, Column<M, Long> modTime, List<Column<M, ?>> otherCols) {
        this.name = name;
        this.factory = factory;
        idColumn = id;
        createTimeColumn = createTime;
        modifyTimeColumn = modTime;

        List<Column<M, ?>> cols = new ArrayList<>(otherCols.size() + 3);
        cols.add(id);
        cols.add(createTime);
        cols.add(modTime);
        cols.addAll(otherCols);
        columns = Collections.unmodifiableList(cols);

        Column<M, ?> naturalKeyColumn = null;
        // make name map and secondary key cols list. Linked hash map keeps insertion order
        Map<String, Column<M, ?>> columnsByName = new LinkedHashMap<>(cols.size(), 1);
        List<Column<M, ?>> secondaryKeyCols = new ArrayList<>(2);
        List<Column.Fk<M, ?, ?>> fkColumns = new ArrayList<>(2);

        int index = 0;
        for (Column<M, ?> c : cols) {
            columnsByName.put(c.sqlName(), c);
            c.setIndex(index);
            if (c.inSecondaryKey()) {
                secondaryKeyCols.add(c);
            }
            // record secondary key
            if (c.isUnique() && !c.equals(id)) {
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
