package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.StringJoiner;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliMain.OUT;


class SearchFood extends CommandImpl {
    private static final String NAME = "search";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        OUT.printf("Usage: %s %s <keyword>\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp(OUT);
            if (args.size() == 1) {
                OUT.println("Please enter a search keyword for the food database");
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
            OUT.print("SQL exception occurred: ");
            OUT.println(e.getMessage());
            return;
        }
        if (resultFoods.isEmpty()) {
            OUT.printf("No matches for keyword '%s'\n", keyword);
        } else {
            // work out how wide the column should be
            int maxNameLength = 0;
            for (Food f : resultFoods.values()) {
                int nameLength = f.getMediumName().length();
                if (nameLength > maxNameLength) {
                    maxNameLength = nameLength;
                }
            }
            String formatStr = "%-" + maxNameLength + "s        %s\n";
            // horizontal line - extra spaces are for whitespace + index name length
            String hline = StringJoiner.of("=").copies(maxNameLength+8+14).join();
            OUT.println("Search results:");
            OUT.println();
            OUT.printf(formatStr, "Food name", "index name");
            OUT.println(hline);
            for (Food f : resultFoods.values()) {
                OUT.printf(formatStr, f.getMediumName(), f.getIndexName());
            }
        }
    }
}
