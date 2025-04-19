package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.sql.entities.MacrosSqlEntity
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.*

@Throws(SqlException::class)
fun <M> prefixSearch(
    db: SqlDatabase,
    table: Table<*, M>,
    cols: List<Column<M, String>>,
    keyword: String,
    extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
): List<Long> {
    return stringSearch(db, table, cols, keyword, globBefore = false, globAfter = true, extraOptions = extraOptions)
}

@Throws(SqlException::class)
fun <M> substringSearch(
    db: SqlDatabase,
    table: Table<*, M>,
    cols: List<Column<M, String>>,
    keyword: String,
    extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
): List<Long> {
    return stringSearch(db, table, cols, keyword, globBefore = true, globAfter = true, extraOptions = extraOptions)
}

@Throws(SqlException::class)
fun <M> exactStringSearch(
    db: SqlDatabase,
    table: Table<*, M>,
    cols: List<Column<M, String>>,
    keyword: String,
    extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
): List<Long> {
    return stringSearch(db, table, cols, keyword, globBefore = false, globAfter = false, extraOptions = extraOptions)
}

// Returns empty list if either keyword is blank or cols is empty
@Throws(SqlException::class)
fun <M> stringSearch(
    db: SqlDatabase,
    table: Table<*, M>,
    cols: List<Column<M, String>>,
    keyword: String,
    globBefore: Boolean,
    globAfter: Boolean,
    extraOptions: SelectQuery.Builder<M>.() -> Unit = {},
): List<Long> {
    if (keyword.isEmpty() || cols.isEmpty()) {
        return emptyList()
    }

    val keywordGlob = (if (globBefore) "%" else "") + keyword + if (globAfter) "%" else ""
    val keywordCopies = List(cols.size) { keywordGlob }
    val query = SingleColumnSelect.build(table.idColumn) {
        whereLike(cols, keywordCopies)
        extraOptions()
    }
    //println(query.toSql())
    return db.selectNonNullColumn(query)
}


// for convenience
@Throws(SqlException::class)
fun <M, I: Any> selectSingleColumn(
    db: SqlDatabase,
    selectColumn: Column<M, I>,
    queryOptions: SelectQuery.Builder<M>.() -> Unit
): List<I?> {
    return db.selectColumn(SingleColumnSelect.build(selectColumn, queryOptions))
}

@Throws(SqlException::class)
fun <M, I: Any, J: Any> selectTwoColumns(
    db: SqlDatabase,
    select1: Column<M, I>,
    select2: Column<M, J>,
    queryOptions: SelectQuery.Builder<M>.() -> Unit
): List<Pair<I?, J?>> {
    val table = select1.table
    return db.selectTwoColumns(TwoColumnSelect.build(table, select1, select2, queryOptions))
}

@Throws(SqlException::class)
fun <M, I: Any, J: Any, K: Any> selectThreeColumns(
    db: SqlDatabase,
    select1: Column<M, I>,
    select2: Column<M, J>,
    select3: Column<M, K>,
    queryOptions: SelectQuery.Builder<M>.() -> Unit
): List<Triple<I?, J?, K?>> {
    val table = select1.table
    val columns = listOf(select1, select2, select3)
    val resultData = db.selectMultipleColumns(MultiColumnSelect.build(table, columns, queryOptions))
    return resultData.map { Triple(it[select1], it[select2], it[select3]) }
}

@Throws(SqlException::class)
fun <M, I: Any> selectNonNullColumn(
    db: SqlDatabase,
    selectColumn: Column<M, I>,
    queryOptions: SelectQuery.Builder<M>.() -> Unit
): List<I> {
    return db.selectNonNullColumn(SingleColumnSelect.build(selectColumn, queryOptions))
}

@Throws(SqlException::class)
fun <M : MacrosSqlEntity<M>> idExistsInTable(db: SqlDatabase, table: Table<*, M>, id: Long): Boolean {
    val idCol = table.idColumn
    val idMatch = selectSingleColumn(db, idCol) {
        where(idCol, id)
    }
    return idMatch.size == 1
}

@Throws(SqlException::class)
fun <M : MacrosSqlEntity<M>> idsExistInTable(db: SqlDatabase, table: Table<*, M>, queryIds: Collection<Long>): Map<Long, Boolean> {
    val idCol = table.idColumn
    val existingIds = selectNonNullColumn(db, idCol) {
        where(idCol, queryIds)
    }.toSet()

    return queryIds.associateWith { existingIds.contains(it) }
}

@Throws(SqlException::class)
fun <M, J: Any> getIdsFromKeys(ds: SqlDatabase, t: Table<*, M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, Long> {
    return if (keys.isNotEmpty()) {
        // The resulting map is unordered
        selectColumnMap(ds, t, keyCol, t.idColumn, keys).mapValues { it.value!! }
    } else {
        emptyMap()
    }
}


// The resulting map is unordered
@Throws(SqlException::class)
fun <M, I: Any, J: Any> selectColumnMap(
    db: SqlDatabase,
    t: Table<*, M>,
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
    val data = db.selectTwoColumns(query)
    for (pair in data) {
        val (key, value) = pair
        if (enforceNotNull) {
            checkNotNull(key) { "Found null key in ${t}.$keyColumn!" }
        } else if (key == null) {
            continue
        }
        check(!unorderedResults.containsKey(key)) {
            "Two rows in $t table contained the same value $key in unique column $keyColumn!"
        }
        unorderedResults[key] = value
    }
    return unorderedResults
}

@Throws(SqlException::class)
fun <K, M: MacrosEntityImpl<M>> findUniqueColumnConflicts(
    db: SqlDatabase,
    table: Table<*, M>,
    objectMap: Map<K, M>
): Map<K, M> {
    return buildMap {
        for (col in table.columns.filter { it.isUnique }) {
            val problemValues = findConflictingUniqueColumnValues(db, objectMap, col)
            objectMap.filterTo(this) { it.value.getData(col) in problemValues }
        }
    }
}


// helper/wildcard capture
@Throws(SqlException::class)
private fun <K, M: MacrosSqlEntity<M>, J: Any> findConflictingUniqueColumnValues(
    ds: SqlDatabase,
    objectMap: Map<K, M>,
    uniqueCol: Column<M, J>
): Set<J> {
    require(uniqueCol.isUnique)

    val objectValues = objectMap.values.mapNotNull { it.getData(uniqueCol) }

    if (objectValues.isEmpty()) {
        return emptySet()
    }

    return selectSingleColumn(ds, uniqueCol) {
        where(uniqueCol, objectValues)
        distinct()
    }.filterNotNullTo(HashSet())
}
