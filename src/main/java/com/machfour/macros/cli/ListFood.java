package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliMain.OUT;

class ListFood extends ModeImpl {
    private static final String NAME = "list";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        OUT.printf("Usage: %s %s <index_name>\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp(OUT);
            if (args.size() == 1) {
                OUT.println("Please enter the index name of the food to list");
            }
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        String indexName = args.get(1);
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
