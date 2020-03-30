package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliMain.ERR;


class Read extends CommandImpl {
    private static final String NAME = "read";
    private static final String USAGE = String.format("Usage: %s %s <file> [-v | --verbose] [--per100]", PROGNAME, NAME);

    Read() {
        super(NAME, USAGE);
    }

    @Override
    public void doAction(List<String> args) {
        if (args.size() < 2) {
            printHelp();
            out.println();
            out.println("Please specify a file to read");
            return;
        } else if (args.contains("--help")) {
            printHelp();
            return;
        }
        String filename = args.get(1);
        boolean verbose = args.contains("--verbose") || args.contains("-v");
        boolean per100 = args.contains("--per100");

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        FileParser fileParser = new FileParser();
        List<Meal> meals;
        try (Reader r = new FileReader(filename)) {
            meals = fileParser.parseFile(r, db);
        } catch (IOException e1) {
            ERR.println("IO exception occurred: " + e1.getMessage());
            return;
        } catch (SQLException e2) {
            ERR.println("SQL exception occurred: " + e2.getMessage());
            return;
        }
        MealPrinter.printMeals(meals, OUT, verbose, per100, true);
        Map<String, String> errors = fileParser.getErrorLines();
        if (!errors.isEmpty()) {
            out.println();
            out.println("Warning: the following lines were skipped");
            for (Map.Entry<String, String> line : errors.entrySet()) {
                out.printf("'%s' - %s\n", line.getKey(), line.getValue());
            }
        }
    }
}

