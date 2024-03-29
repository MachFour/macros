package com.machfour.macros.csv

class CsvException : RuntimeException {
    constructor(message: String): super(message)

    @Suppress("unused")
    constructor(cause: Throwable): super(cause)

    @Suppress("unused")
    constructor(message: String, cause: Throwable): super(message, cause)

}