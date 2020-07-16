package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.Table;
import com.machfour.macros.core.datatype.TypeCastException;
import com.machfour.macros.objects.*;
import com.machfour.macros.storage.CsvRestore;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.FileUtils;

import java.io.*;
import java.sql.SQLException;
import java.util.List;



public class Restore extends CommandImpl {
    private static final String NAME = "restore";
    private static final String USAGE = String.format("%s %s [backup dir]", getProgramName(), NAME);

    public Restore() {
        super(NAME, USAGE);
    }

    @Override
    public void printHelp() {
        out.println("Restores the database using CSV data saved using the 'export' command.");
        out.println("Warning: this will overwrite all data in the database!");
    }

    private <M extends MacrosEntity<M>> void restoreTable(MacrosDataSource db, String exportDir, Table<M> t)
            throws SQLException, IOException, TypeCastException {
        out.println("Restoring " + t.getName() + " table...");
        String csvPath = FileUtils.joinPath(exportDir, t.getName() + ".csv");
        try (Reader csvData = new FileReader(csvPath)) {
            CsvRestore.restoreTable(t, csvData, db, out);
        }
    }

    @Override
    public int doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return 0;
        }

        // default output dir
        String csvDir = config.getDefaultCsvOutputDir();

        if (args.size() >= 2) {
            csvDir = args.get(1);
        }

        MacrosDataSource ds =  config.getDataSourceInstance();

        try {
            restoreTable(ds, csvDir, Food.table());
            restoreTable(ds, csvDir, NutritionData.table());
            restoreTable(ds, csvDir, Serving.table());
            restoreTable(ds, csvDir, Ingredient.table());
            restoreTable(ds, csvDir, Meal.table());
            restoreTable(ds, csvDir, FoodPortion.table());
        } catch (SQLException | IOException | TypeCastException e) {
            out.println();
            err.printf("Exception occurred (%s). Message: %s\n", e.getClass(), e.getMessage());
            return 1;
        }

        out.println();
        out.println("Database successfully restored from CSV data in " + csvDir);
        return 0;
    }
}
