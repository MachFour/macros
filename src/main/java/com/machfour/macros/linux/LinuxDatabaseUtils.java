package com.machfour.macros.linux;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;

import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

class LinuxDatabaseUtils {
    private LinuxDatabaseUtils() {}

    static <M> void bindData(PreparedStatement p, ColumnData<M> values, List<Column<M, ?>> orderedColumns) throws SQLException {
        bindData(p, values, orderedColumns, null);
    }

    static <M> void bindData(PreparedStatement p, ColumnData<M> values, List<Column<M, ?>> orderedColumns, @Nullable Object extra) throws SQLException {
        int colIndex = 1; // parameters are 1 indexed!
        for (Column<M, ?> col : orderedColumns) {
            // Internally, setObject() relies on a ladder of instanceof checks
            p.setObject(colIndex, values.getAsRaw(col));
            colIndex++;
        }
        if (extra != null) {
            p.setObject(colIndex, extra);
        }
    }

    static <E> void bindObjects(PreparedStatement p, Collection<E> objects) throws SQLException {
        int colIndex = 1; // parameters are 1 indexed!
        for (Object o : objects) {
            p.setObject(colIndex, o);
            colIndex++;
        }
    }
}
