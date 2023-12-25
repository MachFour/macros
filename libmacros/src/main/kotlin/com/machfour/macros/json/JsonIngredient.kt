package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonIngredient(
    override val id: EntityId = MacrosEntity.NO_ID,
    override val created: Instant = 0,
    override val modified: Instant = 0,

    @SerialName("index_name")
    val indexName: String,
    val quantity: JsonQuantity,
    val servingName: String? = null,
    val notes: String? = null,
): JsonEntity() {

}