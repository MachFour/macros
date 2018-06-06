package com.machfour.macros.data;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public interface MacrosType<J> {

    // These methods perform type-specific conversion is necessary
    // if raw is null then null will be returned
    J fromRaw(@Nullable Object raw);
    J fromString(@NotNull String stringData);

    default Object toRaw(J data) {
        return data;
    }

    default J cast(Object o) {
        return javaClass().cast(o);
    }

    // TODO this doesn't need to be a public method
    Class<J> javaClass();

    @Override
    String toString();
}
