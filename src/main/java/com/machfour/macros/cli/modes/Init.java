package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;



public class Init extends CommandImpl {
    private static final String NAME = "init";
    private static final String USAGE = String.format("%s %s", config.getProgramName(), NAME);

    public Init() {
        super(NAME, USAGE);
    }

    @Override
    public void printHelp() {
        out.println("Recreates and initialises the database. All previous data is deleted!");
    }

    @Override
    public void doActionNoExitCode(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        }
        MacrosDatabase db = config.getDatabaseInstance();
        try {
            db.deleteIfExists(config.getDbLocation());
            out.printf("Deleted database at %s\n", config.getDbLocation());
        } catch (IOException e) {
            out.println();
            out.println("Error deleting the database: " + e.getMessage());
            return;
        }
        try {
            db.initDb();
        } catch (SQLException | IOException e) {
            out.println();
            out.println("Error initialising the database: " + e.getMessage());
        }
        out.printf("Database re-initialised at %s\n", config.getDbLocation());
    }
}
