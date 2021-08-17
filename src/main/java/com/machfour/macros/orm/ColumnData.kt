package com.machfour.macros.orm

import com.machfour.macros.orm.datatype.TypeCastException

// Class which maps columns to their data values in instances of Macros objects
class ColumnData<M> private constructor(
    // Caller (other constructors in this class) must ensure: existing.hasColumns(cols)
    // Internally, since all of the columns are known at compile time, we can just assign an index to each one
    // and store the values in a list according to that index.
    val table: Table<M>,
    cols: Collection<Column<M, *>>,
    existing: ColumnData<M>?
) {

    constructor(t: Table<M>, cols: Collection<Column<M, *>>) : this(t, cols, null)
    constructor(t: Table<M>) : this(t, t.columns, null)

    // in order to have an arbitrary set of table columns used, we need to have an arraylist big enough to
    // hold columns of any index, up to the number of columns in the table minus 1.
    private val data: Array<Any?> = arrayOfNulls(table.columns.size)
    private val hasData = BooleanArray(table.columns.size)
    // which columns have data stored in this ColumnData object;
    val columns: Set<Column<M, *>> = LinkedHashSet(cols)

    init {
        setDefaultData()
        if (existing != null) {
            copyData(existing, this, cols)
        }
    }

    var isImmutable: Boolean = false
        private set


    internal fun setImmutable() {
        this.isImmutable = true
    }


    companion object {
        fun <M> columnsAreEqual(c1: ColumnData<M>, c2: ColumnData<M>, whichCols: List<Column<M, *>>): Boolean {
            if (!c1.hasColumns(whichCols) || !c1.hasColumns(whichCols)) {
                return false
            }
            return whichCols.all { col -> c1[col] == c2[col] }
        }

        // columns are the same so there will be no type issues
        fun <M> copyData(from: ColumnData<M>, to: ColumnData<M>, which: Collection<Column<M, *>>) {
            assert(to.hasColumns(which) && from.hasColumns(which)) { "Specified columns not present in both from and to" }
            to.assertMutable()
            for (col in which) {
                val o = from.data[col.index]
                to.data[col.index] = o
                to.hasData[col.index] = o != null
            }
        }
    }

    // null represented by empty string
    fun <J> getAsRawString(col: Column<M, J>): String {
        return col.type.toRawString(get(col))
    }

    // null represented by "NULL"
    fun <J> getAsSqlString(col: Column<M, J>): String {
        return col.type.toSqlString(get(col))
    }

    // null represented by empty string
    @Throws(TypeCastException::class)
    fun <J> putFromString(col: Column<M, J>, data: String) {
        put(col, col.type.fromRawString(data))
    }

    fun <J> getAsRaw(col: Column<M, J>): Any? {
        return col.type.toRaw(get(col))
    }

    @Throws(TypeCastException::class)
    fun <J> putFromRaw(col: Column<M, J>, data: Any?) {
        put(col, col.type.fromRaw(data))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ColumnData<*>) {
            return false
        }
        return table == other.table && data.contentDeepEquals(other.data)
        // the following equality should be implied by the latter one above:
        // hasData.equals(((ColumnData) o).hasData)
    }

    fun hasColumns(cols: Collection<Column<M, *>>): Boolean {
        return columns.containsAll(cols)
    }

    override fun hashCode(): Int {
        // TODO return Objects.hash(table, data);
        return arrayOf(table, data).contentHashCode()
    }

    override fun toString(): String {
        val str = StringBuilder("ColumnData<").append(table.name).append("> [")
        for (col in columns) {
            str.append("${col.sqlName} = ${data[col.index]}, ")
        }
        str.append("]")
        return str.toString()
    }

    fun setDefaultData(cols: Collection<Column<M, *>> = columns) {
        assert(hasColumns(cols))
        assertMutable()
        for (col in cols) {
            val o = col.defaultData
            // can't use the put() method due to type erasure
            data[col.index] = o
            hasData[col.index] = o != null
        }

    }

    fun copy(): ColumnData<M> {
        return ColumnData(table, table.columns, this)
    }

    fun copy(whichCols: Collection<Column<M, *>>): ColumnData<M> {
        assertHasColumns(whichCols)
        return ColumnData(table, whichCols, this)
    }

    private fun assertHasColumn(col: Column<M, *>) {
        assertHasColumns(listOf(col))
    }

    private fun assertHasColumns(cols: Collection<Column<M, *>>) {
        assert(columns.containsAll(cols))
    }

    private fun assertMutable() {
        assert(!isImmutable) { "ColumnData has been made immutable" }
    }

    // the type of the data is ensured at time of adding it to this columnData object.
    operator fun <J> get(col: Column<M, J>): J? {
        assertHasColumn(col)
        return getWithoutAssert(col)
    }

    private fun <J> getWithoutAssert(col: Column<M, J>): J? {
        return col.type.cast(data[col.index])
    }

    // will throw exception if the column is not present
    // No validation is performed on the value
    fun <J> put(col: Column<M, J>, value: J?) {
        assertHasColumn(col)
        assertMutable()
        data[col.index] = value
        hasData[col.index] = value != null
    }

    fun hasData(col: Column<M, *>): Boolean {
        assertHasColumn(col)
        return hasData[col.index]
    }

}
