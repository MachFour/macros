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
    fun <M> prefixSearch(
        db: SqlDatabase,
        cols: List<Column<M, String>>,
        keyword: String,
        extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
    ): List<Long> {
        return stringSearch(db, cols, keyword, globBefore = false, globAfter = true, extraOptions = extraOptions)
    }

    @Throws(SQLException::class)
    fun <M> substringSearch(
        db: SqlDatabase,
        cols: List<Column<M, String>>,
        keyword: String,
        extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
    ): List<Long> {
        return stringSearch(db, cols, keyword, globBefore = true, globAfter = true, extraOptions = extraOptions)
    }

    @Throws(SQLException::class)
    fun <M> exactStringSearch(
        db: SqlDatabase,
        cols: List<Column<M, String>>,
        keyword: String,
        extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
    ): List<Long> {
        return stringSearch(db, cols, keyword, globBefore = false, globAfter = false, extraOptions = extraOptions)
    }

    /*
     * Returns empty list for either blank keyword or column list
     */
    @Throws(SQLException::class)
    fun <M> stringSearch(
        db: SqlDatabase,
        cols: List<Column<M, String>>,
        keyword: String,
        globBefore: Boolean,
        globAfter: Boolean,
        extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
    ): List<Long> {
        if (keyword.isEmpty() || cols.isEmpty()) {
            return emptyList()
        }
        val idColumn = cols.first().table.idColumn

        val keywordGlob = (if (globBefore) "%" else "") + keyword + if (globAfter) "%" else ""
        val keywordCopies = List(cols.size) { keywordGlob }
        val query = SingleColumnSelect.build(idColumn) {
            whereLike(cols, keywordCopies)
            extraOptions()
        }
        return db.selectNonNullColumn(query)
    }


    // for convenience
    @Throws(SQLException::class)
    fun <M, I> selectSingleColumn(
        db: SqlDatabase,
        selectColumn: Column<M, I>,
        queryOptions: SelectQuery.Builder<M>.() -> Unit
    ): List<I?> {
        return db.selectColumn(SingleColumnSelect.build(selectColumn, queryOptions))
    }

    @Throws(SQLException::class)
    fun <M, I, J> selectTwoColumns(
        db: SqlDatabase,
        table: Table<M>,
        select1: Column<M, I>,
        select2: Column<M, J>,
        queryOptions: SelectQuery.Builder<M>.() -> Unit
    ): List<Pair<I?, J?>> {
        return db.selectTwoColumns(TwoColumnSelect.build(table, select1, select2, queryOptions))
    }
    @Throws(SQLException::class)
    fun <M, I> selectNonNullColumn(
        db: SqlDatabase,
        selectColumn: Column<M, I>,
        queryOptions: SelectQuery.Builder<M>.() -> Unit
    ): List<I> {
        return db.selectNonNullColumn(SingleColumnSelect.build(selectColumn, queryOptions))
    }

    @Throws(SQLException::class)
    private fun <M : MacrosEntity<M>> isInDatabase(db: SqlDatabase, o: M): Boolean {
        return if (o.id != MacrosEntity.NO_ID) {
            idExistsInTable(db, o.table, o.id)
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
    fun <M : MacrosEntity<M>> idExistsInTable(db: SqlDatabase, table: Table<M>, id: Long): Boolean {
        val idCol = table.idColumn
        val idMatch = selectSingleColumn(db, idCol) {
            where(idCol, id)
        }
        return idMatch.size == 1
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> idsExistInTable(db: SqlDatabase, table: Table<M>, queryIds: Collection<Long>): Map<Long, Boolean> {
        val idCol = table.idColumn
        val existingIds = selectNonNullColumn(db, idCol) {
            where(idCol, queryIds)
        }.toSet()

        return queryIds.associateWith { existingIds.contains(it) }
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