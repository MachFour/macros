package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.storage.CsvStorage;
import com.machfour.macros.storage.MacrosDatabase;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

class Import extends CommandImpl {
    private static final String NAME = "import";
    private static final String USAGE = String.format("%s %s", PROGNAME, NAME);

    Import() {
        super(NAME, USAGE);
    }

    @Override
    public void printHelp() {
        out.println("Imports CSV data (foods and servings) into the database.");
        out.println("Only foods with index names not already in the database will be imported.");
        out.println("However, it will try to import all servings, and so will fail if duplicate servings exist.");
    }

    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        }
        boolean doClear = args.contains("--clear");

        String foodCsvFile = Config.FOOD_CSV_FILENAME;
        String servingCsvFile = Config.SERVING_CSV_FILENAME;
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        try (Reader foodCsv = new FileReader(foodCsvFile);
             Reader servingCsv = new FileReader(servingCsvFile);
        ) {
            if (doClear) {
                out.println("Clearing existing food and serving data...");
                db.clearTable(Food.table());
                db.clearTable(NutritionData.table());
                db.clearTable(Serving.table());
            }
            out.println("Importing data into database...");
            CsvStorage.importFoodData(foodCsv, db, false);
            out.println("Saved foods and nutrition data");
            CsvStorage.importServings(servingCsv, db, false);
            out.println("Saved servings");
        } catch (SQLException e1) {
            out.println();
            out.println("SQL Exception occurred: " + e1.getMessage());
            out.println("Please check the format of the CSV files");
            return;
        } catch (IOException e2) {
            out.println();
            out.println("IO exception occurred: " + e2.getMessage());
            return;
        }

        out.println();
        out.println("Import completed successfully");
    }
}
