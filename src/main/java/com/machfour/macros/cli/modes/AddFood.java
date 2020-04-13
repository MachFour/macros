package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.ArgParsing;
import com.machfour.macros.cli.utils.FoodEditor;
import com.machfour.macros.core.MacrosBuilder;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static com.machfour.macros.linux.Config.PROGNAME;

public class AddFood extends CommandImpl {
    private static final String NAME = "addfood";
    private static final String USAGE = String.format("Usage: %s %s <index name>\n", PROGNAME, NAME);

    /*
     * Ensures the given index name is not already in the database; returns true if it is not present.
     */
    private static boolean checkIndexName(@NotNull MacrosDataSource ds, @NotNull String indexName) throws SQLException {
        return ds.getFoodIdsByIndexName(Collections.singletonList(indexName)).isEmpty();
    }
    public AddFood() {
        super(NAME, USAGE);
    }

    @Override
    public void doAction(List<String> args) {
        out.println("doAction() is deprecated, using doActionWithExitCode() instead");
        doActionWithExitCode(args);
    }

    @Override
    public int doActionWithExitCode(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return -1;
        }
        ArgParsing.Result indexNameArg = ArgParsing.findArgument(args, 1);
        if (indexNameArg.status() != ArgParsing.Status.ARG_FOUND) {
            out.print(usage());
            return -1;
        }

        String indexName = indexNameArg.argument();
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        try {
            if (!checkIndexName(db, indexName)) {
                out.println("Index name " + indexName + " already exists in the database, cannot continue.");
                return 1;
            }
        } catch (SQLException e) {
            out.println(e.getMessage());
            return 1;
        }

        MacrosBuilder<Food> foodBuilder = new MacrosBuilder<>(Food.table());
        MacrosBuilder<NutritionData> nDataBuilder = new MacrosBuilder<>(NutritionData.table());

        FoodEditor editor = null;
        boolean editorInitialised = false;
        try {
            try {
                editor = new FoodEditor(db, foodBuilder, nDataBuilder);
                editor.init();
                editorInitialised = true;
                editor.run();
            } catch (IOException e) {
                out.println("IO Error: " + e.getLocalizedMessage());
                return 1;
            } finally {
                if (editorInitialised) {
                    editor.deInit();
                }
            }
        } catch (IOException e) {
            out.println("IO Error: " + e.getLocalizedMessage());
            return 1;
        }

        return 0;
    }
}
