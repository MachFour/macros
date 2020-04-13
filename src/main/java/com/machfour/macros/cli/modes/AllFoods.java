package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.linux.Config.PROGNAME;

public class AllFoods extends CommandImpl {
    private static final String NAME = "allfoods";
    private static final String USAGE = String.format("Usage: %s %s\n", PROGNAME, NAME);

    public AllFoods() {
        super(NAME, USAGE);
    }

    @Override
    public void doAction(java.util.List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        listFoods(out, db);
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
