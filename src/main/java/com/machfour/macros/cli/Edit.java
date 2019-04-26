package com.machfour.macros.cli;

import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.FoodPortionSpec;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliMain.IN;

class Edit extends CommandImpl {
    private final String NAME = "edit";
    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp(OUT);
            return;
        }

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        ArgParsing.Result mealNameArg = ArgParsing.findArgument(args, 1);
        ArgParsing.Result dayArg = ArgParsing.findArgument(args, 2);
        MealSpec mealSpec = MealSpec.makeMealSpec(mealNameArg, dayArg);

        mealSpec.process(db, true);
        if (mealSpec.error() != null) {
            OUT.println(mealSpec.error());
            return;
        }
        if (mealSpec.created()) {
            String createMsg = String.format("Created meal '%s' on %s", mealSpec.name(), mealSpec.day());
            OUT.println(createMsg);
        }
        Meal toEdit = mealSpec.processedObject();
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
            OUT.println(e);
            return;
        }
        assert (toEdit.getObjectSource() == ObjectSource.DATABASE) : "Not editing an object from the database";

        while (true) {
            // TODO reload meal
            OUT.println();
            OUT.printf("Editing meal: %s on %s\n", toEdit.getName(), CliUtils.prettyDay(toEdit.getDay()));
            OUT.println();
            OUT.print("Action (? for help): ");
            char action = getChar();
            OUT.println();
            switch (action) {
                case 'a':
                    addPortion(toEdit, db);
                    break;
                case 'd':
                    deleteFoodPortion(toEdit, db);
                    OUT.println("WARNING: meal is not reloaded");
                    break;
                case 'D':
                    deleteMeal(toEdit, db);
                    // TODO exit if deleted
                    break;
                case 'e':
                    editFoodPortion(toEdit, db);
                    OUT.println("WARNING: meal is not reloaded");
                    break;
                case 'm':
                    OUT.println("Meal");
                    OUT.println("Not implemented yet, sorry!");
                    break;
                case 'n':
                    renameMeal();
                    OUT.println("WARNING: meal is not reloaded");
                    break;
                case 's':
                    showFoodPortions(toEdit);
                    break;
                case '?':
                    OUT.println();
                    OUT.println("Please choose from one of the following options");
                    printInteractiveHelp(OUT);
                    break;
                case 'x':
                case 'q':
                case '\0':
                    return;
                default:
                    OUT.printf("Unrecognised action: '%c'\n", action);
                    OUT.println();

            }
        }
    }

    private static char getChar() {
        String input = CliUtils.getStringInput(IN, OUT);
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
        out.println("a   - add a new food portion");
        out.println("d   - delete a food portion");
        out.println("D   - delete the entire meal");
        out.println("e   - edit a food portion");
        out.println("m   - move a food portion to another meal");
        out.println("n   - change the name of the meal");
        out.println("s   - show current food portions");
        out.println("?   - print this help");
        out.println("x/q - exit this editor");
    }

    private static void addPortion(Meal toEdit, MacrosDatabase db) {
        OUT.println("Please enter the portion information (see help for how to specify a food portion)");
        // copy from portion
        String inputString = CliUtils.getStringInput(IN, OUT);
        if (inputString != null && !inputString.isEmpty()) {
            FoodPortionSpec spec = FileParser.makefoodPortionSpecFromLine(inputString);
            Portion.process(toEdit, Collections.singletonList(spec), db);
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
        OUT.print("Delete meal");
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
        OUT.println("Delete food portion");
        showFoodPortions(toEdit);
        OUT.print("Enter the number of the food portion to delete and press enter: ");
        List<FoodPortion> portions = toEdit.getFoodPortions();
        Integer n = CliUtils.getIntegerInput(IN, OUT, 0, portions.size()-1);
        if (n == null) {
            OUT.println("Invalid number");
            return;
        }
        try {
            db.deleteObject(portions.get(n));
        } catch (SQLException e3) {
            OUT.println("Error deleting the food portion: " + e3.getMessage());
            return;
        }
        OUT.println("Deleted the food portion");
        OUT.println();
    }
    private static void editFoodPortion(Meal m, MacrosDatabase db) {
        OUT.println("Edit food portion");
        showFoodPortions(m);
        OUT.print("Enter the number of the food portion to edit and press enter: ");
        List<FoodPortion> portions = m.getFoodPortions();
        Integer n = CliUtils.getIntegerInput(IN, OUT, 0, portions.size()-1);
        if (n == null) {
            OUT.println("Invalid number");
            return;
        }
        OUT.print("Enter a new quantity (in the same unit) and press enter: ");
        Double newQty = CliUtils.getDoubleInput(IN, OUT);
        if (newQty == null) {
            OUT.println("Invalid quantity");
            return;
        }

        try {
            ColumnData<FoodPortion> newData = portions.get(n).getAllData(false);
            newData.put(Schema.FoodPortionTable.QUANTITY, newQty);
            db.saveObject(FoodPortion.factory().construct(newData, ObjectSource.DB_EDIT));
        } catch (SQLException e3) {
            OUT.println("Error modifying the food portion: " + e3.getMessage());
            return;
        }
        OUT.println("Successfully saved the food portion");
        OUT.println();
    }

    private static void renameMeal() {
        OUT.println("Rename meal");
        OUT.print("Type a new name and press enter: ");
        String newName = CliUtils.getStringInput(IN, OUT);
        if (newName == null) {
            return;
        }
        OUT.println("The new name is: " + newName);
    }

    @Override
    public void printHelp(PrintStream out) {
        out.printf("Usage: %s %s [meal [day]]\n", PROGNAME, NAME);
        out.println();
        out.println("Interactive meal editor");
        out.println();
        printInteractiveHelp(out);
        out.println();
        out.println("Food portions can be entered in one of the following forms:");
        out.println("1. <food index name>, <quantity>[quantity unit]");
        out.println("2. <food index name>, [<serving name>], <serving count> (omit serving name for default serving)");
        out.println("3. <food index name> (this means 1 of the default serving)");
        out.println("(<> denotes a mandatory argument and [] denotes an optional argument)");
    }

}
