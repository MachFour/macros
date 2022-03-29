package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.*
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.queries.deleteObject
import com.machfour.macros.queries.getMealById
import com.machfour.macros.queries.saveObject
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException


private fun addPortion(toEdit: Meal, db: SqlDatabase) {
    println("Please enter the portion information (see help for how to specify a food portion)")
    // copy from portion
    val inputString = cliGetStringInput()
    if (inputString != null && inputString.isNotEmpty()) {
        val spec = FileParser.makefoodPortionSpecFromLine(inputString)
        processPortions(toEdit, listOf(spec), db)
    }
}

private fun showFoodPortions(toEdit: Meal) {
    println("Food portions:")
    val foodPortions = toEdit.foodPortions
    for (i in foodPortions.indices) {
        val fp = foodPortions[i]
        println("$i: ${fp.prettyFormat(true)}")
    }
    println()
}

private fun deleteMeal(toDelete: Meal, db: SqlDatabase) {
    print("Delete meal")
    print("Are you sure? [y/N] ")
    if ((cliGetChar() == 'y') or (cliGetChar() == 'Y')) {
        try {
            deleteObject(db, toDelete)
        } catch (e: SqlException) {
            println("Error deleting meal: " + e.message)
        }

    }
}

private fun deleteFoodPortion(toEdit: Meal, ds: SqlDatabase) {
    println("Delete food portion")
    showFoodPortions(toEdit)
    print("Enter the number of the food portion to delete and press enter: ")
    val portions = toEdit.foodPortions
    val n = cliGetInteger(0, portions.size - 1)
    if (n == null) {
        println("Invalid number")
        return
    }
    try {
        deleteObject(ds, portions[n])
    } catch (e3: SqlException) {
        println("Error deleting the food portion: " + e3.message)
        return
    }

    println("Deleted the food portion")
    println()
}

private fun editFoodPortion(m: Meal, ds: SqlDatabase) {
    println("Edit food portion")
    showFoodPortions(m)
    print("Enter the number of the food portion to edit and press enter: ")
    val portions = m.foodPortions
    val n = cliGetInteger(0, portions.size - 1)
    if (n == null) {
        println("Invalid number")
        return
    }
    print("Enter a new quantity (in the same unit) and press enter: ")
    val newQty = cliGetDouble()
    if (newQty == null) {
        println("Invalid quantity")
        return
    }

    try {
        val newData = portions[n].dataCopy(withMetadata = false)
        newData.put(FoodPortionTable.QUANTITY, newQty)
        saveObject(ds, FoodPortion.factory.construct(newData, ObjectSource.DB_EDIT))
    } catch (e3: SqlException) {
        println("Error modifying the food portion: " + e3.message)
        return
    }

    println("Successfully saved the food portion")
    println()
}

private fun renameMeal() {
    println("Rename meal")
    print("Type a new name and press enter: ")
    val newName = cliGetStringInput() ?: return
    println("The new name is: $newName")
}


private const val interactiveHelpString =
    "Actions:" +
        "\n" + "a   - add a new food portion" +
        "\n" + "d   - delete a food portion" +
        "\n" + "D   - delete the entire meal" +
        "\n" + "e   - edit a food portion" +
        "\n" + "m   - move a food portion to another meal" +
        "\n" + "n   - change the name of the meal" +
        "\n" + "s   - show current food portions" +
        "\n" + "?   - print this help" +
        "\n" + "x/q - exit this editor"

private const val nonInteractiveHelpString =
    "Food portions can be entered in one of the following forms:" +
        "\n" + "1. <food index name>, <quantity>[quantity unit]" +
        "\n" + "2. <food index name>, [<serving name>], <serving count> (omit serving name for default serving)" +
        "\n" + "3. <food index name> (this means 1 of the default serving)" +
        "\n" + "(<> denotes a mandatory argument and [] denotes an optional argument)"

class Edit(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "edit"
        private const val USAGE = "Usage: $programName $NAME [meal [day]]"

    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }

        val mealNameArg = findArgument(args, 1)
        val dayArg = findArgument(args, 2)

        val ds = config.database

        val mealSpec = MealSpec.makeMealSpec(mealNameArg, dayArg)
        mealSpec.process(ds, true)

        if (mealSpec.error != null) {
            printlnErr(mealSpec.error)
            return 1
        }
        if (mealSpec.isCreated) {
            val createMsg = String.format("Created meal '%s' on %s", mealSpec.name, mealSpec.day)
            println(createMsg)
        }
        val toEdit = mealSpec.processedObject
        return startEditor(ds, toEdit!!.id)
    }

    private fun startEditor(ds: SqlDatabase, mealId: Long): Int {
        val toEdit: Meal?
        try {
            toEdit = getMealById(ds, mealId)
        } catch (e: SqlException) {
            printlnErr(e)
            return 1
        }
        requireNotNull(toEdit) { "Could not re-retrieve meal with id given by processed MealSpec" }
        require(toEdit.source === ObjectSource.DATABASE) { "Not editing an object from the database" }

        while (true) {
            // TODO reload meal
            println()
            println("Editing meal: ${toEdit.name} on ${toEdit.day.prettyPrint()}")
            println()
            print("Action (? for help): ")
            val action = cliGetChar()
            println()
            when (action) {
                'a' -> addPortion(toEdit, ds)
                'd' -> {
                    deleteFoodPortion(toEdit, ds)
                    println("WARNING: meal is not reloaded")
                }
                'D' -> deleteMeal(toEdit, ds)
                'e' -> {
                    editFoodPortion(toEdit, ds)
                    println("WARNING: meal is not reloaded")
                }
                'm' -> {
                    println("Meal")
                    printlnErr("Not implemented yet, sorry!")
                }
                'n' -> {
                    renameMeal()
                    printlnErr("WARNING: meal is not reloaded")
                }
                's' -> showFoodPortions(toEdit)
                '?' -> {
                    println()
                    println("Please choose from one of the following options")
                    println(interactiveHelpString)
                }
                'x', 'q', '\u0000' -> return 0
                else -> {
                    println("Unrecognised action: '${action}'")
                    println()
                }
            }// TODO exit if deleted
        }
    }

    override fun printHelp() {
        println(USAGE)
        println("Interactive meal editor")
        println()
        println()
        println(interactiveHelpString)
        println()
        println(nonInteractiveHelpString)
    }

}
