package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;

class Init extends CommandImpl {
    private static final String NAME = "init";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        out.println("Recreates and initialises the database. All previous data is deleted!");
    }
    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp(OUT);
            return;
        }
        LinuxDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        try {
            db.deleteIfExists(Config.DB_LOCATION);
            OUT.printf("Deleted database at %s\n", Config.DB_LOCATION);
        } catch (IOException e) {
            OUT.println();
            OUT.println("Error deleting the database: " + e.getMessage());
            return;
        }
        try {
            db.initDb();
        } catch (SQLException | IOException e) {
            OUT.println();
            OUT.println("Error initialising the database: " + e.getMessage());
        }
        OUT.printf("Database re-initialised at %s\n", Config.DB_LOCATION);
    }
}
