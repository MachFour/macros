package com.machfour.macros.core.datatype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MacrosType<J> {

    // These methods perform type-specific conversion is necessary
    // if raw is null then null will be returned
    J fromRaw(@Nullable Object raw) throws TypeCastException;
    J fromString(@NotNull String stringData) throws TypeCastException;

    // This returns the data in a form that is able to be inserted into a database
    // for SQLite, this means, for example, that booleans become integers.
    default Object toRaw(J data) {
        return data;
    }

    default String toRawString(J data) {
        return toRawString(data, "");
    }

    default String toSqlString(J data) {
        return toRawString(data, "NULL");
    }

    default String toRawString(J data, String nullString) {
        // can't use Objects.toString() cause of Android API
        return data == null ? nullString : String.valueOf(toRaw(data));
    }

    default J cast(Object o) {
        return javaClass().cast(o);
    }

    // TODO this doesn't need to be a public method
    Class<J> javaClass();

    SqliteType sqliteType();

    @Override
    @NotNull
    String toString();
}
