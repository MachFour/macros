package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.storage.CsvStorage;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;

class Import extends ModeImpl {
    private static final String NAME = "import";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void doAction(List<String> args) {
        String foodCsvFile = Config.FOOD_CSV_FILENAME;
        String servingCsvFile = Config.SERVING_CSV_FILENAME;
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        try {
            OUT.println("Importing data into database...");
            CsvStorage.importFoodData(foodCsvFile, db, false);
            OUT.println("Saved foods and nutrition data");
            CsvStorage.importServings(servingCsvFile, db, false);
            OUT.println("Saved servings");
        } catch (SQLException e1) {
            OUT.println();
            OUT.println("SQL Exception occurred: " + e1.getMessage());
            OUT.println("Please check the format of the CSV files");
            return;
        } catch (IOException e2) {
            OUT.println();
            OUT.println("IO exception occurred: " + e2.getMessage());
            return;
        }

        OUT.println();
        OUT.println("Import completed successfully");
    }
}
