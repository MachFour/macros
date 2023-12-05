package com.machfour.macros.json

import kotlinx.serialization.json.Json
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream

// Serializes the given collection of foods to JSON and writes them to the
// given stream in the .tar.gz format.
// Each food is stored as a separate file in the archive, calling getFilename()
// to choose the filename for each food. By default, the food's index name is used.
// The caller should close the stream if necessary.
fun serializeFoodsToTarGz(
    foods: Collection<JsonFood>,
    serializer: Json,
    output: GzipCompressorOutputStream,
    getFilename: (JsonFood) -> String = { "${it.indexName}.json" },
) {
    return TarArchiveOutputStream(output).use { tar ->
        serializeFoods(foods, serializer, tar, getFilename) { filename, size ->
            TarArchiveEntry(filename).also { it.size = size }
        }
    }
}

fun serializeFoodsToTarGzFile(
    foods: Collection<JsonFood>,
    serializer: Json,
    path: String
) {
    FileOutputStream(path).use { os ->
        GzipCompressorOutputStream(os).use {
            serializeFoodsToTarGz(foods, serializer, it)
        }
    }
}

fun deserializeFoodsFromTarGz(
    input: GzipCompressorInputStream,
    serializer: Json,
    filenameIsIndexName: Boolean,
): List<JsonFood> {
    return buildList {
        input.use { gz ->
            TarArchiveInputStream(gz).use { tar ->
                while (true) {
                    val entry = tar.nextEntry ?: break
                    add(deserializeFood(tar, entry, serializer, filenameIsIndexName))
                }
            }
        }
    }
}

fun deserializeFoodsFromTarGzFile(
    path: String,
    serializer: Json,
    filenameIsIndexName: Boolean = true,
): List<JsonFood> {
    return FileInputStream(path).use {
        GzipCompressorInputStream(it).use { gz ->
            deserializeFoodsFromTarGz(gz, serializer, filenameIsIndexName)
        }
    }
}
