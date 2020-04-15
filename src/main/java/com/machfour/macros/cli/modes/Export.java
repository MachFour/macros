package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.Ingredient;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.storage.CsvExport;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.linux.Config.PROGNAME;

public class Export extends CommandImpl {
    private static final String NAME = "export";
    private static final String USAGE = String.format("%s %s [output dir]", PROGNAME, NAME);

    public Export() {
        super(NAME, USAGE);
    }

    @Override
    public void printHelp() {
        out.println("Exports complete CSV data (foods and servings) from the database.");
        out.println("Please specify the containing directory. It will be created if it doesn't exist.");
    }

    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        }

        // default output dir
        String outputDir = Config.CSV_OUTPUT_DIR;

        if (args.size() >= 2) {
            outputDir = args.get(1);
        }

        String foodCsvFile = Config.joinPath(outputDir, Config.FOOD_CSV_NAME);
        String nutritionDataCsvFile = Config.joinPath(outputDir, Config.NUTRITION_DATA_CSV_NAME);
        String servingCsvFile = Config.joinPath(outputDir, Config.SERVING_CSV_NAME);
        String ingredientsCsvFile = Config.joinPath(outputDir, Config.INGREDIENTS_CSV_NAME);

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        try (Writer foodCsv = new FileWriter(foodCsvFile);
             Writer nutritionDataCsv = new FileWriter(nutritionDataCsvFile);
             Writer servingCsv = new FileWriter(servingCsvFile);
             Writer ingredientsCsv = new FileWriter(ingredientsCsvFile)) {
                out.println("Exporting foods...");
                CsvExport.exportTable(Food.table(), foodCsv, db);
                out.println("Exporting nutrition data...");
                CsvExport.exportTable(NutritionData.table(), nutritionDataCsv, db);
                out.println("Exporting servings...");
                CsvExport.exportTable(Serving.table(), servingCsv, db);
                out.println("Exporting ingredients...");
                CsvExport.exportTable(Ingredient.table(), ingredientsCsv, db);
        } catch (SQLException e1) {
            out.println();
            out.println("SQL Exception occurred: " + e1.getMessage());
            return;
        } catch (IOException e2) {
            out.println();
            out.println("IO exception occurred: " + e2.getMessage());
            return;
        }

        out.println();
        out.println("Export completed successfully");
    }
}
