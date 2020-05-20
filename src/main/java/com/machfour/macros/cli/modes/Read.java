package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.FileParser;
import com.machfour.macros.cli.utils.MealPrinter;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDataSource;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;




public class Read extends CommandImpl {
    private static final String NAME = "read";
    private static final String USAGE = String.format("Usage: %s %s <file> [-v | --verbose] [--per100]", config.getProgramName(), NAME);

    public Read() {
        super(NAME, USAGE);
    }

    @Override
    public int doAction(List<String> args) {
        if (args.size() < 2) {
            printHelp();
            out.println();
            out.println("Please specify a file to read");
            return -1;
        } else if (args.contains("--help")) {
            printHelp();
            return -1;
        }
        String filename = args.get(1);
        boolean verbose = args.contains("--verbose") || args.contains("-v");
        boolean per100 = args.contains("--per100");

        MacrosDataSource ds =  config.getDataSourceInstance();


        FileParser fileParser = new FileParser();
        List<Meal> meals;
        try (Reader r = new FileReader(filename)) {
            meals = fileParser.parseFile(r, ds);
        } catch (IOException e1) {
            err.println("IO exception occurred: " + e1.getMessage());
            return 1;
        } catch (SQLException e2) {
            err.println("SQL exception occurred: " + e2.getMessage());
            return 1;
        }
        MealPrinter.printMeals(meals, out, verbose, per100, true);
        Map<String, String> errors = fileParser.getErrorLines();
        if (!errors.isEmpty()) {
            out.println();
            out.println("Warning: the following lines were skipped");
            for (Map.Entry<String, String> line : errors.entrySet()) {
                out.printf("'%s' - %s\n", line.getKey(), line.getValue());
            }
            return 2;
        }
        return 0;
    }
}

