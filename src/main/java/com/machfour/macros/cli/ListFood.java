package com.machfour.macros.cli;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
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
            return;
        }

        OUT.printf("Food data for '%s':\n", indexName);
        ColumnData<Food> data = foodToList.getAllData();
        for (Column<Food, ?> col : data.getColumns()) {
            if (data.hasData(col)) {
                OUT.printf("%s: %s\n", col.sqlName(), data.getAsNotNullString(col));
            }
        }

        NutritionData nd = foodToList.getNutritionData();
        OUT.printf("Nutrition data for '%s' (per 100%s):\n", indexName, nd.getQtyUnit().getAbbr());
        OUT.printf("Data source: %s\n", nd.getData(Schema.NutritionDataTable.DATA_SOURCE));
        CliUtils.printNutritionData(nd, true, OUT);

        List<Serving> servings = foodToList.getServings();
        if (servings.isEmpty()) {
            return;
        }
        OUT.println();
        OUT.println("Servings:");
        for (Serving s: servings) {
            OUT.printf(" - %s: %.1f%s\n", s.getName(), s.getQuantity(), s.getQtyUnit().getAbbr());

        }
    }
}
