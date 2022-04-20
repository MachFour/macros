package com.machfour.macros.sql

import com.machfour.macros.sql.datatype.TypeCastException

// Class which maps columns to their data values in instances of Macros objects
class RowData<M> private constructor(
    // Caller (other constructors in this class) must ensure: existing.hasColumns(cols)
    // Internally, since all of the columns are known at compile time, we can just assign an index to each one
    // and store the values in a list according to that index.
    val table: Table<M>,
    val columns: Set<Column<M, *>>,
    private val enforceNonNull: Boolean,
    existing: RowData<M>?,
) {

    constructor(t: Table<M>, cols: Collection<Column<M, *>> = t.columns, enforceNonNull: Boolean = false)
            : this(t, cols.toSet(), enforceNonNull, null)

    // in order to have an arbitrary set of table columns used, we need to have an arraylist big enough to
    // hold columns of any index, up to the number of columns in the table minus 1.
    private val data: Array<Any?> = arrayOfNulls(table.columns.size)
    private val hasValue = BooleanArray(table.columns.size)

    // which columns have data stored in this RowData object;

    init {
        // init default data
        columns.forEach { c ->
            // can't use the put() method due to type erasure
            c.defaultData.let { data[c.index] = it; hasValue[c.index] = it != null }
        }
        if (existing != null) {
            copyData(existing, this, columns)
        }
    }

    var isImmutable: Boolean = false
        private set


    // can't set to false
    internal fun makeImmutable() {
        isImmutable = true
    }


    companion object {
        fun <M> columnsAreEqual(c1: RowData<M>, c2: RowData<M>, whichCols: List<Column<M, *>>): Boolean {
            if (!c1.hasColumns(whichCols) || !c1.hasColumns(whichCols)) {
                return false
            }
            return whichCols.all { col -> c1[col] == c2[col] }
        }

        // columns are the same so there will be no type issues
        fun <M> copyData(from: RowData<M>, to: RowData<M>, which: Collection<Column<M, *>>) {
            require(to.hasColumns(which) && from.hasColumns(which)) { "Specified columns not present in both from and to" }
            check(!to.isImmutable) { "RowData has been made immutable" }
            for (col in which) {
                val o = from.data[col.index]
                to.data[col.index] = o
                to.hasValue[col.index] = o != null
            }
        }
    }

    // null represented by empty string
    fun <J: Any> getAsRawString(col: Column<M, J>): String {
        return col.type.toRawString(get(col))
    }

    fun <J: Any> getAsSqlString(col: Column<M, J>): String {
        return col.type.toSqlString(get(col))
    }

    // null represented by empty string
    @Throws(TypeCastException::class)
    fun <J: Any> putFromString(col: Column<M, J>, data: String) {
        put(col, col.type.fromRawString(data))
    }

    fun <J: Any> getAsRaw(col: Column<M, J>): Any? {
        return col.type.toRaw(get(col))
    }

    @Throws(TypeCastException::class)
    fun <J: Any> putFromRaw(col: Column<M, J>, data: Any?) {
        put(col, col.type.fromRaw(data))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RowData<*>) {
            return false
        }
        return table == other.table && data.contentDeepEquals(other.data)
        // the following equality should be implied by the latter one above:
        // hasData.equals(((RowData) o).hasData)
    }

    fun hasColumns(cols: Collection<Column<M, *>>): Boolean {
        return columns.containsAll(cols)
    }

    override fun hashCode(): Int {
        // TODO return Objects.hash(table, data);
        return arrayOf(table, data).contentHashCode()
    }

    override fun toString(): String {
        val str = StringBuilder("RowData<").append(table.name).append("> [")
        for (col in columns) {
            str.append("${col.sqlName} = ${data[col.index]}, ")
        }
        str.append("]")
        return str.toString()
    }

    fun copy(): RowData<M> {
        return RowData(table, columns, enforceNonNull, this)
    }

    fun copy(whichCols: Collection<Column<M, *>>): RowData<M> {
        checkHasColumns(whichCols)
        return RowData(table, whichCols.toSet(), enforceNonNull,this)
    }

    private fun assertHasColumn(col: Column<M, *>) {
        checkHasColumns(listOf(col))
    }

    private fun checkHasColumns(cols: Collection<Column<M, *>>) {
        check(columns.containsAll(cols))
    }

    // the type of the data is ensured at time of adding it to this RowData object.
    operator fun <J: Any> get(col: Column<M, J>): J? {
        assertHasColumn(col)
        val value = col.type.cast(data[col.index])
        if (enforceNonNull) {
            check(col.isNullable || value != null) { "null data retrieved from not-nullable column" }
        }
        return value
    }

    // will throw exception if the column is not present
    // No validation is performed on the value
    fun <J: Any> put(col: Column<M, J>, value: J?) {
        assertHasColumn(col)
        check(!isImmutable) { "RowData has been made immutable" }
        data[col.index] = value
        hasValue[col.index] = value != null
    }

    fun hasValue(col: Column<M, *>): Boolean {
        assertHasColumn(col)
        return hasValue[col.index]
    }

}
