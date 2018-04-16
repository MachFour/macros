package com.machfour.macros.data;

import com.machfour.macros.util.DateStamp;

// basic types corresponding roughly to database types
public class MacrosType<T> {
    public static final MacrosType<Boolean> BOOLEAN = new MacrosType<>("boolean", Boolean.class);
    public static final MacrosType<Long> ID = new MacrosType<>("id", Long.class);
    public static final MacrosType<Long> INTEGER = new MacrosType<>("integer", Long.class);
    public static final MacrosType<Double> REAL = new MacrosType<>("real", Double.class);
    public static final MacrosType<String> TEXT = new MacrosType<>("text", String.class);
    public static final MacrosType<Long> TIMESTAMP = new MacrosType<>("timestamp", Long.class);
    public static final MacrosType<DateStamp> DATESTAMP = new MacrosType<>("datestamp", DateStamp.class);
    private final String name;
    private final Class<T> javaClass;

    private MacrosType(String name, Class<T> javaClass) {
        this.name = name;
        this.javaClass = javaClass;
    }

    public Class<T> javaClass() {
        return javaClass;
    }

    @Override
    public String toString() {
        return name;
    }
}
