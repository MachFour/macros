package com.machfour.macros.json

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ZipKtTest {
    class ZipTestCase(
        val name: String,
        val serialize: (Collection<JsonFood>, ByteArrayOutputStream) -> Unit,
        val deserialize: (ByteArrayInputStream) -> Collection<JsonFood>,
    )

    @Test
    fun testJsonZipRoundTrip() {
        val testcases = listOf(
            ZipTestCase(
                name = "typical",
                serialize = { foods, out ->
                    val serializer = Json { prettyPrint = true }
                    serialiseFoodsToZip(foods, out, serializer)
                },
                deserialize = { deserializeFoodsFromZip(it) }
            ),
            ZipTestCase(
                name = "no pretty print",
                serialize = { foods, out ->
                    val serializer = Json { prettyPrint = false }
                    serialiseFoodsToZip(foods, out, serializer)
                },
                deserialize = { deserializeFoodsFromZip(it) }
            ),
            ZipTestCase(
                name = "custom filename",
                serialize = { foods, out ->
                    val serializer = Json { prettyPrint = true }
                    var i = 0
                    serialiseFoodsToZip(foods, out, serializer, getFilename = { "${i++}.json" })
                },
                deserialize = { deserializeFoodsFromZip(it, filenameIsIndexName = false) }
            ),
        )

        val expect = deserializedTestFoods
        val out = ByteArrayOutputStream(1*1024*1024)

        for (t in testcases) {
            t.serialize(expect, out)
            val actual = t.deserialize(ByteArrayInputStream(out.toByteArray()))
            assertIterableEquals(expect, actual)
            out.reset()
        }
    }
}