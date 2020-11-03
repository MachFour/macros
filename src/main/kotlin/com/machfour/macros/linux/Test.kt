package com.machfour.macros.linux

object Test {

    class Closeable : AutoCloseable {
        @Throws(Exception::class)
        override fun close() {
            throwException()
        }

        @Throws(Exception::class)
        fun throwException() {
            throw Exception()
        }
    }

    @Throws(Exception::class)
    private fun getCloseable() : Closeable? {
        return Closeable()
    }

    @Throws(Exception::class)
    private fun thing(): Int {
        val out: Int

        try {
            getCloseable().use {
                it?.throwException()
                out = 10
            }
        } finally {
            println("finally")
        }
        return out
    }


    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val int = thing()
            println(int)
        } catch (e: Exception) {}
    }
}
