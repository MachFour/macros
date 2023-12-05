package com.machfour.macros.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.InputStream

internal fun <E: ArchiveEntry> serializeFoods(
    foods: Collection<JsonFood>,
    serializer: Json,
    out: ArchiveOutputStream<E>,
    getFilename: (JsonFood) -> String = { "${it.indexName}.json" },
    makeArchiveEntry: (filename: String, size: Long) -> E,
) {
    val serialisedFoods = foods.map { serializer.encodeToString<JsonFood>(it) }

    for ((f, json) in foods.zip(serialisedFoods)) {
        val filename = getFilename(f)
        val jsonBytes = json.toByteArray()
        val entry = makeArchiveEntry(filename, jsonBytes.size.toLong())
        out.putArchiveEntry(entry)
        out.write(jsonBytes)
        out.closeArchiveEntry()
    }
    out.finish()
}

// Deserializes a single food by reading the input stream from the given archive
// positioned at the given entry. The read size is taken from entry.getSize().
// The input stream should be closed by the caller if necessary.
internal fun deserializeFood(
    inputStream: InputStream,
    entry: ArchiveEntry,
    serializer: Json,
    filenameIsIndexName: Boolean
): JsonFood {
    val bytes = inputStream.readNBytes(entry.size.toInt())
    val food = serializer.decodeFromString<JsonFood>(String(bytes))
    return if (!filenameIsIndexName) food else
        food.copy(indexName = entry.name.removeSuffix(".json"))
}

// Serializes the given collection of foods to JSON and writes them to the
// given stream in the .zip format with default settings.
// Each food is stored as a separate file in the archive, calling getFilename()
// to choose the filename for each food. By default, the food's index name is used.
// The caller should close the stream if necessary.
fun serializeFoodsToZip(
    foods: Collection<JsonFood>,
    serializer: Json,
    output: ZipArchiveOutputStream,
    getFilename: (JsonFood) -> String = { "${it.indexName}.json" },
) {
    return serializeFoods(foods, serializer, output, getFilename) { filename, size ->
        ZipArchiveEntry(filename).also { it.size = size }
    }
}

fun serializeFoodsToZipFile(
    foods: Collection<JsonFood>,
    serializer: Json,
    zipPath: String
) {
    ZipArchiveOutputStream(File(zipPath)).use {
        serializeFoodsToZip(foods, serializer, it)
    }
}

fun deserializeFoodsFromZip(
    input: ZipFile,
    serializer: Json,
    filenameIsIndexName: Boolean,
): List<JsonFood> {
    return buildList {
        input.use { zip ->
            for (entry in zip.entries) {
                zip.getInputStream(entry).use {
                    add(deserializeFood(it, entry, serializer, filenameIsIndexName))
                }
            }
        }
    }
}

fun deserializeFoodsFromZipFile(
    zipPath: String,
    serializer: Json,
    filenameIsIndexName: Boolean = true
) : List<JsonFood> {
    return ZipFile(zipPath).use {
        deserializeFoodsFromZip(it, serializer, filenameIsIndexName)
    }
}
