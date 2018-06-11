package com.machfour.macros.cli;

import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.Config;
import com.machfour.macros.storage.CsvStorage;
import com.machfour.macros.linux.MacrosLinuxDatabase;
import com.machfour.macros.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CliMain {
    private static final PrintStream out = System.out;
    private static final PrintStream err = System.err;

    private static final Mode IMPORT = new Import();
    private static final Mode INIT = new Init();
    private static final Mode READ = new Read();
    private static final Mode HELP = new Help();
    private static final Mode NO_ARGS = new NoArgs();
    private static final Mode INVALID_MODE = new InvalidMode();
    private static final Mode[] MODES = {
            IMPORT
            , READ
            , INIT
            , HELP
            , INVALID_MODE
            , NO_ARGS
    };
    private static final Map<String, Mode> MODES_BY_NAME;

    static {
        MODES_BY_NAME = new HashMap<>();
        for (Mode m : MODES) {
            assert MODES_BY_NAME.containsKey(m.name()): "Two modes have the same name";
            MODES_BY_NAME.put(m.name(), m);
        }
    }

    static class Init extends ModeImpl {
        private static final String NAME = "init";
        @Override
        public String name() {
            return NAME;
        }
        @Override
        public void doAction(String[] args) {
            MacrosLinuxDatabase db = MacrosLinuxDatabase.getInstance(Config.DB_LOCATION);
            try {
                db.deleteIfExists(Config.DB_LOCATION);
                out.printf("Deleted database at %s\n", Config.DB_LOCATION);
            } catch (IOException e) {
                e.printStackTrace(out);
                out.println();
                out.println("Error deleting the database");
                return;
            }
            try {
                db.initDb();
            } catch (SQLException | IOException e) {
                e.printStackTrace(out);
                out.println();
                out.println("Error initialising the database");
            }
            out.printf("Database re-initialised at %s\n", Config.DB_LOCATION);
        }
    }

    static class Import extends ModeImpl {
        private static final String NAME = "import";
        @Override
        public String name() {
            return NAME;
        }
        @Override
        public void doAction(String[] args) {
            String foodCsvFile = Config.FOOD_CSV_FILENAME;
            String servingCsvFile = Config.SERVING_CSV_FILENAME;
            MacrosLinuxDatabase db = MacrosLinuxDatabase.getInstance(Config.DB_LOCATION);

            Pair<List<Food>, List<NutritionData>> csvObjects;
            List<Serving> csvServings;
            try {
                out.printf("Importing food data from CSV file: %s\n", foodCsvFile);
                csvObjects = CsvStorage.buildFoodObjectTree(foodCsvFile);
                out.printf("Importing servings from CSV file: %s\n", servingCsvFile);
                csvServings = CsvStorage.buildServings(servingCsvFile);
            } catch (IOException e) {
                out.println();
                e.printStackTrace(out);
                out.println();
                out.println("No data was imported.");
                return;
            }

            try {
                out.println("Saving data into database...");
                db.saveObjects(csvObjects.first, ObjectSource.IMPORT);
                out.println("Saved food data");
                List<NutritionData> completedNd = db.completeForeignKeys(csvObjects.second, Schema.NutritionDataTable.FOOD_ID);
                db.saveObjects(completedNd, ObjectSource.IMPORT);
                out.println("Saved nutrition information");
                List<Serving> completedServings = db.completeForeignKeys(csvServings, Schema.ServingTable.FOOD_ID);
                db.saveObjects(completedServings, ObjectSource.IMPORT);
                out.println("Saved servings");
            } catch (SQLException e) {
                out.println();
                e.printStackTrace(out);
                return;
            }

            out.println();
            out.println("Import completed successfully");
        }
    }

    static class Read extends ModeImpl {
        private static final String NAME = "read";
        @Override
        public String name() {
            return NAME;
        }
        @Override
        public void doAction(String[] args) {
            if (args.length < 2) {
                out.println("Usage: macros --read <file>");
                out.println();
                out.println("Please specify a file to read");
                return;
            }
            String filename = args[1];
            MacrosLinuxDatabase db = MacrosLinuxDatabase.getInstance(Config.DB_LOCATION);
            FileParser fileParser = new FileParser(db);
            List<Meal> meals;
            try {
                meals = fileParser.parseFile(filename);
            } catch (IOException e1) {
                err.println("IO exception occurred: " + e1.getMessage());
                return;
            } catch (SQLException e2) {
                err.println("SQL exception occurred: " + e2.getMessage());
                return;
            }
            MealPrinter mp = new MealPrinter(out);
            mp.printMeals(meals);
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
    static class Help extends ModeImpl {
        private static final String NAME = "help";
        @Override
        public String name() {
            return NAME;
        }
        @Override
        public void doAction(String[] args) {
            out.println("Help!");
        }
    }
    static class InvalidMode extends ModeImpl {
        private static final String NAME = "_invalidMode";
        @Override
        public String name() {
            return NAME;
        }
        @Override
        public void doAction(String[] args) {
            out.printf("Mode not recognised: '%s'\n", args[0]);
            out.println();
            NO_ARGS.doAction(new String[0]);
        }
    }
    static class NoArgs extends ModeImpl {
        private static final String NAME = "_noArgs";
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public void doAction(String[] args) {
            out.println("Please specify one of the following modes:");
            for (Mode m : MODES) {
                if (m.isUserMode()) {
                    out.println(m.name());
                }
            }
        }
    }

    private static @NotNull Mode parseMode(String modeString) {
        Mode defaultMode = INVALID_MODE;
        if (modeString == null) {
            return defaultMode;
        }
        String cleanedModeString = modeString.trim();
        if (cleanedModeString.startsWith("--")) {
            cleanedModeString = cleanedModeString.substring(2);
        }
        return MODES_BY_NAME.getOrDefault(cleanedModeString, defaultMode);
    }

    public static void main(String[] args) {
        Mode mode = args.length == 0 ? NO_ARGS : parseMode(args[0]);
        // mode args start from index 1
        mode.doAction(args);
    }
}
