package com.machfour.macros.json

import kotlinx.serialization.json.Json
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class TarGzKtTest {
    class TarGzTestCase(
        val name: String,
        val serializer: Json = JsonSerializer,
        val serialize: (Collection<JsonFood>, OutputStream, Json) -> Unit,
        val deserialize: (InputStream, Json) -> Collection<JsonFood>,
    )
    @Test
    fun testJsonTarGzRoundTrip() {
        val testcases = listOf(
            TarGzTestCase(
                name = "typical",
                serialize = { foods, stream, serializer ->
                    val out = GzipCompressorOutputStream(stream)
                    serializeFoodsToTarGz(foods, serializer, out)
                },
                deserialize = { stream, serializer ->
                    val input = GzipCompressorInputStream(stream)
                    deserializeFoodsFromTarGz(input, serializer, true)
                }
            ),
            TarGzTestCase(
                name = "no pretty print",
                serializer = Json { prettyPrint = false },
                serialize = { foods, stream, serializer ->
                    val out = GzipCompressorOutputStream(stream)
                    serializeFoodsToTarGz(foods, serializer, out)
                },
                deserialize = { stream, serializer ->
                    val input = GzipCompressorInputStream(stream)
                    deserializeFoodsFromTarGz(input, serializer, true)
                }
            ),
            TarGzTestCase(
                name = "custom filename",
                serialize = { foods, stream, serializer ->
                    val out = GzipCompressorOutputStream(stream)
                    var i = 0
                    serializeFoodsToTarGz(foods, serializer, out, getFilename = { "${i++}.json" })
                },
                deserialize = { stream, serializer ->
                    val input = GzipCompressorInputStream(stream)
                    deserializeFoodsFromTarGz(input, serializer, false)
                }
            ),
        )

        val expect = deserializedTestFoods
        val out = ByteArrayOutputStream(16*1024)

        for (t in testcases) {
            t.serialize(expect, out, t.serializer)
            val actual = t.deserialize(ByteArrayInputStream(out.toByteArray()), t.serializer)
            assertIterableEquals(expect, actual)
            out.reset()
        }
    }
}