package com.machfour.macros.data;

import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.NotNull;

// basic types corresponding roughly to database types
public class Types {
    public static final Bool BOOLEAN = new Bool();
    public static final NullBool NULLBOOLEAN = new NullBool();
    public static final Id ID = new Id();
    public static final Int INTEGER = new Int();
    public static final Real REAL = new Real();
    public static final Text TEXT = new Text();
    public static final Time TIMESTAMP = new Time();
    public static final Date DATESTAMP = new Date();

    public static final class Bool implements MacrosType<Boolean> {
        @Override
        public String toString() {
            return "boolean";
        }
        @Override
        public Boolean fromRaw(Object raw) {
            if (raw == null) {
                return null;
            }
            else if (raw instanceof Boolean) {
                return (Boolean) raw;
            } else {
                return fromString(raw.toString());
            }
        }
        @Override
        public Boolean fromString(@NotNull String boolString) {
            return Boolean.parseBoolean(boolString);
        }
        @Override
        public Class<Boolean> javaClass() {
            return Boolean.class;
        }
    }
    // Boolean type where null means false. This is a hack used to ensure there's only one default serving per food,
    // using a UNIQUE check on (food_id, is_default)
    public static final class NullBool implements MacrosType<Boolean> {
        @Override
        public String toString() {
            return "null-boolean";
        }
        @Override
        public Boolean fromRaw(Object raw) {
            if (raw == null) {
                return false;
            }
            else if (raw instanceof Boolean) {
                return (Boolean) raw;
            } else {
                return fromString(raw.toString());
            }
        }
        @Override
        public Boolean fromString(@NotNull String boolString) {
            return Boolean.parseBoolean(boolString);
        }
        @Override
        // return true if data is true, or null otherwise
        public Object toRaw(Boolean data) {
            return (data != null && data) ? true : null;
        }

        @Override
        public Class<Boolean> javaClass() {
            return Boolean.class;
        }
    }
    public static final class Id implements MacrosType<Long> {
        @Override
        public String toString() {
            return "id";
        }
        @Override
        public Long fromString(@NotNull String stringData) {
            return stringToLong(stringData);
        }
        @Override
        public Long fromRaw(Object data) {
            return objectToLong(data);
        }
        @Override
        public Class<Long> javaClass() {
            return Long.class;
        }
    }

    public static final class Int implements MacrosType<Long> {
        @Override
        public String toString() {
            return "integer";
        }
        @Override
        public Long fromRaw(Object data) {
            return objectToLong(data);
        }
        @Override
        public Long fromString(@NotNull String stringData) {
            return stringToLong(stringData);
        }
        @Override
        public Class<Long> javaClass() {
            return Long.class;
        }
    }

    public static final class Real implements MacrosType<Double> {
        @Override
        public String toString() {
            return "real";
        }
        @Override
        public Double fromString(@NotNull String doubleString) {
            try {
                return Double.parseDouble(doubleString);
            } catch (NumberFormatException e) {
                throw new ClassCastException("Cannot convert string '" + doubleString + "' to Double");
            }
        }
        @Override
        public Double fromRaw(Object raw) {
            if (raw == null) {
                return null;
            } else if (raw instanceof Double) {
                return (Double) raw;
            } else {
                return fromString(raw.toString());
            }
        }
        @Override
        public Class<Double> javaClass() {
            return Double.class;
        }
    }

    public static final class Text implements MacrosType<String> {
        @Override
        public String toString() {
            return "text";
        }
        @Override
        public String fromString(@NotNull String stringData) {
            return stringData;
        }
        @Override
        public String fromRaw(Object raw) {
            return raw == null ? null : raw.toString();
        }
        @Override
        public Class<String> javaClass() {
            return String.class;
        }
    }

    public static final class Time implements MacrosType<Long> {
        @Override
        public String toString() {
            return "time";
        }
        @Override
        public Long fromRaw(Object data) {
            return objectToLong(data);
        }
        @Override
        public Long fromString(@NotNull String stringData) {
            return stringToLong(stringData);
        }
        @Override
        public Class<Long> javaClass() {
            return Long.class;
        }
    }

    public static final class Date implements MacrosType<DateStamp> {
        @Override
        public String toString() {
            return "date";
        }
        // convert from string
        @Override
        public DateStamp fromRaw(Object raw) {
            return raw == null ? null : fromString(raw.toString());
        }
        @Override
        public DateStamp fromString(@NotNull String stringData) {
            return DateStamp.fromIso8601String(stringData);
        }
        public Object toRaw(DateStamp data) {
            return data.toString();
        }
        @Override
        public Class<DateStamp> javaClass() {
            return DateStamp.class;
        }
    }

    private static Long stringToLong(@NotNull String longString) {
        try {
            return Long.parseLong(longString);
        } catch (NumberFormatException e) {
            throw new ClassCastException("Cannot convert string '" + longString + "' to Long");
        }
    }

    private static Long objectToLong(Object data) {
        Long converted;
        if (data == null) {
            converted = null;
        } else if (data instanceof Long) {
            converted = (Long) data;
        } else if (data instanceof Integer) {
            converted = Integer.toUnsignedLong((Integer)data);
        } else {
            return stringToLong(String.valueOf(data));
        }
        return converted;
    }

}
