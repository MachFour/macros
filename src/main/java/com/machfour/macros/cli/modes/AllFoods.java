package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.objects.Food;
import com.machfour.macros.queries.FoodQueries;
import com.machfour.macros.storage.MacrosDataSource;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;



public class AllFoods extends CommandImpl {
    private static final String NAME = "allfoods";
    private static final String USAGE = String.format("Usage: %s %s\n", config.getProgramName(), NAME);

    public AllFoods() {
        super(NAME, USAGE);
    }

    @Override
    public void doActionNoExitCode(java.util.List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        }
        MacrosDataSource db = config.getDataSourceInstance();
        listFoods(out, db);
    }

    public static void listFoods(PrintStream out, MacrosDataSource ds) {
        List<Food> allFoods;
        try {
            allFoods = FoodQueries.getAllFoods(ds);
        } catch (SQLException e) {
            out.print("SQL exception occurred: ");
            out.println(e.getErrorCode());
            return;
        }

        if (allFoods.isEmpty()) {
            out.println("No foods currently recorded in the database.");
        } else {
            out.println("============");
            out.println(" All Foods  ");
            out.println("============");
            SearchFood.printFoodList(allFoods, out);
        }
        //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getDefault());
    }
}
