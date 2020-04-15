package com.machfour.macros.cli.interactive;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalFactory;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.MacrosBuilder;
import com.machfour.macros.names.*;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.UnicodeUtils;
import org.jetbrains.annotations.NotNull;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.input.KeyStroke;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FoodEditor {

    private enum Action {
          EDIT("Edit")
        , VALIDATE_ALL("Validate All")
        , SAVE_EXIT("Save/Exit");

        final String name;

        Action(String name) {
            this.name = name;
        }

        @NotNull @Override
        public String toString() {
            return name;
        }
    }
    private static final int NUM_ACTIONS = Action.values().length;

    private static final Collection<Column<Food, ?>> FOOD_TABLE_COLUMNS = Food.table().columns();
    private static final Collection<Column<NutritionData, ?>> ND_TABLE_COLUMNS = NutritionData.table().columns();

    private final MacrosDataSource ds;
    private final MacrosBuilder<Food> foodBuilder;
    private final MacrosBuilder<NutritionData> nDataBuilder;
    private final ColumnStrings colStrings;
    private final Terminal terminal;

    private TextGraphics textGraphics;

    // have all the fields been validated by the MacrosBuilders
    private boolean isAllValidated;
    // currently highlighted (as a choice) or active Action
    private Action currentAction;
    // are we still selecting which action, or is the action active?
    private boolean choosingAction;
    // which column is currently being edited (used when EDIT is active)
    private int currentColumnIndex;

    // records whether keyboard input actually did anything, and thus the UI has changed
    private boolean uiNeedsReprint;
    private boolean finishedEditing;

    // where the printing is happening
    private int terminalRow;
    private int terminalCol;

    // maps column Indices to terminal rows
    private final List<Integer> terminalRowForColumnIndex;
    private final List<Column<Food, ?>> foodColumnsForDisplay;
    private final List<Column<NutritionData, ?>> ndColumnsForDisplay;

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

        int numFoodColumns = FOOD_TABLE_COLUMNS.size();
        int numNdColumns = ND_TABLE_COLUMNS.size();

        this.terminalRowForColumnIndex = new ArrayList<>(numFoodColumns + numNdColumns);
        this.foodColumnsForDisplay = new ArrayList<>(numFoodColumns);
        this.ndColumnsForDisplay = new ArrayList<>(numNdColumns);

        this.terminal = terminal;

    }


    private static boolean isRecognisedKeyType(KeyType kt) {
        switch (kt) {
            // things we care about
            case Character:
            case Backspace:
            case ArrowLeft:
            case ArrowRight:
            case ArrowUp:
            case ArrowDown:
            case Enter:
            case Escape:
                return true;
            // things we don't care about
            case MouseEvent:
            case Home:
            case Delete:
            case EOF:
            case ReverseTab:
            case Tab:
            case Insert:
            case CursorLocation:
            default:
                return false;
        }
    }

    private void stepAction(boolean forward) {
        int nextActionOrdinal = currentAction.ordinal();
        if (forward) {
            if (++nextActionOrdinal >= NUM_ACTIONS) {
                nextActionOrdinal -= NUM_ACTIONS;
            }
        } else {
            if (--nextActionOrdinal < 0) {
                nextActionOrdinal += NUM_ACTIONS;
            }
        }
        currentAction = Action.values()[nextActionOrdinal];
    }

    private boolean processEnter() throws IOException {
        if (isEditing()) {
            // Enter saves the edit and moves to the next field
            // save and validate
            //foodBuilder.()
            // move to next field
            stepField(true);
        } else if (choosingAction) {
            // select the action and start it
            choosingAction = false;
            startAction();
        }
        // always needs repaint
        return true;
    }

    // begin the currently selected action (currentAction)
    private void startAction() {
        switch (currentAction) {
            case EDIT:
                // TODO
                break;
            case SAVE_EXIT:
                // TODO
                save();
                quit();
                break;
            case VALIDATE_ALL:
                validateAll();
                // TODO
                break;
        }
    }

    private boolean isEditing() {
        return currentAction == Action.EDIT && !choosingAction;
    }

    private boolean processArrow(KeyType kt) throws IOException {
        boolean uiChanged = false;
        switch (kt) {
            // up and down cancel edits and move to next field
            case ArrowUp:
                if (isEditing()) {
                    stepField(false);
                    uiChanged = true;
                }
                break;
            case ArrowDown:
                if (isEditing()) {
                    stepField(true);
                    uiChanged = true;
                }
                break;
            case ArrowLeft:
                if (choosingAction) {
                    stepAction(false);
                    uiChanged = true;
                }
                break;
            case ArrowRight:
                if (choosingAction) {
                    stepAction(true);
                    uiChanged = true;
                }
                break;
            default:
                throw new IllegalArgumentException("KeyType is not an arrow key");
        }
        return uiChanged;
    }

    // stop editing if editing, else quit
    private boolean processEscape() {
        if (isEditing()) {
            choosingAction = true;
            return true;
        } else {
            quit();
            return true;
        }
    }

    private boolean processCharacter() {
        if (isEditing()) {
            return true;
        } else {
            return false;
        }
    }
    private boolean processBackspace() {
        if (isEditing()) {
            return true;
        } else {
            return false;
        }
    }
    // Return true if the UI needs update after the command has processed
    // (i.e. because something changed)
    private boolean processCommand(@NotNull KeyStroke command) throws IOException {
        // keyType is one of the 'recognised' keytypes now
        KeyType type = command.getKeyType();
        switch (command.getKeyType()) {
            case Character:
                return processCharacter();
            case Backspace:
                return processBackspace();
            case ArrowLeft:
            case ArrowRight:
            case ArrowUp:
            case ArrowDown:
                return processArrow(type);
            case Enter:
                return processEnter();
            case Escape:
                return processEscape();
            default:
                throw new IllegalArgumentException("Unrecognised keytype: " + command.getKeyType());
        }
    }

    public void run() throws IOException {
        outer:
        while (!finishedEditing) {
                // UI loop
            if (uiNeedsReprint) {
                printLayout();
                uiNeedsReprint = false;
            }
            KeyStroke command = null;
            // wait for keystroke
            while (command == null || !isRecognisedKeyType(command.getKeyType())) {
                command = terminal.pollInput();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // gtfo
                    break outer;
                }
            }
            uiNeedsReprint = processCommand(command);
        }
    }

    // figure out which columns should be displayed.
    // Defaults to isUserEditable()
    private void initDisplayColumns() {
        for (Column<Food, ?> col: FOOD_TABLE_COLUMNS) {
            if (col.isUserEditable()) {
                foodColumnsForDisplay.add(col);
            }
        }
        // TODO display minimal set of nutrient columns first
        for (Column<NutritionData, ?> col: ND_TABLE_COLUMNS) {
            if (col.isUserEditable()) {
                ndColumnsForDisplay.add(col);
            }
        }

        // also fill the terminalRowForColumnIndex so we can set() it later
        for (int i = 0; i < numColumnsForDisplay(); i++) {
            terminalRowForColumnIndex.add(0);
        }
    }

    public void init() throws IOException {
        terminal.enterPrivateMode();
        terminal.clearScreen();
        textGraphics = terminal.newTextGraphics();

        this.terminalRow = 0;
        this.terminalCol = 0;
        this.charsLeftOnLine = 0;
        this.currentAction = Action.EDIT;
        this.choosingAction = true;
        this.isAllValidated = false;
        this.finishedEditing = false;
        this.uiNeedsReprint = true;

        initDisplayColumns();
    }

    public void deInit() throws IOException {
        terminal.exitPrivateMode();
        terminal.close();
    }

    private void newScreen() throws IOException {
        terminal.clearScreen();
        //terminal.setCursorPosition(0, 0);
        terminalRow = 0;
        terminalCol = 0;
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
        }
    }

    private void newline() throws IOException {
        //TerminalPosition currentPos = terminal.getCursorPosition();
        //terminal.setCursorPosition(currentPos.withRelativeRow(2).getRow(), 0);
        //terminal.putCharacter('\n');
        terminalRow += 1;
        terminalCol = 0;
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
        charsLeftOnLine -= width;
        textGraphics.putString(terminalCol, terminalRow, padded);
        terminalCol += padded.length();

        if (newline) {
            newline();
        }
    }

    private static final int columnNameWidth = 28;
    private static final int actionPaddingWidth = 20;

    private Column<?, ?> columnForColumnIndex(int index) {
        int numDisplayedFoodCols = foodColumnsForDisplay.size();
        if (index >= numDisplayedFoodCols) {
            return ndColumnsForDisplay.get(index - numDisplayedFoodCols);
        } else {
            return foodColumnsForDisplay.get(index);
        }
    }

    private void printLayout() throws IOException {
        newScreen();
        terminal.setCursorVisible(false);
        println("== Macros Food Editor ==");
        newline();
        // actions
        String actionIndicator = choosingAction ? " >" : ">>"; // print double caret if the action is active
        for (Action a : Action.values()) {
            String indicator = (a == currentAction) ? actionIndicator : "  ";
            print(String.format(" %s %s", indicator, a.name), actionPaddingWidth, false);
        }
        newline();
        newline();
        println("| Current command: " + currentAction);
        println("| Current column index: " + currentColumnIndex);
        newline();
        newline();
        println("| Food Details");


        int columnIndex = 0;
        for (Column<Food, ?> col: foodColumnsForDisplay) {
            terminalRowForColumnIndex.set(columnIndex, terminalRow);
            print("|");
            print(colStrings.getName(col), columnNameWidth, true);
            print(": ");
            Object data = foodBuilder.getField(col);
            print(data == null ? "" : data);
            newline();
            columnIndex++;
        }

        newline();

        println("| Nutrition Details");
        for (Column<NutritionData, ?> col: ndColumnsForDisplay) {
            terminalRowForColumnIndex.set(columnIndex, terminalRow);
            print("|");
            print(colStrings.getName(col), columnNameWidth, true);
            print(": ");
            Object data = nDataBuilder.getField(col);
            print(data == null ? "" : data);
            newline();
            columnIndex++;
        }

        terminal.flush();
        if (isEditing()) {
            terminal.setCursorVisible(true);
            // move cursor to relevant row, and columnNameWidth + 3 (3 for the "|" and ": ")
            int firstDataCol = columnNameWidth + 3;
            int activeColumnRow = terminalRowForColumnIndex.get(currentColumnIndex);
            terminal.setCursorPosition(firstDataCol, activeColumnRow);
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
        // TODO
    }

    private void save() {
        // TODO
    }

    private void quit() {
        finishedEditing = true;
    }

    private int numColumnsForDisplay() {
        return foodColumnsForDisplay.size() + ndColumnsForDisplay.size();
    }

    private void stepField(boolean forward) {
        if (forward) {
            if (++currentColumnIndex >= numColumnsForDisplay()) {
                currentColumnIndex = 0;
            }
        } else {
            if (--currentColumnIndex < 0) {
                currentColumnIndex = numColumnsForDisplay() - 1;
            }
        }
    }


}
