package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsing
import com.machfour.macros.cli.utils.MealSpec


class NewMeal : CommandImpl(NAME, USAGE) {
    companion object {
        private const val NAME = "newmeal"
        private val USAGE = "Usage: $programName $NAME <meal name> [<day>]"
    }

    override fun doAction(args: List<String>) : Int {
        if (args.size == 1 || args.contains("--help")) {
            printHelp()
            return -1
        }

        // cases: day not specified vs day specified
        // meal exists with that name, or doesn't exist
        val ds = config.dataSourceInstance

        val mealNameArg = ArgParsing.findArgument(args, 1)
        val dayArg = ArgParsing.findArgument(args, 2)
        val mealSpec = MealSpec.makeMealSpec(mealNameArg, dayArg)

        mealSpec.process(ds, true)

        if (mealSpec.error != null) {
            out.println(mealSpec.error)
            return 1
        }
        if (mealSpec.isCreated) {
            val prettyDay = mealSpec.day!!.prettyPrint()
            out.println("Created meal '${mealSpec.name}' on $prettyDay")
        }
        //Meal toEdit = mealSpec.processedObject();
        return 0
    }

}
