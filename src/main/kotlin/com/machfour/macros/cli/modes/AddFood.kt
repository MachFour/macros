package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsing
import com.machfour.macros.cli.interactive.FoodEditor
import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.Schema
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.storage.MacrosDataSource

import java.io.IOException
import java.sql.SQLException
import java.util.Collections


class AddFood : CommandImpl(NAME, USAGE) {
    companion object {
        private const val NAME = "addfood"
        private val USAGE = "Usage: $programName $NAME <index name>"

        /*
         * Ensures the given index name is not already in the database; returns true if it is not present.
         */
        @Throws(SQLException::class)
        private fun checkIndexName(ds: MacrosDataSource, indexName: String): Boolean {
            return FoodQueries.getFoodIdsByIndexName(ds, listOf(indexName)).isEmpty()
        }
    }

    override fun doActionNoExitCode(args: List<String>) {
        out.println("doAction() is deprecated, using doActionWithExitCode() instead")
        doAction(args)
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return -1
        }
        val (_, indexName, status) = ArgParsing.findArgument(args, 1)
        if (status !== ArgParsing.Status.ARG_FOUND) {
            out.print(usage)
            return -1
        }

        val ds = config.dataSourceInstance

        try {
            // TODO move this check inside MacrosBuilder validations
            if (!checkIndexName(ds, indexName!!)) {
                out.println("Index name $indexName already exists in the database, cannot continue.")
                return 1
            }
        } catch (e: SQLException) {
            out.println(e.message)
            return 1
        }

        val foodBuilder = MacrosBuilder(Food.table).apply {
            setField(Schema.FoodTable.INDEX_NAME, indexName)
            markFixed(Schema.FoodTable.INDEX_NAME)
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
                out.println("IO Error: " + e.localizedMessage)
                return 1
            } finally {
                if (editorInitialised) {
                    editor!!.deInit()
                }
            }
        } catch (e: IOException) {
            out.println("IO Error: " + e.localizedMessage)
            return 1
        }

        return 0
    }

}
