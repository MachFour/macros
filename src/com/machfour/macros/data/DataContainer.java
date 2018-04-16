package com.machfour.macros.data;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.Objects;

/*
 * Class to hold a single piece of data (of any type) from the database.
 * This allows objects to hold a single map (from Column to DataContainer)
 * containing everything. It also allows querying data by the column object, rather
 * than having to know the instance variable sqlName
 */
public class DataContainer<T> {

    @NotNull
    public final MacrosType type;
    @Nullable
    public final T data;

    public DataContainer(@NotNull MacrosType<T> t, @Nullable T o) {
        type = t;
        data = o;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DataContainer) {
            DataContainer d = (DataContainer) o;
            return Objects.equals(type, d.type) && Objects.equals(data, d.data);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, data);
    }


}
