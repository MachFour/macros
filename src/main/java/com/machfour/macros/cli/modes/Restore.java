package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.core.Table;
import com.machfour.macros.core.datatype.TypeCastException;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.*;
import com.machfour.macros.storage.CsvExport;
import com.machfour.macros.storage.CsvRestore;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.FileUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.linux.Config.PROGNAME;

public class Restore extends CommandImpl {
    private static final String NAME = "restore";
    private static final String USAGE = String.format("%s %s [backup dir]", PROGNAME, NAME);

    public Restore() {
        super(NAME, USAGE);
    }

    @Override
    public void printHelp() {
        out.println("Restores the database using CSV data saved using the 'export' command.");
        out.println("Warning: this will overwrite all data in the database!");
    }

    private <M extends MacrosPersistable<M>> void restoreTable(MacrosDatabase db, String exportDir, Table<M> t)
            throws SQLException, IOException, TypeCastException {
        out.println("Restoring " + t.name() + " table...");
        String csvPath = FileUtils.joinPath(exportDir, t.name() + ".csv");
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
        String csvDir = Config.DEFAULT_CSV_OUTPUT_DIR;

        if (args.size() >= 2) {
            csvDir = args.get(1);
        }

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        try {
            restoreTable(db, csvDir, Food.table());
            restoreTable(db, csvDir, NutritionData.table());
            restoreTable(db, csvDir, Serving.table());
            restoreTable(db, csvDir, Ingredient.table());
            restoreTable(db, csvDir, Meal.table());
            restoreTable(db, csvDir, FoodPortion.table());
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
