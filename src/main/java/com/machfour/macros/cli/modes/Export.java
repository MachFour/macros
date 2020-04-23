package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.Table;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.*;
import com.machfour.macros.storage.CsvExport;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.FileUtils;

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

    private <M extends MacrosEntity<M>> void exportTable(MacrosDatabase db, String outDir, Table<M> t)
            throws SQLException, IOException {
        out.println("Exporting " + t.name() + " table...");
        String outCsvPath = FileUtils.joinPath(outDir, t.name() + ".csv");
        try (Writer outCsv = new FileWriter(outCsvPath)) {
            CsvExport.exportTable(t, outCsv, db);
        }
    }

    @Override
    public int doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return 0;
        }

        String outputDir = args.size() >= 2 ? args.get(1) : Config.DEFAULT_CSV_OUTPUT_DIR;
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        try {
            exportTable(db, outputDir, Food.table());
            exportTable(db, outputDir, NutritionData.table());
            exportTable(db, outputDir, Serving.table());
            exportTable(db, outputDir, Ingredient.table());
            exportTable(db, outputDir, Meal.table());
            exportTable(db, outputDir, FoodPortion.table());
        } catch (SQLException | IOException e) {
            out.println();
            err.printf("Exception occurred (%s). Message: %s\n", e.getClass(), e.getMessage());
            return 1;
        }

        out.println();
        out.println("Export completed successfully");
        return 0;
    }
}
