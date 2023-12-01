package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.interactive.FoodEditor
import com.machfour.macros.cli.utils.ArgParsingResult
import com.machfour.macros.cli.utils.findArgument
import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.queries.getFoodIdsByIndexName
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import java.io.IOException

class AddFood(config: CliConfig) : CommandImpl(config) {
    override val name: String = "addfood"
    override val usage: String = "Usage: ${config.programName} $name <index name>"

    companion object {
        /*
         * Ensures the given index name is not already in the database; returns true if it is not present.
         */
        @Throws(SqlException::class)
        private fun checkIndexName(ds: SqlDatabase, indexName: String): Boolean {
            return getFoodIdsByIndexName(ds, listOf(indexName)).isEmpty()
        }
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 2
        }
        val indexNameArg = findArgument(args, 1)
        if (indexNameArg !is ArgParsingResult.KeyValFound) {
            println(usage)
            return 2
        }
        val indexName = indexNameArg.argument

        val ds = config.database

        try {
            // TODO move this check inside MacrosBuilder validations
            if (!checkIndexName(ds, indexName)) {
                println("Index name $indexName already exists in the database, cannot continue.")
                return 1
            }
        } catch (e: SqlException) {
            println(e.message)
            return 1
        }

        val foodBuilder = MacrosBuilder(FoodTable).apply {
            setField(FoodTable.INDEX_NAME, indexName)
            markFixed(FoodTable.INDEX_NAME)
        }

        var editor: FoodEditor? = null
        var editorInitialised = false
        try {
            try {
                editor = FoodEditor(ds, foodBuilder)
                editor.init()
                editorInitialised = true
                editor.run()
            } catch (e: IOException) {
                println("IO Error: " + e.localizedMessage)
                return 1
            } finally {
                if (editorInitialised) {
                    editor!!.deInit()
                }
            }
        } catch (e: IOException) {
            println("IO Error: " + e.localizedMessage)
            return 1
        }

        return 0
    }

}
