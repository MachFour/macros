package com.machfour.macros.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test


class JsonTest {

    @Test
    fun testSerialize() {
        val prettyJSON = Json { prettyPrint = true }

        val expected = serializedTestFoods.map { Json.parseToJsonElement(it) }
        val actual = deserializedTestFoods.map { prettyJSON.encodeToJsonElement(it) }

        assertIterableEquals(expected, actual)
    }

    @Test
    fun testDeserialize() {
        val prettyJSON = Json { prettyPrint = true }

        val expected = deserializedTestFoods
        val actual = serializedTestFoods.map { prettyJSON.decodeFromString<JsonFood>(it) }

        assertIterableEquals(expected, actual)
    }
}