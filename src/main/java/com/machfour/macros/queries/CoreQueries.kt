package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.SelectQuery
import com.machfour.macros.sql.generator.SingleColumnSelect
import com.machfour.macros.sql.generator.TwoColumnSelect
import java.sql.SQLException

internal object CoreQueries {

    // if number of parameters in a query gets too large, Android SQlite will not execute the query
    internal const val ITERATE_THRESHOLD = 200

    @Throws(SQLException::class)
    fun <M> prefixSearch(ds: SqlDatabase, t: Table<M>, cols: List<Column<M, String>>, keyword: String): List<Long> {
        return stringSearch(ds, t, cols, keyword, globBefore = false, globAfter = true)
    }

    @Throws(SQLException::class)
    fun <M> substringSearch(ds: SqlDatabase, t: Table<M>, cols: List<Column<M, String>>, keyword: String): List<Long> {
        return stringSearch(ds, t, cols, keyword, globBefore = true, globAfter = true)
    }

    @Throws(SQLException::class)
    fun <M> exactStringSearch(ds: SqlDatabase, t: Table<M>, cols: List<Column<M, String>>, keyword: String): List<Long> {
        return stringSearch(ds, t, cols, keyword, globBefore = false, globAfter = false)
    }

    /*
     * Returns empty list for either blank keyword or column list
     */
    @Throws(SQLException::class)
    fun <M> stringSearch(
        ds: SqlDatabase,
        t: Table<M>,
        cols: List<Column<M, String>>,
        keyword: String,
        globBefore: Boolean,
        globAfter: Boolean
    ): List<Long> {
        if (keyword.isEmpty() || cols.isEmpty()) {
            return emptyList()
        }

        val keywordGlob = (if (globBefore) "%" else "") + keyword + if (globAfter) "%" else ""
        val keywordCopies = List(cols.size) { keywordGlob }
        val query = SingleColumnSelect.build(t, t.idColumn) {
            whereLike(cols, keywordCopies)
        }
        return ds.selectNonNullColumn(query)
    }


    // for convenience
    @Throws(SQLException::class)
    fun <M, I> selectSingleColumn(
        ds: SqlDatabase,
        table: Table<M>,
        selectColumn: Column<M, I>,
        queryOptions: SelectQuery.Builder<M>.() -> Unit
    ): List<I?> {
        return ds.selectColumn(SingleColumnSelect.build(table, selectColumn, queryOptions))
    }

    @Throws(SQLException::class)
    fun <M, I, J> selectTwoColumns(
        ds: SqlDatabase,
        table: Table<M>,
        select1: Column<M, I>,
        select2: Column<M, J>,
        queryOptions: SelectQuery.Builder<M>.() -> Unit
    ): List<Pair<I?, J?>> {
        return ds.selectTwoColumns(TwoColumnSelect.build(table, select1, select2, queryOptions))
    }
    @Throws(SQLException::class)
    fun <M, I> selectNonNullColumn(
        ds: SqlDatabase,
        table: Table<M>,
        selectColumn: Column<M, I>,
        queryOptions: SelectQuery.Builder<M>.() -> Unit
    ): List<I> {
        return ds.selectNonNullColumn(SingleColumnSelect.build(table, selectColumn, queryOptions))
    }

    @Throws(SQLException::class)
    private fun <M : MacrosEntity<M>> isInDatabase(ds: SqlDatabase, o: M): Boolean {
        return if (o.id != MacrosEntity.NO_ID) {
            idExistsInTable(ds, o.table, o.id)
        } else {
            val secondaryKey = o.table.secondaryKeyCols
            if (secondaryKey.isEmpty()) {
                // no way to know except by ID...
                TODO()
            }
            TODO()
            @Suppress("UNREACHABLE_CODE")
            false
        }
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> idExistsInTable(ds: SqlDatabase, table: Table<M>, id: Long): Boolean {
        val idCol = table.idColumn
        val idMatch = selectSingleColumn(ds, table, idCol) {
            where(idCol, id)
        }
        return idMatch.size == 1
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> idsExistInTable(ds: SqlDatabase, table: Table<M>, queryIds: Collection<Long>): Map<Long, Boolean> {
        val idCol: Column<M, Long> = table.idColumn
        val existingIds = selectNonNullColumn(ds, table, idCol) {
            where(idCol, queryIds)
        }.toSet()

        return LinkedHashMap<Long, Boolean>(queryIds.size, 1f).apply {
            for (id in queryIds) {
                this[id] = existingIds.contains(id)
            }

        }
    }

    @Throws(SQLException::class)
    fun <M, J> getIdsFromKeys(ds: SqlDatabase, t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, Long> {
        return if (keys.isNotEmpty()) {
            // The resulting map is unordered
            selectColumnMap(ds, t, keyCol, t.idColumn, keys).mapValues { it.value!! }
        } else {
            emptyMap()
        }
    }


    // The resulting map is unordered
    @Throws(SQLException::class)
    fun <M, I, J> selectColumnMap(
        ds: SqlDatabase,
        t: Table<M>,
        keyColumn: Column<M, I>,
        valueColumn: Column<M, J>,
        keys: Collection<I>,
        // when number of keys gets too large, split up the query
        iterateThreshold: Int = ITERATE_THRESHOLD
    ): Map<I, J?> {
        require(!keyColumn.isNullable && keyColumn.isUnique) { "Key column $keyColumn (table $t) must be unique and not nullable" }
        val unorderedResults = HashMap<I, J?>(keys.size, 1.0f)
        val query = TwoColumnSelect.build(t, keyColumn, valueColumn) {
            where(keyColumn, keys, iterate = keys.size > iterateThreshold)
        }
        val data = ds.selectTwoColumns(query)
        for (pair in data) {
            val (key, value) = pair
            assert(key != null) { "Found null key in ${t}.$keyColumn!" }
            assert(!unorderedResults.containsKey(key)) { "Two rows in the DB contained the same data in the key column!" }
            unorderedResults[key!!] = value

        }
        return unorderedResults
    }

}