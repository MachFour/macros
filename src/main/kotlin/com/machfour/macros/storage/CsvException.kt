package com.machfour.macros.storage

class CsvException : RuntimeException {
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
    constructor(message: String, cause: Throwable): super(message, cause)

}