package com.machfour.macros.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

// Exports the given collection of foods to JSON, using the given function
// to write each food's JSON data. The filename for each food is generated
// using the getFilename function. By default, it uses the food's index name.
fun serialiseFoodsToZip(
    foods: Collection<JsonFood>,
    outputStream: OutputStream,
    serializer: Json = Json { prettyPrint = true },
    deflate: Boolean = true,
    getFilename: (JsonFood) -> String = { "${it.indexName}.json" },
) {
    val serialisedFoods = foods.map { serializer.encodeToString<JsonFood>(it) }

    ZipOutputStream(outputStream).use { zip ->
        if (deflate) {
            zip.setMethod(ZipOutputStream.DEFLATED)
        } else {
            zip.setMethod(ZipOutputStream.STORED)
        }

        for ((f, json) in foods.zip(serialisedFoods)) {
            val filename = getFilename(f)
            zip.putNextEntry(ZipEntry(filename))
            json.toByteArray().let { zip.write(it, 0, it.size) }
            zip.closeEntry()
        }
        zip.finish()
    }
}

fun serialiseFoodsToZipGzFile(foods: Collection<JsonFood>, zgzPath: String) {
    FileOutputStream(zgzPath).use {
        GZIPOutputStream(it).use { gz ->
            serialiseFoodsToZip(foods, gz, deflate = false)
        }
    }
}

fun serialiseFoodsToZipFile(foods: Collection<JsonFood>, zipPath: String) {
    FileOutputStream(zipPath).use {
        serialiseFoodsToZip(foods, it)
    }
}

fun deserializeFoodsFromZip(
    inputStream: InputStream,
    serializer: Json = Json { prettyPrint = true },
    filenameIsIndexName: Boolean = true
): List<JsonFood> {
    return buildList {
        ZipInputStream(inputStream).use {
            while (true) {
                val entry = it.nextEntry ?: break
                val json = String(it.readAllBytes())
                val food = serializer.decodeFromString<JsonFood>(json)
                if (filenameIsIndexName) {
                    add(food.copy(indexName = entry.name.removeSuffix(".json")))
                } else {
                    add(food)
                }
            }
        }
    }
}
