package com.machfour.macros.core.datatype;

import com.machfour.macros.core.datatype.MacrosType;
import com.machfour.macros.core.datatype.SqliteType;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;

// basic types corresponding roughly to database types
// TODO check that s.equals(fromString(s).toString()) for valid strings s, for each type
public class Types {
    public static final Bool BOOLEAN = new Bool();
    public static final NullBool NULLBOOLEAN = new NullBool();
    public static final Id ID = new Id();
    public static final Int INTEGER = new Int();
    public static final Real REAL = new Real();
    public static final Text TEXT = new Text();
    public static final Time TIMESTAMP = new Time();
    public static final Date DATESTAMP = new Date();

    public static class Bool implements MacrosType<Boolean> {
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
            return boolString.equalsIgnoreCase("true")
                || boolString.equalsIgnoreCase("yes")
                || boolString.equals("1");
        }
        @Override
        public Class<Boolean> javaClass() {
            return Boolean.class;
        }
        @Override
        public SqliteType sqliteType() {
            return SqliteType.INTEGER;
        }
    }
    // Boolean type where null means false. This is a hack used to ensure there's only one default serving per food,
    // using a UNIQUE check on (food_id, is_default)
    public static final class NullBool extends Bool {
        @Override
        public String toString() {
            return "null-boolean";
        }
        @Override
        public Boolean fromRaw(Object raw) {
            return raw == null ? false : super.fromRaw(raw);
        }
        @Override
        public Boolean fromString(@NotNull String boolString) {
            return boolString.equals("") ? false : super.fromString(boolString);
        }
        @Override
        // return true if data is true, or null otherwise
        public Object toRaw(Boolean data) {
            return (data != null && data) ? true : null;
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
        @Override
        public SqliteType sqliteType() {
            return SqliteType.INTEGER;
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
        @Override
        public SqliteType sqliteType() {
            return SqliteType.INTEGER;
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
        @Override
        public SqliteType sqliteType() {
            return SqliteType.REAL;
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
        @Override
        public SqliteType sqliteType() {
            return SqliteType.TEXT;
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
        @Override
        public SqliteType sqliteType() {
            return SqliteType.INTEGER;
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
        @Override
        public SqliteType sqliteType() {
            return SqliteType.TEXT;
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
            // TODO API level: converted = Integer.toUnsignedLong((Integer)data);
            converted = Long.parseLong(data.toString());
        } else {
            return stringToLong(String.valueOf(data));
        }
        return converted;
    }

}
