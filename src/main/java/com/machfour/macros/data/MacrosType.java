package com.machfour.macros.data;

public interface MacrosType<J> {

    // These methods should be overridden if any type-specific conversion is necessary
    @SuppressWarnings("unchecked")
    default J fromRaw(Object raw) {
        return (J) raw;
    }
    default Object toRaw(J data) {
        return data;
    }

    @Override
    String toString();
}
