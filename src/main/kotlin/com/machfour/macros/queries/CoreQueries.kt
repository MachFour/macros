package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.SelectQuery
import com.machfour.macros.sql.generator.SingleColumnSelect
import com.machfour.macros.sql.generator.TwoColumnSelect

@Throws(SqlException::class)
internal fun <M> prefixSearch(
    db: SqlDatabase,
    cols: List<Column<M, String>>,
    keyword: String,
    extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
): List<Long> {
    return stringSearch(db, cols, keyword, globBefore = false, globAfter = true, extraOptions = extraOptions)
}

@Throws(SqlException::class)
internal fun <M> substringSearch(
    db: SqlDatabase,
    cols: List<Column<M, String>>,
    keyword: String,
    extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
): List<Long> {
    return stringSearch(db, cols, keyword, globBefore = true, globAfter = true, extraOptions = extraOptions)
}

@Throws(SqlException::class)
internal fun <M> exactStringSearch(
    db: SqlDatabase,
    cols: List<Column<M, String>>,
    keyword: String,
    extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
): List<Long> {
    return stringSearch(db, cols, keyword, globBefore = false, globAfter = false, extraOptions = extraOptions)
}

// Returns empty list if either keyword is blank or cols is empty
@Throws(SqlException::class)
internal fun <M> stringSearch(
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
@Throws(SqlException::class)
internal fun <M, I: Any> selectSingleColumn(
    db: SqlDatabase,
    selectColumn: Column<M, I>,
    queryOptions: SelectQuery.Builder<M>.() -> Unit
): List<I?> {
    return db.selectColumn(SingleColumnSelect.build(selectColumn, queryOptions))
}

@Throws(SqlException::class)
internal fun <M, I: Any, J: Any> selectTwoColumns(
    db: SqlDatabase,
    select1: Column<M, I>,
    select2: Column<M, J>,
    queryOptions: SelectQuery.Builder<M>.() -> Unit
): List<Pair<I?, J?>> {
    val table = select1.table
    return db.selectTwoColumns(TwoColumnSelect.build(table, select1, select2, queryOptions))
}
@Throws(SqlException::class)
internal fun <M, I: Any> selectNonNullColumn(
    db: SqlDatabase,
    selectColumn: Column<M, I>,
    queryOptions: SelectQuery.Builder<M>.() -> Unit
): List<I> {
    return db.selectNonNullColumn(SingleColumnSelect.build(selectColumn, queryOptions))
}

@Throws(SqlException::class)
internal fun <M : MacrosEntity<M>> idExistsInTable(db: SqlDatabase, table: Table<M>, id: Long): Boolean {
    val idCol = table.idColumn
    val idMatch = selectSingleColumn(db, idCol) {
        where(idCol, id)
    }
    return idMatch.size == 1
}

@Throws(SqlException::class)
internal fun <M : MacrosEntity<M>> idsExistInTable(db: SqlDatabase, table: Table<M>, queryIds: Collection<Long>): Map<Long, Boolean> {
    val idCol = table.idColumn
    val existingIds = selectNonNullColumn(db, idCol) {
        where(idCol, queryIds)
    }.toSet()

    return queryIds.associateWith { existingIds.contains(it) }
}

@Throws(SqlException::class)
internal fun <M, J: Any> getIdsFromKeys(ds: SqlDatabase, t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, Long> {
    return if (keys.isNotEmpty()) {
        // The resulting map is unordered
        selectColumnMap(ds, t, keyCol, t.idColumn, keys).mapValues { it.value!! }
    } else {
        emptyMap()
    }
}


// The resulting map is unordered
@Throws(SqlException::class)
internal fun <M, I: Any, J: Any> selectColumnMap(
    ds: SqlDatabase,
    t: Table<M>,
    keyColumn: Column<M, I>,
    valueColumn: Column<M, J>,
    keys: Collection<I>,
    enforceNotNull: Boolean = true // if false, the key column can be nullable and null keys will be ignored
): Map<I, J?> {
    require(keyColumn.isUnique) { "Key column $keyColumn (table $t) must be unique" }
    if (enforceNotNull) {
        require(!keyColumn.isNullable) { "Key column $keyColumn (table $t) must be not nullable" }
    }
    val unorderedResults = HashMap<I, J?>(keys.size, 1.0f)
    val query = TwoColumnSelect.build(t, keyColumn, valueColumn) {
        where(keyColumn, keys)
    }
    val data = ds.selectTwoColumns(query)
    for (pair in data) {
        val (key, value) = pair
        if (enforceNotNull) {
            requireNotNull(key) { "Found null key in ${t}.$keyColumn!" }
        } else if (key == null) {
            continue
        }
        assert(!unorderedResults.containsKey(key)) {
            "Two rows in $t table contained the same value $key in unique column $keyColumn!"
        }
        unorderedResults[key] = value
    }
    return unorderedResults
}

@Throws(SqlException::class)
internal fun <K, M: MacrosEntity<M>> findUniqueColumnConflicts(
    db: SqlDatabase,
    objectMap: Map<K, M>
): Map<K, M> {
    val table = objectMap.entries.firstOrNull()?.value?.table ?: return emptyMap()

    val uniqueCols = table.columns.filter { it.isUnique }

    return buildMap {
        for (col in uniqueCols) {
            val problemValues = findConflictingUniqueColumnValues(db, objectMap, col)
            objectMap.filterTo(this) { it.value.getData(col) in problemValues }
        }
    }
}


// helper/wildcard capture
@Throws(SqlException::class)
private fun <K, M: MacrosEntity<M>, J: Any> findConflictingUniqueColumnValues(
    ds: SqlDatabase,
    objectMap: Map<K, M>,
    uniqueCol: Column<M, J>
): Set<J> {
    assert(uniqueCol.isUnique)

    val objectValues = objectMap.values.mapNotNull { it.getData(uniqueCol) }

    return if (objectValues.isEmpty()) {
        emptySet()
    } else {
        selectSingleColumn(ds, uniqueCol) {
            where(uniqueCol, objectValues)
            distinct()
        }.filterNotNullTo(HashSet())
    }
}
