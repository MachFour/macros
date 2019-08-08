package com.machfour.macros.cli;

import com.machfour.macros.ingredients.IngredientsParser;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.machfour.macros.cli.CliMain.PROGNAME;

public class AddRecipe extends CommandImpl {
    private static final String NAME = "addrecipe";
    private static final String USAGE = String.format("Usage: %s %s <recipes.json>", PROGNAME, NAME);

    public AddRecipe() {
        super(NAME, USAGE);
    }

    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        } else if (args.size() < 2) {
            out.println(usage());
            return;
        }

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        List<String> indexNames = new ArrayList<>();

        try (Reader jsonReader = new FileReader(args.get(2))) {
            out.println("Importing recipes...");
            indexNames.addAll(IngredientsParser.importRecipes(jsonReader, db));
        } catch (IOException e1) {
            out.println("IO exception occurred: " + e1.getLocalizedMessage());
        } catch (SQLException e2) {
            out.println("SQL exception occurred: " + e2.getMessage());
        }

        if (indexNames.isEmpty()) {
            out.println("No recipes imported! Check the recipes file");
        } else {
            out.println("The following composite foods were imported (listed by index name):");
            for (String s : indexNames) {
                out.println(s);
            }
        }
    }

}
