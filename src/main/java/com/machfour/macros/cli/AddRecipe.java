package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.PrintStream;

import static com.machfour.macros.cli.CliMain.PROGNAME;

public class AddRecipe extends CommandImpl {
    private static final String NAME = "addrecipe";
    private static final String USAGE = String.format("Usage: %s %s <recipes.json>", PROGNAME, NAME);

    public AddRecipe() {
        super(NAME, USAGE);
    }

    @Override
    public void doAction(java.util.List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
    }

}
