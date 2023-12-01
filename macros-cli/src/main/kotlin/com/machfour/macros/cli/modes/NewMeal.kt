package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.findArgument
import com.machfour.macros.cli.utils.makeMealSpec


class NewMeal(config: CliConfig) : CommandImpl(config) {
    override val name = "newmeal"
    override val usage = "Usage: ${config.programName} $name <meal name> [<day>]"

    override fun doAction(args: List<String>): Int {
        if (args.size == 1 || args.contains("--help")) {
            printHelp()
            return 2
        }

        // cases: day not specified vs day specified
        // meal exists with that name, or doesn't exist
        val ds = config.database

        val mealNameArg = findArgument(args, 1)
        val dayArg = findArgument(args, 2)
        val mealSpec = makeMealSpec(mealNameArg, dayArg)

        mealSpec.process(ds, true)

        if (mealSpec.error != null) {
            println(mealSpec.error)
            return 1
        }
        if (mealSpec.isCreated) {
            val prettyDay = mealSpec.day!!.prettyPrint()
            println("Created meal '${mealSpec.name}' on $prettyDay")
        }
        //Meal toEdit = mealSpec.processedObject();
        return 0
    }

}
