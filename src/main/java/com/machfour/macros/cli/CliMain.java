package com.machfour.macros.cli;

import com.machfour.macros.objects.Food;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.linux.Config;
import com.machfour.macros.storage.CsvStorage;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.util.StringJoiner;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CliMain {
    private static final String PROGNAME = "macros";
    private static final PrintStream OUT = System.out;
    private static final PrintStream ERR = System.err;

    private static final Mode IMPORT = new Import();
    private static final Mode INIT = new Init();
    private static final Mode READ = new Read();
    private static final Mode HELP = new Help();
    private static final Mode SEARCH = new SearchFood();
    private static final Mode LISTFOOD = new ListFood();
    private static final Mode NO_ARGS = new NoArgs();
    private static final Mode INVALID_MODE = new InvalidMode();
    private static final Mode[] MODES = {
            IMPORT
            , READ
            , INIT
            , SEARCH
            , LISTFOOD
            , HELP
            , INVALID_MODE
            , NO_ARGS
    };
    private static final Map<String, Mode> MODES_BY_NAME;

    static {
        MODES_BY_NAME = new HashMap<>();
        for (Mode m : MODES) {
            assert !MODES_BY_NAME.containsKey(m.name()): "Two modes have the same name";
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
            LinuxDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
            try {
                db.deleteIfExists(Config.DB_LOCATION);
                OUT.printf("Deleted database at %s\n", Config.DB_LOCATION);
            } catch (IOException e) {
                e.printStackTrace(OUT);
                OUT.println();
                OUT.println("Error deleting the database");
                return;
            }
            try {
                db.initDb();
            } catch (SQLException | IOException e) {
                e.printStackTrace(OUT);
                OUT.println();
                OUT.println("Error initialising the database");
            }
            OUT.printf("Database re-initialised at %s\n", Config.DB_LOCATION);
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
            MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

            try {
                OUT.println("Importing data into database...");
                CsvStorage.importFoodData(foodCsvFile, db, false);
                OUT.println("Saved foods and nutrition data");
                CsvStorage.importServings(servingCsvFile, db, false);
                OUT.println("Saved servings");
            } catch (SQLException | IOException e) {
                OUT.println();
                e.printStackTrace(OUT);
                return;
            }

            OUT.println();
            OUT.println("Import completed successfully");
        }
    }

    static class SearchFood extends ModeImpl {
        private static final String NAME = "search";
        @Override
        public String name() {
            return NAME;
        }
        @Override
        public void doAction(String[] args) {
            assert args.length > 0;
            if (args.length == 1) {
                OUT.printf("Usage: %s %s <keyword>\n", PROGNAME, args[0]);
                OUT.println("Please enter a search keyword for the food database");
                return;
            }
            MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
            String keyword = args[1];
            List<Food> resultFoods = Collections.emptyList();
            try {
                List<Long> resultIds = db.foodSearch(keyword);
                if (!resultIds.isEmpty()) {
                    resultFoods = db.getFoodsById(resultIds);
                }
            } catch (SQLException e) {
                OUT.print("SQL exception occurred: ");
                OUT.println(e.getErrorCode());
                return;
            }
            if (resultFoods.isEmpty()) {
                OUT.printf("No matches for keyword '%s'\n", keyword);
            } else {
                OUT.println("Search results:");
                OUT.println();
                OUT.printf("%-40s        %-15s\n", "Food name", "index name");
                OUT.println(new StringJoiner<>(Collections.nCopies(63, "=")).join());
                for (Food f : resultFoods) {
                    OUT.printf("%-40s        %-15s\n", f.getMediumName(), f.getIndexName());
                }
            }
        }
    }

    static class ListFood extends ModeImpl {
        private static final String NAME = "list";
        @Override
        public String name() {
            return NAME;
        }
        @Override
        public void doAction(String[] args) {
            assert args.length > 0;
            if (args.length == 1) {
                OUT.printf("Usage: %s %s <index_name>\n", PROGNAME, args[0]);
                OUT.println("Please enter the index name of the food to list");
                return;
            }
            MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
            String indexName = args[1];
            Food foodToList = null;
            try {
                foodToList = db.getFoodByIndexName(indexName);
            } catch (SQLException e) {
                OUT.print("SQL exception occurred: ");
                OUT.println(e.getErrorCode());
            }
            if (foodToList == null) {
                OUT.printf("No food found with index name %s\n", indexName);
            } else {
                //TODO
                OUT.printf("Food data for '%s':\n", indexName);
                OUT.println(foodToList.getAllData());
                OUT.printf("Nutrition data for '%s':\n", indexName);
                OUT.println(foodToList.getNutritionData());
            }
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
                OUT.println("Usage: macros --read <file>");
                OUT.println();
                OUT.println("Please specify a file to read");
                return;
            }
            String filename = args[1];
            MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
            FileParser fileParser = new FileParser(db);
            List<Meal> meals;
            try {
                meals = fileParser.parseFile(filename);
            } catch (IOException e1) {
                ERR.println("IO exception occurred: " + e1.getMessage());
                return;
            } catch (SQLException e2) {
                ERR.println("SQL exception occurred: " + e2.getMessage());
                return;
            }
            MealPrinter mp = new MealPrinter(OUT);
            mp.printMeals(meals);
            Map<String, String> errors = fileParser.getErrorLines();
            if (!errors.isEmpty()) {
                OUT.println();
                OUT.println("Warning: the following lines were skipped");
                for (Map.Entry<String, String> line : errors.entrySet()) {
                    OUT.printf("'%s' - %s\n", line.getKey(), line.getValue());
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
            OUT.println("Help!");
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
            OUT.printf("Mode not recognised: '%s'\n", args[0]);
            OUT.println();
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
            OUT.println("Please specify one of the following modes:");
            for (Mode m : MODES) {
                if (m.isUserMode()) {
                    OUT.println(m.name());
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
