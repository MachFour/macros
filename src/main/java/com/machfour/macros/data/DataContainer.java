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
public class DataContainer<T extends MacrosType<J>, J> implements Cloneable {

    @NotNull
    public final T type;
    @Nullable
    public final J data;

    public DataContainer(@NotNull T t, @Nullable J o) {
        type = t;
        data = o;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DataContainer
            && type.equals(((DataContainer) o).type)
            && Objects.equals(data, ((DataContainer) o).data);
    }

    @Override
    public String toString() {
        return String.valueOf(type) + "/" + String.valueOf(data);
    }
    // shallow clone
    @Override
    @SuppressWarnings("unchecked")
    public DataContainer<T, J> clone() {
        DataContainer<T, J> clone;
        try {
            clone = (DataContainer<T, J>) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new DataContainer<>(type, data);
        }
        return clone;
    }

    @Nullable
    public J getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, data);
    }


}
