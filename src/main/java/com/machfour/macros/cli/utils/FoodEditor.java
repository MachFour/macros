package com.machfour.macros.cli.utils;

import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalFactory;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.MacrosBuilder;
import com.machfour.macros.core.Table;
import com.machfour.macros.names.*;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.UnicodeUtils;
import org.jetbrains.annotations.NotNull;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.input.KeyStroke;

import java.io.IOException;
import java.io.PrintStream;

public class FoodEditor {

    private enum Action {
          EDIT("Edit")
        , VALIDATE_ALL("Validate All")
        , SAVE("Save")
        , EXIT("Exit");

        final String name;

        Action(String name) {
            this.name = name;
        }

        @NotNull @Override
        public String toString() {
            return name;
        }
    }

    private static final Table<Food> FOOD_TABLE = Food.table();
    private static final Table<NutritionData> ND_TABLE = NutritionData.table();


    private final MacrosDataSource ds;
    private final MacrosBuilder<Food> foodBuilder;
    private final MacrosBuilder<NutritionData> nDataBuilder;
    private final ColumnStrings colStrings;
    private final Terminal terminal;

    private final PrintStream out;

    private boolean isAllValidated;
    private Action currentAction;
    // which column is currently being edited
    private int currentColumnIndex;


    private final int layoutWidth = 79;
    // used for layout
    private int charsLeftOnLine;

    private static Terminal defaultTerminal() throws IOException {
        TerminalFactory tf = new DefaultTerminalFactory();
        return tf.createTerminal();
    }

    public FoodEditor(@NotNull MacrosDataSource ds, @NotNull MacrosBuilder<Food> foodBuilder,
               @NotNull MacrosBuilder<NutritionData> nDataBuilder) throws IOException {
        this(ds, foodBuilder, nDataBuilder,
                DefaultColumnStrings.getInstance(), defaultTerminal());
    }

    // TODO servings?
    public FoodEditor(@NotNull MacrosDataSource ds, @NotNull MacrosBuilder<Food> foodBuilder,
                      @NotNull MacrosBuilder<NutritionData> nDataBuilder,
                      @NotNull ColumnStrings colStrings,
                      @NotNull Terminal terminal) {
        this.ds = ds;
        this.foodBuilder = foodBuilder;
        this.nDataBuilder = nDataBuilder;
        this.colStrings = colStrings;

        this.terminal = terminal;

        this.out = System.out;
        this.charsLeftOnLine = 0;
    }

    public void run() throws IOException {
        while (true) {
            printLayout();
            KeyStroke command = null;
            while (command == null || command.getKeyType() == KeyType.MouseEvent) {
                // do nothing
                command = terminal.pollInput();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (command.getKeyType() == KeyType.Character && command.getCharacter() == 'q') {
                break;
            }
        }

    }

    public void init() throws IOException {
        terminal.enterPrivateMode();
        terminal.clearScreen();
    }

    public void deInit() throws IOException {
        terminal.exitPrivateMode();
        terminal.close();
    }

    private void newScreen() throws IOException {
        terminal.clearScreen();
        terminal.setCursorPosition(0, 0);
    }



    // print string, left aligned in given width, or just print if width <= 0
    // Object o's toString() method must not contain any new lines or tab characters
    private String padString(@NotNull Object o, int width, boolean rightAlign) {
        String s = o.toString();
        if (s.contains("\n") || s.contains("\t")) {
            throw new IllegalArgumentException("Object's toString() cannot contain newline or tab characters");
        }
        if (width <= 0) {
            return s;
            //charsLeftOnLine -= UnicodeUtils.displayLength(s);
        } else {
            // account for wide characters - these take twice as much space
            int wideChars = UnicodeUtils.countDoubleWidthChars(s);
            int displayLength = s.length() + wideChars;
            if (displayLength > width) {
                // string has to be truncated: subtract (displayLength - width) chars
                int maxIndex = s.length() - (displayLength - width); // == width - wideChars
                s = s.substring(0, maxIndex);
                // recount the wide characters and display length
                wideChars = UnicodeUtils.countDoubleWidthChars(s);
                displayLength = s.length() + wideChars;
            }
            assert displayLength <= width;
            String align = rightAlign ? "" : "-";
            return String.format("%" + align + (width - wideChars) + "s", s);
            //charsLeftOnLine -= width;
        }
    }

    private void newline() throws IOException {
        //TerminalPosition currentPos = terminal.getCursorPosition();
        //terminal.setCursorPosition(currentPos.withRelativeRow(2).getRow(), 0);
        terminal.putCharacter('\n');
    }

    private void print(@NotNull Object o) throws IOException {
        print(o, 0, false, false);
    }
    private void println(@NotNull Object o) throws IOException {
        print(o, 0, false, true);
    }
    private void print(@NotNull Object o, int width, boolean rightAlign) throws IOException {
        print(o, width, rightAlign, false);
    }
    private void println(@NotNull Object o, int width, boolean rightAlign) throws IOException {
        print(o, width, rightAlign, true);
    }

    private void print(@NotNull Object o, int width, boolean rightAlign, boolean newline) throws IOException {
        String padded = padString(o, width, rightAlign);
        for (int i = 0; i < padded.length(); i++) {
            terminal.putCharacter(padded.charAt(i));
        }
        // TODO set cursor position to original one + width ?
        if (newline) {
            newline();
        }
    }


    private void printLayout() throws IOException {
        newScreen();
        println("Macros Food Editor");
        newline();
        // actions
        for (Action a : Action.values()) {
            String indicator = (a == currentAction) ? ">" : " ";
            print(String.format(" %s %s", indicator, a.name), 16, false);
        }
        newline();
        newline();
        println("Food Details:");
        newline();

        for (Column<Food, ?> col: FOOD_TABLE.columns()) {
            if (!col.isUserEditable()) {
                continue;
            }
            print(colStrings.getName(col), 28, true);
            newline();
        }

        newline();

        println("Nutrition Details:");
        for (Column<NutritionData, Double> col: NutritionData.NUTRIENT_COLUMNS) {
            print(colStrings.getNutrientName(col), 28, true);
            newline();
        }




        /* Rough idea:
         |--------------------------------------------- ... (terminal edge)
         | Macros Food Editor: <index name>
         |
         | > Edit    Validate All    Save     Exit      ... (current action is Edit)
         |
         |  Food details
         |      name               ____________
         |      brand         >>>  ____________  <<<    ... (editing brand)
         |      notes              ____________
         |      ...
         |
         |  Nutrition details
         |      quantity               100
         |      quantity unit           g
         |      kilojoules             400
         */



    }

    private void validateAll() {

    }

    private void saveAndQuit() {

    }

    private void stepField(boolean forward) {

    }


}
