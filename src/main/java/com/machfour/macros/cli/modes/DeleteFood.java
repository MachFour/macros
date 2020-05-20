package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.CliUtils;
import com.machfour.macros.objects.Food;
import com.machfour.macros.storage.MacrosDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class DeleteFood extends CommandImpl {
    private static final String NAME = "deletefood";
    private static final String USAGE = String.format("Usage: %s %s <index name 1> [<index name 2>] [...]", config.getProgramName(), NAME);

    public DeleteFood() {
        super(NAME, USAGE);
    }

    public int doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return -1;
        } else if (args.size() < 2) {
            out.println(usage());
            return -1;
        }

        MacrosDataSource ds = config.getDataSourceInstance();

        out.println("Retrieving foods...");
        out.println();

        List<Food> foodsToDelete;
        try {
            foodsToDelete = new ArrayList<>(ds.getFoodsByIndexName(args.subList(1, args.size())).values());
        } catch (SQLException e) {
            out.println("SQL Exception while retrieving foods: " + e);
            return 1;
        }

        switch (foodsToDelete.size()) {
            case 0:
                out.println("No matching foods found!");
                return 2;
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
                ds.openConnection();
                ds.beginTransaction();
                for (Food f : foodsToDelete) {
                    // XXX will ON DELETE CASCADE just do what we want here?
                    ds.deleteObject(f);
                    out.println("Deleted " + f.getIndexName());
                }
                ds.endTransaction();
            } catch (SQLException e) {
                err.println("SQL Exception occurred while deleting foods: " + e);
                err.println("No foods deleted");
                return 1;
            } finally {
                try {
                    ds.closeConnection();
                } catch (SQLException e) {
                    err.println("Warning: SQL exception occurred when closing the DB");
                }
            }
        } else {
            out.println("No action performed");
        }
        return 0;

    }



}
