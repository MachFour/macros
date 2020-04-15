package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.core.Schema;
import com.machfour.macros.core.Table;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.*;
import com.machfour.macros.storage.CsvExport;
import com.machfour.macros.storage.MacrosDatabase;

import javax.crypto.Mac;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Arrays;
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

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        try {
             out.println("Exporting foods...");
             CsvExport.exportTable(Food.table(), outputDir, db);
             out.println("Exporting nutrition data...");
             CsvExport.exportTable(NutritionData.table(), outputDir, db);
             out.println("Exporting servings...");
             CsvExport.exportTable(Serving.table(), outputDir, db);
             out.println("Exporting ingredients...");
             CsvExport.exportTable(Ingredient.table(), outputDir, db);
             out.println("Exporting meals...");
            CsvExport.exportTable(Meal.table(), outputDir, db);
            out.println("Exporting food portions...");
            CsvExport.exportTable(FoodPortion.table(), outputDir, db);
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
