package com.machfour.macros.core.datatype

import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.MiscUtils

// basic types corresponding roughly to database types
// TODO check that s.equals(fromString(s).toString()) for valid strings s, for each type
class Types {
    companion object {
        @JvmField
        val BOOLEAN = Bool()
        @JvmField
        val NULLBOOLEAN = NullBool()
        @JvmField
        val ID = Id()
        @JvmField
        val INTEGER = Int()
        @JvmField
        val REAL = Real()
        @JvmField
        val TEXT = Text()
        @JvmField
        val TIMESTAMP = Time()
        @JvmField
        val DATESTAMP = Date()

        @Throws(TypeCastException::class)
        internal fun stringToLong(longString: String): Long {
            return try {
                longString.toLong()
            } catch (e: NumberFormatException) {
                throw TypeCastException("Cannot convert string '$longString' to Long")
            }
        }

        @Throws(TypeCastException::class)
        internal fun objectToLong(data: Any): Long {
            return when (data) {
                is Long -> data // auto unbox
                is kotlin.Int -> MiscUtils.toUnsignedLong(data)
                else -> stringToLong(data.toString())
            }
        }
    }

    open class Bool : MacrosTypeImpl<Boolean>() {
        companion object {
            // TODO internationalisation lol
            private val truthyStrings = setOf("true", "t", "yes", "y", "1")
            private val falseyStrings = setOf("false", "f", "no", "n", "0")
        }

        override fun toString() = "boolean"

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): Boolean {
            return when (data) {
                is Boolean -> data
                else -> fromNonEmptyString(data.toString())
            }
        }

        @Throws(TypeCastException::class)
        override fun fromNonEmptyString(data: String): Boolean {
            // TODO internationalisation...
            val truthy = truthyStrings.contains(data.toLowerCase())
            val falsey = falseyStrings.contains(data.toLowerCase())

            // can't be both (bad programming)
            assert(!(truthy && falsey))

            // if neither then it's a user problem
            return if (!truthy && !falsey) {
                throw TypeCastException("Cannot convert string '$data' to boolean")
            } else {
                truthy
            }
        }

        override fun javaClass(): Class<Boolean> = Boolean::class.javaObjectType

        // SQLite doesn't have a boolean type, so we return int
        override fun toRaw(data: Boolean?): Any? {
            return when (data) {
                null -> null
                true -> 1L
                else -> 0L
            }
        }

        override fun sqliteType(): SqliteType = SqliteType.INTEGER
    }

    // Boolean type where null means false. This is a hack used to ensure there's only one default serving per food,
    // using a UNIQUE check on (food_id, is_default)
    class NullBool : Bool() {
        override fun toString(): String = "null-boolean"

        @Throws(TypeCastException::class)
        override fun fromRaw(data: Any?): Boolean? {
            return when (data) {
                null -> false
                else -> super.fromRawNotNull(data)
            }
        }

        // return 1 (as long) if data is true, or null otherwise
        override fun toRaw(data: Boolean?): Any? {
            return when (data) {
                null -> null
                true -> 1L
                else -> null
            }
        }
    }

    class Id : MacrosTypeImpl<Long>() {
        override fun toString(): String = "id"

        @Throws(TypeCastException::class)
        override fun fromNonEmptyString(data: String): Long = stringToLong(data)

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): Long = objectToLong(data)

        override fun javaClass(): Class<Long> = Long::class.javaObjectType

        override fun sqliteType(): SqliteType = SqliteType.INTEGER
    }

    class Int : MacrosTypeImpl<Long>() {
        override fun toString(): String = "integer"

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): Long = objectToLong(data)

        @Throws(TypeCastException::class)
        override fun fromNonEmptyString(data: String): Long = stringToLong(data)

        override fun javaClass(): Class<Long> = Long::class.javaObjectType

        override fun sqliteType(): SqliteType = SqliteType.INTEGER
    }

    class Real : MacrosTypeImpl<Double>() {
        override fun toString(): String = "real"

        @Throws(TypeCastException::class)
        override fun fromNonEmptyString(data: String): Double {
            return try {
                data.toDouble()
            } catch (e: NumberFormatException) {
                throw TypeCastException("Cannot convert string '$data' to Double")
            }
        }

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): Double {
            return when (data) {
                is Double -> data
                else -> fromNonEmptyString(data.toString())
            }
        }

        override fun javaClass(): Class<Double> = Double::class.javaObjectType

        override fun sqliteType(): SqliteType = SqliteType.REAL
    }

    class Text : MacrosTypeImpl<String>() {
        override fun toString(): String = "text"
        override fun fromNonEmptyString(data: String): String = data
        override fun fromRawNotNull(data: Any): String = data.toString()
        override fun javaClass(): Class<String> = String::class.java
        override fun sqliteType(): SqliteType = SqliteType.TEXT
    }

    class Time : MacrosTypeImpl<Long>() {
        override fun toString(): String = "time"

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): Long = objectToLong(data)

        @Throws(TypeCastException::class)
        override fun fromNonEmptyString(data: String): Long = stringToLong(data)

        override fun javaClass(): Class<Long> = Long::class.javaObjectType

        override fun sqliteType(): SqliteType = SqliteType.INTEGER
    }

    class Date : MacrosTypeImpl<DateStamp>() {
        override fun toString(): String = "date"

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): DateStamp = fromNonEmptyString(data.toString())

        override fun fromNonEmptyString(data: String): DateStamp = DateStamp.fromIso8601String(data)
        override fun toRaw(data: DateStamp?): Any? = data?.toString()
        override fun javaClass(): Class<DateStamp> = DateStamp::class.java
        override fun sqliteType(): SqliteType = SqliteType.TEXT
    }
}