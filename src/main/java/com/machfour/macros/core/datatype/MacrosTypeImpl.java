package com.machfour.macros.core.datatype;

import com.machfour.macros.util.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MacrosTypeImpl<J> implements MacrosType<J> {


    // gets the java class associated with this type
    abstract Class<J> javaClass();

    // this one does the real conversion work
    @NotNull
    protected abstract J fromNonEmptyString(String data) throws TypeCastException;

    // this one does the real conversion work
    @NotNull
    protected abstract J fromRawNotNull(@NotNull Object data) throws TypeCastException;

    // This would normally be final, but the 'NullBool' type needs to override this, to treat null as false
    @Nullable
    public J fromRaw(Object data) throws TypeCastException {
        return data == null ? null : fromRawNotNull(data);
    }

    @Override
    @Nullable
    // Empty strings are treated as null object
    // TODO rename to fromRawString?
    public final J fromRawString(@NotNull String data) throws TypeCastException {
        return data.isEmpty() ? fromRaw(null) : fromNonEmptyString(data);
    }

    // Returns a string representation suitable for saving into a textual format (e.g. CSV)
    // In particular, null data becomes empty strings
    @NotNull
    public final String toRawString(J data) {
        return toString(data, "");
    }

    // Returns a string representation suitable for use in issuing an SQL command to store the given data
    // into an SQL database. In particular, null data is converted into the string "NULL"
    @NotNull
    public final String toSqlString(J data) {
        return toString(data, "NULL");
    }

    // Returns a string representation of the given data, with null data represented by the string 'null'
    @NotNull
    public final String toString(J data) {
        return toString(data, "null");
    }

    @NotNull
    public final String toString(J data, String nullString) {
        // can't use Objects.toString() cause of Android API
        return MiscUtils.nullableToString(toRaw(data), nullString);
    }

    @Override
    public J cast(Object o) {
        return javaClass().cast(o);
    }

    // this is not anything to do with the data!
    @Override
    @NotNull
    public abstract String toString();
}
