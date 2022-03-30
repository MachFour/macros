package com.machfour.macros.sql.datatype

interface SqlType<J: Any> {
    // These methods perform type-specific conversion is necessary
    // if raw is null then null will be returned
    @Throws(TypeCastException::class)
    fun fromRaw(data: Any?): J?

    // tries to convert the given string representation into the desired type
    // Empty strings will return the result of fromRaw(null)
    // This method will never return null if the string is non-empty
    @Throws(TypeCastException::class)
    fun fromRawString(data: String): J?

    // This returns the data in a form that is able to be inserted into a database
    // for SQLite, this means, for example, that booleans become integers.
    fun toRaw(data: J?): Any? {
        return data
    }

    // Returns a string representation suitable for saving into a textual format (e.g. CSV)
    // In particular, null data becomes empty strings
    fun toRawString(data: J?): String

    // Returns a string representation suitable for use in issuing an SQL command to store the given data
    // into an SQL database. In particular, null data is converted into the string "NULL"
    fun toSqlString(data: J?): String

    // Returns a string representation of the given data, with a custom placeholder as null
    fun toString(data: J?, nullString: String = "null"): String

    // A dumb Java cast from the given object to the Java class associated with this Type
    fun cast(o: Any?): J?

    // may not be needed anymore - was used in Android DB code
    fun sqliteType(): SqliteType
}