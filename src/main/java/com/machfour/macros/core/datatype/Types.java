package com.machfour.macros.core.datatype;

import com.machfour.macros.util.DateStamp;
import com.machfour.macros.util.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    public static class Bool extends MacrosTypeImpl<Boolean> {
        private static final Set<String> truthyStrings;
        private static final Set<String> falseyStrings;

        static {
            // TODO internationalisation lol
            List<String> truthyStringsList = Arrays.asList("true", "t", "yes", "y", "1");
            List<String> falseyStringsList = Arrays.asList("false", "f", "no", "n", "0");

            truthyStrings = Collections.unmodifiableSet(new HashSet<>(truthyStringsList));
            falseyStrings = Collections.unmodifiableSet(new HashSet<>(falseyStringsList));
        }

        @Override @NotNull
        public String toString() {
            return "boolean";
        }

        @Override
        public @NotNull Boolean fromRawNotNull(@NotNull Object raw) throws TypeCastException {
            if (raw instanceof Boolean) {
                return (Boolean) raw;
            } else {
                return fromNonEmptyString(raw.toString());
            }
        }
        @Override
        public @NotNull Boolean fromNonEmptyString(@NotNull String boolString) throws TypeCastException {
            // TODO internationalisation...
            boolean truthy = truthyStrings.contains(boolString.toLowerCase());
            boolean falsey = falseyStrings.contains(boolString.toLowerCase());
            assert !(truthy && falsey); // can't be both (bad programming)

            if (!truthy && !falsey) { // if neither then it's a user problem
                throw new TypeCastException("Cannot convert string '" + boolString + "' to boolean");
            } else {
                return truthy;
            }
        }
        @Override
        public Class<Boolean> javaClass() {
            return Boolean.class;
        }
        // SQLite doesn't have a boolean type, so we return int
        @Override
        public Object toRaw(Boolean data) {
            if (data == null) {
                return null;
            } else {
                return data ? 1L : 0L;
            }
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
        @NotNull
        public String toString() {
            return "null-boolean";
        }

        @Override
        public Boolean fromRaw(Object raw) throws TypeCastException {
            if (raw == null) {
                return false;
            } else {
                return super.fromRawNotNull(raw);
            }
        }

        @Override
        // return 1 (as long) if data is true, or null otherwise
        public Object toRaw(Boolean data) {
            return (data != null && data) ? 1L : null;
        }
    }
    public static final class Id extends MacrosTypeImpl<Long> {
        @Override
        public @NotNull String toString() {
            return "id";
        }

        @Override
        @NotNull
        public Long fromNonEmptyString(@NotNull String stringData) throws TypeCastException {
            return stringToLong(stringData);
        }
        @Override
        @NotNull
        public Long fromRawNotNull(@NotNull Object data) throws TypeCastException {
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

    public static final class Int extends MacrosTypeImpl<Long> {
        @Override
        public @NotNull String toString() {
            return "integer";
        }
        @Override
        @NotNull
        public Long fromRawNotNull(@NotNull Object data) throws TypeCastException {
            return objectToLong(data);
        }
        @Override
        public @NotNull Long fromNonEmptyString(@NotNull String stringData) throws TypeCastException {
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

    public static final class Real extends MacrosTypeImpl<Double> {
        @Override
        @NotNull
        public String toString() {
            return "real";
        }

        @Override
        @NotNull
        public Double fromNonEmptyString(@NotNull String doubleString) throws TypeCastException {
            try {
                return Double.parseDouble(doubleString);
            } catch (NumberFormatException e) {
                throw new TypeCastException("Cannot convert string '" + doubleString + "' to Double");
            }
        }
        @Override
        @NotNull
        public Double fromRawNotNull(@NotNull Object raw) throws TypeCastException {
            if (raw instanceof Double) {
                return (Double) raw;
            } else {
                return fromNonEmptyString(raw.toString());
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

    public static final class Text extends MacrosTypeImpl<String> {
        @Override
        @NotNull
        public String toString() {
            return "text";
        }

        @Override
        @NotNull
        public String fromNonEmptyString(@NotNull String stringData) {
            return stringData;
        }

        @Override
        @NotNull
        public String fromRawNotNull(@NotNull Object raw) {
            return raw.toString();
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

    public static final class Time extends MacrosTypeImpl<Long> {
        @Override
        @NotNull
        public String toString() {
            return "time";
        }

        @Override
        @NotNull
        public Long fromRawNotNull(@NotNull Object data) throws TypeCastException {
            return objectToLong(data);
        }
        @Override
        public @NotNull Long fromNonEmptyString(@NotNull String stringData) throws TypeCastException {
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

    public static final class Date extends MacrosTypeImpl<DateStamp> {
        @Override
        @NotNull
        public String toString() {
            return "date";
        }

        @Override
        @NotNull
        public DateStamp fromRawNotNull(@NotNull Object raw) throws TypeCastException {
            return fromNonEmptyString(raw.toString());
        }

        @Override
        @NotNull
        public DateStamp fromNonEmptyString(@NotNull String stringData) {
            return DateStamp.fromIso8601String(stringData);
        }

        @Nullable
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

    private static Long stringToLong(@NotNull String longString) throws TypeCastException {
        try {
            return Long.parseLong(longString);
        } catch (NumberFormatException e) {
            throw new TypeCastException("Cannot convert string '" + longString + "' to Long");
        }
    }

    private static long objectToLong(@NotNull Object data) throws TypeCastException {
        if (data instanceof Long) {
            // auto unboxing
            return (Long) data;
        } else if (data instanceof Integer) {
            // TODO API level: converted = Integer.toUnsignedLong((Integer)data);
            return MiscUtils.toSignedLong((Integer) data);
        } else {
            return stringToLong(String.valueOf(data));
        }
    }

}
