package com.machfour.macros.sql.datatype

import com.machfour.datestamp.DateStamp
import com.machfour.datestamp.iso8601StringDateStamp
import com.machfour.macros.core.EntityId
import kotlin.reflect.KClass

// basic types corresponding roughly to database types
// TODO check that s.equals(fromString(s).toString()) for valid strings s, for each type
class Types {
    companion object {
        val BOOLEAN = Bool()
        val NULL_BOOLEAN = NullBool()
        val ID = Id()
        val INTEGER = Integer()
        val LONG = LongInteger()
        val REAL = Real()
        val TEXT = Text()
        val TIMESTAMP = Time()
        val DATESTAMP = Date()

        @Throws(TypeCastException::class)
        internal fun stringToInt(s: String): Int {
            return try {
                s.toInt()
            } catch (e: NumberFormatException) {
                throw TypeCastException("Cannot convert string '$s' to Int")
            }
        }

        @Throws(TypeCastException::class)
        internal fun objectToInt(data: Any): Int {
            return when (data) {
                is Int -> data // auto unbox
                else -> stringToInt(data.toString())
            }
        }
        @Throws(TypeCastException::class)
        internal fun stringToLong(s: String): Long {
            return try {
                s.toLong()
            } catch (e: NumberFormatException) {
                throw TypeCastException("Cannot convert string '$s' to Long")
            }
        }

        @Throws(TypeCastException::class)
        internal fun objectToLong(data: Any): Long {
            return when (data) {
                is Long -> data // auto unbox
                is Int -> data.toLong()
                else -> stringToLong(data.toString())
            }
        }
    }

    open class Bool internal constructor(): SqlTypeImpl<Boolean>() {
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
            val truthy = truthyStrings.contains(data.lowercase())
            val falsey = falseyStrings.contains(data.lowercase())

            // can't be both (bad programming)
            check(!(truthy && falsey))

            // if neither then it's a user problem
            return if (!truthy && !falsey) {
                throw TypeCastException("Cannot convert string '$data' to boolean")
            } else {
                truthy
            }
        }

        override fun kotlinClass(): KClass<Boolean> = Boolean::class

        // SQLite doesn't have a boolean type, so we return int
        override fun toRaw(data: Boolean?): Any? {
            return when (data) {
                true -> 1L
                false -> 0L
                null -> null
            }
        }

        override fun sqliteType(): SqliteType = SqliteType.INTEGER
    }

    // Boolean type where null means false. This is a hack used to ensure there's only one default serving per food,
    // using a UNIQUE check on (food_id, is_default)
    class NullBool internal constructor(): Bool() {
        override fun toString(): String = "null-boolean"

        @Throws(TypeCastException::class)
        override fun fromRaw(data: Any?): Boolean {
            return when (data) {
                null -> false
                else -> super.fromRawNotNull(data)
            }
        }

        // return 1 (as long) if data is true, or null otherwise
        override fun toRaw(data: Boolean?): Any? {
            return when (data) {
                true -> 1L
                else -> null
            }
        }
    }

    class Id internal constructor(): SqlTypeImpl<EntityId>() {
        override fun toString(): String = "id"

        @Throws(TypeCastException::class)
        override fun fromNonEmptyString(data: String): EntityId = EntityId(stringToLong(data))

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): EntityId = EntityId(objectToLong(data))

        override fun toRaw(data: EntityId?): Long? {
            return data?.value
        }

        override fun toRawString(data: EntityId?): String {
            return data?.value?.toString() ?: ""
        }

        override fun toSqlString(data: EntityId?): String {
            return data?.value?.toString() ?: "NULL"
        }

        override fun kotlinClass(): KClass<EntityId> = EntityId::class

        override fun sqliteType(): SqliteType = SqliteType.INTEGER
    }

    class Integer internal constructor(): SqlTypeImpl<Int>() {
        override fun toString(): String = "integer"

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): Int = objectToInt(data)

        @Throws(TypeCastException::class)
        override fun fromNonEmptyString(data: String): Int = stringToInt(data)

        override fun kotlinClass(): KClass<Int> = Int::class

        override fun sqliteType(): SqliteType = SqliteType.INTEGER
    }
    class LongInteger internal constructor(): SqlTypeImpl<Long>() {
        override fun toString(): String = "long"

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): Long = objectToLong(data)

        @Throws(TypeCastException::class)
        override fun fromNonEmptyString(data: String): Long = stringToLong(data)

        override fun kotlinClass(): KClass<Long> = Long::class

        override fun sqliteType(): SqliteType = SqliteType.INTEGER
    }

    class Real internal constructor(): SqlTypeImpl<Double>() {
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

        override fun kotlinClass(): KClass<Double> = Double::class

        override fun sqliteType(): SqliteType = SqliteType.REAL
    }

    class Text internal constructor(): SqlTypeImpl<String>() {
        override fun toString(): String = "text"
        override fun fromNonEmptyString(data: String): String = data
        override fun fromRawNotNull(data: Any): String = data.toString()
        override fun kotlinClass(): KClass<String> = String::class
        override fun sqliteType(): SqliteType = SqliteType.TEXT
    }

    class Time internal constructor(): SqlTypeImpl<Long>() {
        override fun toString(): String = "time"

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): Long = objectToLong(data)

        @Throws(TypeCastException::class)
        override fun fromNonEmptyString(data: String): Long = stringToLong(data)

        override fun kotlinClass(): KClass<Long> = Long::class

        override fun sqliteType(): SqliteType = SqliteType.INTEGER
    }

    class Date internal constructor(): SqlTypeImpl<DateStamp>() {
        override fun toString(): String = "date"

        @Throws(TypeCastException::class)
        override fun fromRawNotNull(data: Any): DateStamp = fromNonEmptyString(data.toString())

        override fun fromNonEmptyString(data: String): DateStamp = iso8601StringDateStamp(data)
        override fun toRaw(data: DateStamp?): Any? = data?.toString()
        override fun kotlinClass(): KClass<DateStamp> = DateStamp::class
        override fun sqliteType(): SqliteType = SqliteType.TEXT
    }
}