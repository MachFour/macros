package com.machfour.macros.validation;

import com.machfour.macros.data.Column;
import com.machfour.macros.util.DateStamp;

import java.util.Map;

public final class Validations {
    public static final Validation REQUIRED_FIELD;
    public static final Validation POSITIVE;
    public static final Validation INTEGRAL;
    public static final Validation NUMERIC;
    public static final Validation DATE;
    public static final Validation UNIQUE;

    static {
        REQUIRED_FIELD = new RequiredField();
        POSITIVE = new Positive();
        INTEGRAL = new Integral();
        NUMERIC = new Numeric();
        DATE = new Date();
        UNIQUE = new Unique();
    }

    private Validations() {
    }

    private static class RequiredField implements Validation {
        @Override
        public boolean validate(Map<Column, String> stringValues, Column field) {
            return !("".equals(stringValues.get(field)));
        }
    }

    private static class Numeric implements Validation {
        @Override
        public boolean validate(Map<Column, String> stringValues, Column field) {
            try {
                Double.valueOf(stringValues.get(field));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    private static class Positive implements Validation {
        @Override
        public boolean validate(Map<Column, String> stringValues, Column field) {
            return NUMERIC.validate(stringValues, field) &&
                Double.valueOf(stringValues.get(field)) > 0;
        }

    }

    private static class Date implements Validation {
        @Override
        public boolean validate(Map<Column, String> stringValues, Column field) {
            try {
                DateStamp.fromIso8601String(stringValues.get(field));
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    private static class Integral implements Validation {
        @Override
        public boolean validate(Map<Column, String> stringValues, Column field) {
            try {
                Long.valueOf(stringValues.get(field));
                return true;
            } catch (NumberFormatException e) {
                // allow blank strings, since those should be validated by REQUIRED_FIELD
                return "".equals(stringValues.get(field));
            }
        }

    }

    private static class Unique implements Validation {
        @Override
        public boolean validate(Map<Column, String> stringValues, Column field) {
            //TODO
            return true;
        }
    }

}
