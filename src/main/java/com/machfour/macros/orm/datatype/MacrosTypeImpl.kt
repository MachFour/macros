package com.machfour.macros.orm.datatype

abstract class MacrosTypeImpl<J> : MacrosType<J> {
    // gets the java class associated with this type
    abstract fun javaClass(): Class<J>

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
    final override fun toRawString(data: J?): String {
        return toString(data, "")
    }

    // Returns a string representation suitable for use in issuing an SQL command to store the given data
    // into an SQL database. In particular, null data is converted into the string "NULL"
    final override fun toSqlString(data: J?): String {
        return toString(data, "NULL")
    }

    // Returns a string representation of the given data, with null data represented by the string 'null'
    final override fun toString(data: J?): String {
        return toString(data, "null")
    }

    final override fun toString(data: J?, nullString: String): String {
        return data?.toString() ?: nullString
    }

    final override fun cast(o: Any?): J? {
        return javaClass().cast(o)
    }

    // this is not anything to do with the data!
    abstract override fun toString(): String
}