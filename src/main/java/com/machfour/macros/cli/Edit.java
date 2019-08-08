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
import com.machfour.macros.util.PrintFormatting;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliMain.IN;

class Edit extends CommandImpl {
    private static final String NAME = "edit";
    private static final String USAGE = String.format("Usage: %s %s [meal [day]]\n", PROGNAME, NAME);

    Edit() {
        super(NAME, USAGE);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        }

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        ArgParsing.Result mealNameArg = ArgParsing.findArgument(args, 1);
        ArgParsing.Result dayArg = ArgParsing.findArgument(args, 2);
        MealSpec mealSpec = MealSpec.makeMealSpec(mealNameArg, dayArg);

        mealSpec.process(db, true);
        if (mealSpec.error() != null) {
            out.println(mealSpec.error());
            return;
        }
        if (mealSpec.created()) {
            String createMsg = String.format("Created meal '%s' on %s", mealSpec.name(), mealSpec.day());
            out.println(createMsg);
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
            out.println(e);
            return;
        }
        assert (toEdit.getObjectSource() == ObjectSource.DATABASE) : "Not editing an object from the database";

        while (true) {
            // TODO reload meal
            out.println();
            out.printf("Editing meal: %s on %s\n", toEdit.getName(), PrintFormatting.prettyDay(toEdit.getDay()));
            out.println();
            out.print("Action (? for help): ");
            char action = getChar();
            out.println();
            switch (action) {
                case 'a':
                    addPortion(toEdit, db);
                    break;
                case 'd':
                    deleteFoodPortion(toEdit, db);
                    out.println("WARNING: meal is not reloaded");
                    break;
                case 'D':
                    deleteMeal(toEdit, db);
                    // TODO exit if deleted
                    break;
                case 'e':
                    editFoodPortion(toEdit, db);
                    out.println("WARNING: meal is not reloaded");
                    break;
                case 'm':
                    out.println("Meal");
                    out.println("Not implemented yet, sorry!");
                    break;
                case 'n':
                    renameMeal();
                    out.println("WARNING: meal is not reloaded");
                    break;
                case 's':
                    showFoodPortions(toEdit);
                    break;
                case '?':
                    out.println();
                    out.println("Please choose from one of the following options");
                    out.println(interactiveHelpString());
                    break;
                case 'x':
                case 'q':
                case '\0':
                    return;
                default:
                    out.printf("Unrecognised action: '%c'\n", action);
                    out.println();

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

    private static String interactiveHelpString() {
        return "Actions:"
            + "\n" + "a   - add a new food portion"
            + "\n" + "d   - delete a food portion"
            + "\n" + "D   - delete the entire meal"
            + "\n" + "e   - edit a food portion"
            + "\n" + "m   - move a food portion to another meal"
            + "\n" + "n   - change the name of the meal"
            + "\n" + "s   - show current food portions"
            + "\n" + "?   - print this help"
            + "\n" + "x/q - exit this editor";
    }

    private static void addPortion(Meal toEdit, MacrosDatabase db) {
        out.println("Please enter the portion information (see help for how to specify a food portion)");
        // copy from portion
        String inputString = CliUtils.getStringInput(IN, OUT);
        if (inputString != null && !inputString.isEmpty()) {
            FoodPortionSpec spec = FileParser.makefoodPortionSpecFromLine(inputString);
            Portion.process(toEdit, Collections.singletonList(spec), db);
        }
    }

    private static void showFoodPortions(Meal toEdit) {
        out.println("Food portions:");
        List<FoodPortion> foodPortions = toEdit.getFoodPortions();
        for (int i = 0; i < foodPortions.size(); ++i) {
            FoodPortion fp = foodPortions.get(i);
            out.printf("%d: %s\n", i, fp.prettyFormat(true));
        }
        out.println();
    }
    private static void deleteMeal(Meal toDelete, MacrosDatabase db) {
        out.print("Delete meal");
        out.print("Are you sure? [y/N] ");
        if (getChar() == 'y' | getChar() == 'Y') {
            try {
                db.deleteObject(toDelete);
            } catch (SQLException e) {
                out.println("Error deleting meal: " + e.getMessage());
            }
        }
    }
    private static void deleteFoodPortion(Meal toEdit, MacrosDatabase db) {
        out.println("Delete food portion");
        showFoodPortions(toEdit);
        out.print("Enter the number of the food portion to delete and press enter: ");
        List<FoodPortion> portions = toEdit.getFoodPortions();
        Integer n = CliUtils.getIntegerInput(IN, OUT, 0, portions.size()-1);
        if (n == null) {
            out.println("Invalid number");
            return;
        }
        try {
            db.deleteObject(portions.get(n));
        } catch (SQLException e3) {
            out.println("Error deleting the food portion: " + e3.getMessage());
            return;
        }
        out.println("Deleted the food portion");
        out.println();
    }
    private static void editFoodPortion(Meal m, MacrosDatabase db) {
        out.println("Edit food portion");
        showFoodPortions(m);
        out.print("Enter the number of the food portion to edit and press enter: ");
        List<FoodPortion> portions = m.getFoodPortions();
        Integer n = CliUtils.getIntegerInput(IN, OUT, 0, portions.size()-1);
        if (n == null) {
            out.println("Invalid number");
            return;
        }
        out.print("Enter a new quantity (in the same unit) and press enter: ");
        Double newQty = CliUtils.getDoubleInput(IN, OUT);
        if (newQty == null) {
            out.println("Invalid quantity");
            return;
        }

        try {
            ColumnData<FoodPortion> newData = portions.get(n).getAllData(false);
            newData.put(Schema.FoodPortionTable.QUANTITY, newQty);
            db.saveObject(FoodPortion.factory().construct(newData, ObjectSource.DB_EDIT));
        } catch (SQLException e3) {
            out.println("Error modifying the food portion: " + e3.getMessage());
            return;
        }
        out.println("Successfully saved the food portion");
        out.println();
    }

    private static void renameMeal() {
        out.println("Rename meal");
        out.print("Type a new name and press enter: ");
        String newName = CliUtils.getStringInput(IN, OUT);
        if (newName == null) {
            return;
        }
        out.println("The new name is: " + newName);
    }

    public void printHelp() {
        out.println(USAGE);
        out.println("Interactive meal editor");
        out.println();
        out.println();
        out.println(interactiveHelpString());
        out.println();
        out.println("Food portions can be entered in one of the following forms:");
        out.println("1. <food index name>, <quantity>[quantity unit]");
        out.println("2. <food index name>, [<serving name>], <serving count> (omit serving name for default serving)");
        out.println("3. <food index name> (this means 1 of the default serving)");
        out.println("(<> denotes a mandatory argument and [] denotes an optional argument)");
    }
}
