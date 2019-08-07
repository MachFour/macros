package com.machfour.macros.cli;

import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliUtils.deNull;

class AllFoods extends CommandImpl {
    private static final String NAME = "allfoods";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        OUT.printf("Usage: %s %s\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(java.util.List<String> args) {
        if (args.contains("--help")) {
            printHelp(OUT);
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        listFoods(OUT, db);
    }

    public static void listFoods(PrintStream out, MacrosDatabase db) {
        List<Food> allFoods;
        try {
            allFoods = db.getAllFoods();
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
