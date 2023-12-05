package com.machfour.macros.json

import kotlinx.serialization.json.Json

// Default serializer to use for writing JSON files
val JsonSerializer = Json {
    prettyPrint = true
}