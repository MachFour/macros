package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsing
import com.machfour.macros.cli.utils.CliUtils
import com.machfour.macros.cli.utils.FileParser
import com.machfour.macros.cli.utils.MealSpec
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.core.schema.FoodPortionTable
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.queries.MealQueries
import com.machfour.macros.queries.WriteQueries
import com.machfour.macros.persistence.MacrosDatabase

import java.sql.SQLException


class Edit(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "edit"
        private const val USAGE = "Usage: $programName $NAME [meal [day]]"

        private fun interactiveHelpString(): String {
            return ("Actions:"
                    + "\n" + "a   - add a new food portion"
                    + "\n" + "d   - delete a food portion"
                    + "\n" + "D   - delete the entire meal"
                    + "\n" + "e   - edit a food portion"
                    + "\n" + "m   - move a food portion to another meal"
                    + "\n" + "n   - change the name of the meal"
                    + "\n" + "s   - show current food portions"
                    + "\n" + "?   - print this help"
                    + "\n" + "x/q - exit this editor")
        }
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }

        val mealNameArg = ArgParsing.findArgument(args, 1)
        val dayArg = ArgParsing.findArgument(args, 2)

        val ds = config.database

        val mealSpec = MealSpec.makeMealSpec(mealNameArg, dayArg)
        mealSpec.process(ds, true)

        if (mealSpec.error != null) {
            err.println(mealSpec.error)
            return 1
        }
        if (mealSpec.isCreated) {
            val createMsg = String.format("Created meal '%s' on %s", mealSpec.name, mealSpec.day)
            out.println(createMsg)
        }
        val toEdit = mealSpec.processedObject
        return startEditor(ds, toEdit!!.id)
    }

    private fun startEditor(ds: MacrosDatabase, mealId: Long): Int {
        val toEdit: Meal?
        try {
            toEdit = MealQueries.getMealById(ds, mealId)
        } catch (e: SQLException) {
            err.println(e)
            return 1
        }
        requireNotNull(toEdit) { "Could not re-retrieve meal with id given by processed MealSpec" }
        require(toEdit.objectSource === ObjectSource.DATABASE) { "Not editing an object from the database" }

        while (true) {
            // TODO reload meal
            out.println()
            out.println("Editing meal: ${toEdit.name} on ${toEdit.day.prettyPrint()}")
            out.println()
            out.print("Action (? for help): ")
            val action = CliUtils.getChar(input, out)
            out.println()
            when (action) {
                'a' -> addPortion(toEdit, ds)
                'd' -> {
                    deleteFoodPortion(toEdit, ds)
                    out.println("WARNING: meal is not reloaded")
                }
                'D' -> deleteMeal(toEdit, ds)
                'e' -> {
                    editFoodPortion(toEdit, ds)
                    out.println("WARNING: meal is not reloaded")
                }
                'm' -> {
                    out.println("Meal")
                    err.println("Not implemented yet, sorry!")
                }
                'n' -> {
                    renameMeal()
                    err.println("WARNING: meal is not reloaded")
                }
                's' -> showFoodPortions(toEdit)
                '?' -> {
                    out.println()
                    out.println("Please choose from one of the following options")
                    out.println(interactiveHelpString())
                }
                'x', 'q', '\u0000' -> return 0
                else -> {
                    out.println("Unrecognised action: '${action}'")
                    out.println()
                }
            }// TODO exit if deleted
        }
    }

    private fun addPortion(toEdit: Meal, db: MacrosDatabase) {
        out.println("Please enter the portion information (see help for how to specify a food portion)")
        // copy from portion
        val inputString = CliUtils.getStringInput(input, out)
        if (inputString != null && inputString.isNotEmpty()) {
            val spec = FileParser.makefoodPortionSpecFromLine(inputString)
            Portion.process(toEdit, listOf(spec), db, out, err)
        }
    }

    private fun showFoodPortions(toEdit: Meal) {
        out.println("Food portions:")
        val foodPortions = toEdit.getFoodPortions()
        for (i in foodPortions.indices) {
            val fp = foodPortions[i]
            out.println("$i: ${fp.prettyFormat(true)}")
        }
        out.println()
    }

    private fun deleteMeal(toDelete: Meal, db: MacrosDatabase) {
        out.print("Delete meal")
        out.print("Are you sure? [y/N] ")
        if ((CliUtils.getChar(input, out) == 'y') or (CliUtils.getChar(input, out) == 'Y')) {
            try {
                WriteQueries.deleteObject(db, toDelete)
            } catch (e: SQLException) {
                out.println("Error deleting meal: " + e.message)
            }

        }
    }

    private fun deleteFoodPortion(toEdit: Meal, ds: MacrosDatabase) {
        out.println("Delete food portion")
        showFoodPortions(toEdit)
        out.print("Enter the number of the food portion to delete and press enter: ")
        val portions = toEdit.getFoodPortions()
        val n = CliUtils.getIntegerInput(input, out, 0, portions.size - 1)
        if (n == null) {
            out.println("Invalid number")
            return
        }
        try {
            WriteQueries.deleteObject(ds, portions[n])
        } catch (e3: SQLException) {
            out.println("Error deleting the food portion: " + e3.message)
            return
        }

        out.println("Deleted the food portion")
        out.println()
    }

    private fun editFoodPortion(m: Meal, ds: MacrosDatabase) {
        out.println("Edit food portion")
        showFoodPortions(m)
        out.print("Enter the number of the food portion to edit and press enter: ")
        val portions = m.getFoodPortions()
        val n = CliUtils.getIntegerInput(input, out, 0, portions.size - 1)
        if (n == null) {
            out.println("Invalid number")
            return
        }
        out.print("Enter a new quantity (in the same unit) and press enter: ")
        val newQty = CliUtils.getDoubleInput(input, out)
        if (newQty == null) {
            out.println("Invalid quantity")
            return
        }

        try {
            val newData = portions[n].dataCopy(withMetadata = false)
            newData.put(FoodPortionTable.QUANTITY, newQty)
            WriteQueries.saveObject(ds, FoodPortion.factory.construct(newData, ObjectSource.DB_EDIT))
        } catch (e3: SQLException) {
            out.println("Error modifying the food portion: " + e3.message)
            return
        }

        out.println("Successfully saved the food portion")
        out.println()
    }

    private fun renameMeal() {
        out.println("Rename meal")
        out.print("Type a new name and press enter: ")
        val newName = CliUtils.getStringInput(input, out) ?: return
        out.println("The new name is: $newName")
    }

    override fun printHelp() {
        out.println(USAGE)
        out.println("Interactive meal editor")
        out.println()
        out.println()
        out.println(interactiveHelpString())
        out.println()
        out.println("Food portions can be entered in one of the following forms:")
        out.println("1. <food index name>, <quantity>[quantity unit]")
        out.println("2. <food index name>, [<serving name>], <serving count> (omit serving name for default serving)")
        out.println("3. <food index name> (this means 1 of the default serving)")
        out.println("(<> denotes a mandatory argument and [] denotes an optional argument)")
    }

}
