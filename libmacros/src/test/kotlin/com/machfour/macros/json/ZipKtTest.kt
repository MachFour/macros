package com.machfour.macros.json

import kotlinx.serialization.json.Json
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import java.nio.channels.SeekableByteChannel

class ZipKtTest {
    class ZipTestCase(
        val name: String,
        val serializer: Json,
        val serialize: (Collection<JsonFood>, SeekableByteChannel, Json) -> Unit,
        val deserialize: (SeekableByteChannel, Json) -> Collection<JsonFood>,
    )
    @Test
    fun testJsonZipRoundTrip() {
        val testcases = listOf(
            ZipTestCase(
                name = "zip, typical",
                serializer = Json { prettyPrint = true },
                serialize = { foods, channel, serializer ->
                    val out = ZipArchiveOutputStream(channel)
                    serializeFoodsToZip(foods, serializer, out)
                },
                deserialize = { channel, serializer ->
                    val input = ZipFile(channel)
                    deserializeFoodsFromZip(input, serializer, true)
                }
            ),
            ZipTestCase(
                name = "no pretty print",
                serializer = Json { prettyPrint = false },
                serialize = { foods, channel, serializer ->
                    val out = ZipArchiveOutputStream(channel)
                    serializeFoodsToZip(foods, serializer, out)
                },
                deserialize = { channel, serializer ->
                    val input = ZipFile(channel)
                    deserializeFoodsFromZip(input, serializer, true)
                }
            ),
            ZipTestCase(
                name = "custom filename",
                serializer = Json { prettyPrint = true },
                serialize = { foods, channel, serializer ->
                    val out = ZipArchiveOutputStream(channel)
                    var i = 0
                    serializeFoodsToZip(foods, serializer, out, getFilename = { "${i++}.json" })
                },
                deserialize = { channel, serializer ->
                    val input = ZipFile(channel)
                    deserializeFoodsFromZip(input, serializer, false)
                }
            ),
        )

        val expect = deserializedTestFoods

        for (t in testcases) {
            val out = SeekableInMemoryByteChannel(16*1024)
            t.serialize(expect, out, t.serializer)
            val actual = t.deserialize(out, t.serializer)
            assertIterableEquals(expect, actual)
        }
    }
}