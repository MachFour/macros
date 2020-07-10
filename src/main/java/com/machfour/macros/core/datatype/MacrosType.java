package com.machfour.macros.core.datatype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MacrosType<J> {

    // These methods perform type-specific conversion is necessary
    // if raw is null then null will be returned
    @Nullable
    J fromRaw(@Nullable Object raw) throws TypeCastException;

    @Nullable
    // tries to convert the given string representation into the desired type
    // Empty strings will return the result of fromRaw(null)
    // This method will never return null if the string is non-empty
    @Deprecated // Use fromRawString instead
    default J fromString(@NotNull String stringData) throws TypeCastException {
        return fromRawString(stringData);
    }

    @Nullable
    // tries to convert the given string representation into the desired type
    // Empty strings will return the result of fromRaw(null)
    // This method will never return null if the string is non-empty
    J fromRawString(@NotNull String stringData) throws TypeCastException;

    // This returns the data in a form that is able to be inserted into a database
    // for SQLite, this means, for example, that booleans become integers.
    default Object toRaw(J data) {
        return data;
    }

    // Returns a string representation suitable for saving into a textual format (e.g. CSV)
    // In particular, null data becomes empty strings
    @NotNull
    String toRawString(J data);

    // Returns a string representation suitable for use in issuing an SQL command to store the given data
    // into an SQL database. In particular, null data is converted into the string "NULL"
    @NotNull
    String toSqlString(J data);

    // Returns a string representation of the given data, with null data represented by the string 'null'
    @NotNull
    String toString(J data);

    // Returns a string representation of the given data, with a custom placeholder as null
    @NotNull
    String toString(J data, String nullString);

    // A dumb Java cast from the given object to the Java class associated with this Type
    J cast(Object o);

    // used in Android database code
    SqliteType sqliteType();

}
