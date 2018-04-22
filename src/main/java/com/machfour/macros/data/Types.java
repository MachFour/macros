package com.machfour.macros.data;

import com.machfour.macros.core.Ingredient;
import com.machfour.macros.util.DateStamp;

// basic types corresponding roughly to database types
public class Types {
    public static final Bool BOOLEAN = new Bool();
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
    }

    private static Long ObjectToLong(Object data) {
        Long converted;
        if (data == null) {
            converted = null;
        } else if (data instanceof Long) {
            converted = (Long) data;
        } else if (data instanceof Integer) {
            converted = Integer.toUnsignedLong((Integer)data);
        } else {
            try {
                converted = Long.parseLong(data.toString());
            } catch (NumberFormatException e) {
                throw new ClassCastException("Cannot convert raw Object to Long");
            }
        }
        return converted;
    }

    public static final class Id implements MacrosType<Long> {
        @Override
        public String toString() {
            return "id";
        }
        @Override
        public Long fromRaw(Object data) {
            return ObjectToLong(data);
        }
    }

    public static final class Int implements MacrosType<Long> {
        @Override
        public String toString() {
            return "integer";
        }
        @Override
        public Long fromRaw(Object data) {
            return ObjectToLong(data);
        }
    }

    public static final class Real implements MacrosType<Double> {
        @Override
        public String toString() {
            return "real";
        }
    }

    public static final class Text implements MacrosType<String> {
        @Override
        public String toString() {
            return "text";
        }
    }

    public static final class Time implements MacrosType<Long> {
        @Override
        public String toString() {
            return "time";
        }
        @Override
        public Long fromRaw(Object data) {
            return ObjectToLong(data);
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
            return DateStamp.fromIso8601String(raw.toString());
        }
        @Override
        public Object toRaw(DateStamp data) {
            return data.toString();
        }
    }
}
