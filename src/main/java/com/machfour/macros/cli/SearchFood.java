package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.StringJoiner;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;

import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliMain.OUT;


class SearchFood extends CommandImpl {
    private static final String NAME = "search";
    private static final String USAGE = String.format("Usage: %s %s <keyword>", PROGNAME, NAME);

    SearchFood() {
        super(NAME, USAGE);
    }

    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp();
            if (args.size() == 1) {
                out.println("Please enter a search keyword for the food database");
            }
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        String keyword = args.get(1);

        Map<Long, Food> resultFoods = Collections.emptyMap();
        try {
            Set<Long> resultIds = db.foodSearch(keyword);
            if (!resultIds.isEmpty()) {
                resultFoods = db.getFoodsById(resultIds);
            }
        } catch (SQLException e) {
            out.print("SQL exception occurred: ");
            out.println(e.getMessage());
            return;
        }
        if (resultFoods.isEmpty()) {
            out.printf("No matches for keyword '%s'\n", keyword);
        } else {
            out.println("Search results:");
            out.println();
            printFoodList(resultFoods.values(), OUT);
        }
    }

    public static void printFoodList(Collection<Food> foods, PrintStream out) {
        // work out how wide the column should be
        int maxNameLength = 0;
        for (Food f : foods) {
            int nameLength = f.getMediumName().length();
            if (nameLength > maxNameLength) {
                maxNameLength = nameLength;
            }
        }
        String formatStr = "%-" + maxNameLength + "s        %s\n";
        // horizontal line - extra spaces are for whitespace + index name length
        String hline = StringJoiner.of("=").copies(maxNameLength+8+14).join();
        out.printf(formatStr, "Food name", "index name");
        out.println(hline);
        for (Food f : foods) {
            out.printf(formatStr, f.getMediumName(), f.getIndexName());
        }
    }
}
