package com.machfour.macros.sql.datatype

import kotlin.reflect.KClass
import kotlin.reflect.safeCast

abstract class SqlTypeImpl<J: Any> : SqlType<J> {
    // gets the java class associated with this type
    abstract fun kotlinClass(): KClass<J>

    // this one does the real conversion work
    @Throws(TypeCastException::class)
    protected abstract fun fromNonEmptyString(data: String): J

    // this one does the real conversion work
    @Throws(TypeCastException::class)
    protected abstract fun fromRawNotNull(data: Any): J

    // This would normally be final, but the 'NullBool' type needs to override this, to treat null as false
    @Throws(TypeCastException::class)
    override fun fromRaw(data: Any?): J? {
        return data?.let { fromRawNotNull(it) }
    }

    @Throws(TypeCastException::class)  // Empty strings are treated as null object
    final override fun fromRawString(data: String): J? {
        return if (data.isEmpty()) fromRaw(null) else fromNonEmptyString(data)
    }

    // Returns a string representation suitable for saving into a textual format (e.g. CSV)
    // In particular, null data becomes empty strings
    override fun toRawString(data: J?): String {
        return toRaw(data)?.toString() ?: ""
    }

    // Returns a string representation suitable for use in issuing an SQL command to store the given data
    // into an SQL database. In particular, null data is converted into the string "NULL"
    override fun toSqlString(data: J?): String {
        return toString(data, "NULL")
    }

    final override fun toString(data: J?, nullString: String): String {
        return data?.toString() ?: nullString
    }

    final override fun cast(o: Any?): J? {
        return kotlinClass().safeCast(o)
    }

    // this is not anything to do with the data!
    abstract override fun toString(): String
}