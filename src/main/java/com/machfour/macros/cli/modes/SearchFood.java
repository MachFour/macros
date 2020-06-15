package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.objects.Food;
import com.machfour.macros.queries.FoodQueries;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.StringJoiner;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;




public class SearchFood extends CommandImpl {
    private static final String NAME = "search";
    private static final String USAGE = String.format("Usage: %s %s <keyword>", config.getProgramName(), NAME);

    public SearchFood() {
        super(NAME, USAGE);
    }

    @Override
    public void doActionNoExitCode(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp();
            if (args.size() == 1) {
                out.println("Please enter a search keyword for the food database");
            }
            return;
        }
        MacrosDataSource ds =  config.getDataSourceInstance();
        String keyword = args.get(1);

        Map<Long, Food> resultFoods = Collections.emptyMap();
        try {
            Set<Long> resultIds = FoodQueries.foodSearch(ds, keyword);
            if (!resultIds.isEmpty()) {
                resultFoods = FoodQueries.getFoodsById(ds, resultIds);
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
            printFoodList(resultFoods.values(), out);
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
