package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.entities.Food
import com.machfour.macros.json.*
import com.machfour.macros.queries.StaticDataSource
import com.machfour.macros.sql.SqlException
import kotlinx.coroutines.runBlocking
import java.io.IOException

private enum class ArchiveType {
    Unknown, Zip, TarGz
}

class Json(config: CliConfig): CommandImpl(config) {
    override val name = "json"
    override val usage = "Usage: ${config.programName} $name --export path/to/archive.ext (ext = zip or tar.gz)\n" +
                         "       ${config.programName} $name --import path/to/archive.ext"

    override fun printHelp() {
        println(usage)
        println("Exports or imports food database to an archive file consisting of one JSON record " +
            "per food. Some information such as creation/modification time is not preserved."
        )
    }

    private fun handleUnrecognisedArchiveType(): Int {
        println("Unrecognised archive extension")
        println(usage)
        return 2
    }

    private fun parseArchiveExtension(archivePath: String): ArchiveType {
        return when {
            archivePath.endsWith(".zip") -> ArchiveType.Zip
            archivePath.endsWith(".tar.gz") -> ArchiveType.TarGz
            else -> ArchiveType.Unknown
        }
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        if (args.size < 3) {
            println(usage)
            return 2
        }

        val archivePath = args[2]
        val archiveType = parseArchiveExtension(archivePath)

        return when(args[1]) {
            "--import" -> { import(archivePath, archiveType, false) }
            "--export" -> { export(archivePath, archiveType) }
            else -> {
                println(usage)
                return 2
            }
        }
    }

    private fun export(destPath: String, archiveType: ArchiveType): Int {
        val serializeFoods: (List<JsonFood>) -> Unit = when(archiveType) {
            ArchiveType.Zip -> { { serializeFoodsToZipFile(it, JsonSerializer, destPath) } }
            ArchiveType.TarGz -> { { serializeFoodsToTarGzFile(it, JsonSerializer, destPath) } }
            ArchiveType.Unknown -> return handleUnrecognisedArchiveType()
        }

        val dataSource = StaticDataSource(config.database)
        try {
            var foods: Map<Long, Food> = HashMap()
            runBlocking {
                dataSource.getAllFoods().collect { foods = it }
            }
            val jsonFoods = foods.values.map { JsonFood(it) }
            serializeFoods(jsonFoods)
            println("Exported ${foods.size} foods to $destPath")
        } catch (e: SqlException) {
            return handleException(e)
        } catch (e: IOException) {
            return handleException(e)
        }

        return 0
    }

    private fun import(srcPath: String, archiveType: ArchiveType, clear: Boolean): Int {
        val deserializeFoods: () -> List<JsonFood> = when(archiveType) {
            ArchiveType.Zip -> { { deserializeFoodsFromZipFile(srcPath, JsonSerializer) } }
            ArchiveType.TarGz -> { { deserializeFoodsFromTarGzFile(srcPath, JsonSerializer) } }
            ArchiveType.Unknown -> return handleUnrecognisedArchiveType()
        }

        if (clear) {
            clearFoodsAndServings(config.database)
        }

        val foods = try {
            deserializeFoods()
        } catch (e: IOException) {
            print(e.message)
            return 1
        }

        foods.takeIf { it.isNotEmpty() }?.let {
            it.forEach { println(it.indexName) }
            println()
        }

        println("Read ${foods.size} foods")
        println()
        println("Unfortunately, Json import is not fully implemented yet. Sorry!")
        return 0
    }

    private fun handleException(e: Exception): Int {
        println()
        printlnErr("Exception occurred (${e.javaClass}). Message: ${e.message}")
        return 1
    }
}
