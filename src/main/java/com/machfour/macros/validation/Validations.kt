package com.machfour.macros.validation

import com.machfour.macros.core.Column
import com.machfour.macros.util.DateStamp

object Validations {
    val REQUIRED_FIELD = RequiredField()
    val POSITIVE = Positive()
    val INTEGRAL = Integral()
    val NUMERIC = Numeric()
    val DATE = Date()
    val UNIQUE = Unique()

    class RequiredField : Validation {
        override fun <M> validate(stringValues: Map<Column<M, *>, String?>, field: Column<M, *>): Boolean {
            return "" != stringValues[field]
        }
    }

    class Numeric : Validation {
        override fun <M> validate(stringValues: Map<Column<M, *>, String?>, field: Column<M, *>): Boolean {
            return stringValues[field]?.toDoubleOrNull() != null
        }
    }

    class Positive : Validation {
        override fun <M> validate(stringValues: Map<Column<M, *>, String?>, field: Column<M, *>): Boolean {
            return NUMERIC.validate(stringValues, field) && java.lang.Double.valueOf(stringValues[field]) > 0
        }
    }

    class Date : Validation {
        override fun <M> validate(stringValues: Map<Column<M, *>, String?>, field: Column<M, *>): Boolean {
            val dateString = stringValues[field] ?: return false;
            return try {
                DateStamp.fromIso8601String(dateString)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }
    }

    class Integral : Validation {
        override fun <M> validate(stringValues: Map<Column<M, *>, String?>, field: Column<M, *>): Boolean {
            return try {
                java.lang.Long.valueOf(stringValues[field])
                true
            } catch (e: NumberFormatException) {
                // allow blank strings, since those should be validated by REQUIRED_FIELD
                "" == stringValues[field]
            }
        }
    }

    class Unique : Validation {
        override fun <M> validate(stringValues: Map<Column<M, *>, String?>, field: Column<M, *>): Boolean {
            //TODO
            return true
        }
    }

}