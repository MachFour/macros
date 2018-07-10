package com.machfour.macros.cli;

import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliMain.IN;

class Edit extends ModeImpl {
    private final String NAME = "edit";
    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp(OUT);
            return;
        }

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        MealSpec mealSpec = CliUtils.makeMealSpec(args);
        CliUtils.processMealSpec(mealSpec, db, false);
        if (mealSpec.error != null) {
            OUT.println(mealSpec.error);
            return;
        }

        Meal toEdit = mealSpec.createdObject;
        startEditor(db, toEdit.getId());
    }

    private static void startEditor(MacrosDatabase db, long mealId) {
        Meal toEdit;
        try {
            toEdit = db.getMealById(mealId);
            if (toEdit == null) {
                return;
            }
        } catch (SQLException e) {
            // exit
            return;
        }
        assert (toEdit.getObjectSource() == ObjectSource.DATABASE) : "Not editing an object from the database";
        OUT.printf("Editing meal: %s on %s\n", toEdit.getName(), CliUtils.prettyDay(toEdit.getDay()));
        OUT.println();
        OUT.println("Please choose from one of the following options");
        printInteractiveHelp(OUT);
        OUT.println();

        while (true) {
            // TODO reload meal
            OUT.print("Type the letter of the action to perform and press enter: ");
            char action = getChar();
            OUT.println();
            switch (action) {
                case 'a':
                    addPortion(toEdit, db);
                    break;
                case 'd':
                    deleteFoodPortion(toEdit, db);
                    break;
                case 'D':
                    deleteMeal(toEdit, db);
                    // TODO exit if deleted
                    break;
                case 'm':
                    OUT.println("Meal");
                    OUT.println("Not implemented yet, sorry!");
                    break;
                case 'n':
                    renameMeal();
                    break;
                case 's':
                    showFoodPortions(toEdit);
                    break;
                case '?':
                    printInteractiveHelp(OUT);
                    break;
                case 'x':
                case '\0':
                    return;
                default:
                    OUT.printf("Unrecognised action: '%c'\n", action);
                    OUT.println();

            }
        }
    }

    private static char getChar() {
        String input = getStringInput();
        if (input == null || input.isEmpty()) {
            return '\0';
        } else {
            return input.charAt(0);
        }
    }

    @Override
    public String name() {
        return NAME;
    }

    private static void printInteractiveHelp(PrintStream out) {
        out.println("Actions: ");
        out.println("a - add a new food portion");
        out.println("d - delete a food portion");
        out.println("D - delete the entire meal");
        out.println("m - move a food portion to another meal");
        out.println("n - change the name of the meal");
        out.println("s - show current food portions");
        out.println("? - print this help");
        out.println("x - exit this editor");
    }

    private static void addPortion(Meal toEdit, MacrosDatabase db) {
        OUT.println("Please enter the portion information (see help for how to specify a food portion)");
        // copy from portion
        String inputString = getStringInput();
        if (inputString != null && !inputString.isEmpty()) {
            FoodPortionSpec spec = FileParser.makefoodPortionSpecFromLine(inputString);
            Portion.process(toEdit, spec, db);
        }
    }

    private static void showFoodPortions(Meal toEdit) {
        OUT.println("Food portions:");
        List<FoodPortion> foodPortions = toEdit.getFoodPortions();
        for (int i = 0; i < foodPortions.size(); ++i) {
            FoodPortion fp = foodPortions.get(i);
            OUT.printf("%d: %s\n", i, fp.prettyFormat(true));
        }
        OUT.println();
    }
    private static void deleteMeal(Meal toDelete, MacrosDatabase db) {
        OUT.print("Are you sure? [y/N] ");
        if (getChar() == 'y' | getChar() == 'Y') {
            try {
                db.deleteObject(toDelete);
            } catch (SQLException e) {
                OUT.println("Error deleting meal: " + e.getMessage());
            }
        }
    }
    private static void deleteFoodPortion(Meal toEdit, MacrosDatabase db) {
        showFoodPortions(toEdit);
        OUT.print("Enter the number of the food portion to delete and press enter: ");
        String input = getStringInput();
        if (input == null) {
            return;
        }
        try {
            int portionNumber = Integer.parseInt(input);
            List<FoodPortion> portions = toEdit.getFoodPortions();
            if (portionNumber >= 0 && portionNumber < portions.size()) {
                // delete it
                db.deleteObject(portions.get(portionNumber));
            } else {
                OUT.println("No food portion with number: " + portionNumber);
            }
        } catch (NumberFormatException e2) {
            OUT.printf("Could not read number: '%s'\n", input);
            return;
        } catch (SQLException e3) {
            OUT.println("Error deleting the food portion: " + e3.getMessage());
            return;
        }
        OUT.println("Deleted the food portion");
        OUT.println();
    }

    private static void renameMeal() {
        OUT.print("Type a new name and press enter: ");
        String newName = getStringInput();
        if (newName == null) {
            return;
        }
        OUT.println("The new name is: " + newName);
    }

    private static @Nullable String getStringInput() {
        try {
            String input = IN.readLine();
            if (input != null) {
                return input.trim();
            } else {
                return null;
            }
        } catch (IOException e) {
            OUT.println("Error reading input: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void printHelp(PrintStream out) {
        out.printf("Usage: %s %s [-m meal] [-d day]\n", PROGNAME, NAME);
        out.println();
        out.println("Interactive meal editor");
        out.println();
        printInteractiveHelp(out);
        out.println();
        out.println("Food portions can be entered in one of the following forms:");
        out.println("<> denotes mandatory arguments, [] denotes optional arguments, and other characters are literal");
        out.println("1. <food index name>, <quantity>[quantity unit]");
        out.println("2. <food index name>, [<serving name>], <serving count> (omit serving name for default serving)");
        out.println("3. <food index name> (this means 1 of the default serving)");
    }

}
