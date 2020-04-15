package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.CliUtils;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.storage.MacrosDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.machfour.macros.linux.Config.PROGNAME;

public class DeleteFood extends CommandImpl {
    private static final String NAME = "deletefood";
    private static final String USAGE = String.format("Usage: %s %s <index name 1> [<index name 2>] [...]", PROGNAME, NAME);

    public DeleteFood() {
        super(NAME, USAGE);
    }

    public void doActionNoExitCode(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        } else if (args.size() < 2) {
            out.println(usage());
            return;
        }

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        out.println("Retrieving foods...");
        out.println();

        List<Food> foodsToDelete = Collections.emptyList();
        try {
            foodsToDelete = new ArrayList<>(db.getFoodsByIndexName(args.subList(1, args.size())).values());
        } catch (SQLException e) {
            out.println("SQL Exception while retrieving foods: " + e);
            return;
        }

        switch (foodsToDelete.size()) {
            case 0:
                out.println("No matching foods found!");
                return;
            case 1:
                out.println("===== Food to delete =====");
                out.println();
                break;
            default:
                out.println("===== Foods to delete =====");
                out.println();
                break;
        }

        for (Food f : foodsToDelete) {
            ShowFood.printFoodSummary(f, out);
        }

        out.println();
        String plural = foodsToDelete.size() != 1 ? "s" : "";
        out.print("Confirm delete " + foodsToDelete.size() + " food" + plural + "? [y/N] ");
        char response = CliUtils.getChar(in, out);
        out.println();
        if (response == 'y' || response == 'Y') {
            out.println("Deleting foods...");
            out.println();
            try {
                db.openConnection();
                db.beginTransaction();
                for (Food f : foodsToDelete) {
                    // XXX will ON DELETE CASCADE just do what we want here?
                    db.deleteObject(f);
                    out.println("Deleted " + f.getIndexName());
                }
                db.endTransaction();
            } catch (SQLException e) {
                out.println("SQL Exception occurred while deleting foods: " + e);
                out.println("No foods deleted");
            } finally {
                try {
                    db.closeConnection();
                } catch (SQLException e) {
                    out.println("Warning: SQL exception occurred when closing the DB");
                }
            }
        } else {
            out.println("No action performed");
        }

    }



}
