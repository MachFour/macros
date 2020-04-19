package com.machfour.macros.cli.interactive;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalFactory;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.MacrosBuilder;
import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.names.*;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.UnicodeUtils;
import com.machfour.macros.validation.ValidationError;
import org.jetbrains.annotations.NotNull;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.input.KeyStroke;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FoodEditor {

    private enum Action {
          SAVE("Save")
        , RESET("Reset")
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


    // whether the currentAction or the currentField should be highlihghed
    private boolean isEditing;
    // which field (Column) is currently highlighted (if editing) or was last active
    private int currentField;
    // current highlighted (if not editing) or last highlighted (if editing) Action
    @NotNull
    private Action currentAction;

    private final StringBuilder editingValue;

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

    public FoodEditor(@NotNull MacrosDataSource ds,
                      @NotNull MacrosBuilder<Food> foodBuilder,
                      @NotNull MacrosBuilder<NutritionData> nDataBuilder) throws IOException {
        this(ds, foodBuilder, nDataBuilder, DefaultColumnStrings.getInstance(), defaultTerminal());
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

        this.editingValue = new StringBuilder();

        this.terminalRow = 0;
        this.terminalCol = 0;
        this.charsLeftOnLine = 0;
        this.currentAction = Action.SAVE;
        this.isEditing = true;
        this.currentField = 0;
        this.isAllValidated = false;
        this.finishedEditing = false;
        this.uiNeedsReprint = true;
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

    private void resetEditingValue() {
        editingValue.delete(0, editingValue.length());
    }

    private boolean isEditingNutritionData() {
        return (currentField >= foodColumnsForDisplay.size());
    }

    @SuppressWarnings("unchecked")
    private boolean processEnter() throws IOException {
        if (isEditing) {
            // Enter saves the edit and moves to the next field
            // save and validate
            if (isEditingNutritionData()) {
                Column<NutritionData, ?> col = (Column<NutritionData, ?>)columnForColumnIndex(currentField);
                nDataBuilder.setFieldFromString(col, editingValue.toString());

            } else {
                Column<Food, ?> col = (Column<Food, ?>)columnForColumnIndex(currentField);
                foodBuilder.setFieldFromString(col, editingValue.toString());
            }
            // move to next field
            resetEditingValue();
            stepField(true);
        } else {
            // select the action and start it
            startAction();
        }
        // always needs repaint
        return true;
    }

    // begin the currently selected action (currentAction)
    private void startAction() {
        switch (currentAction) {
            case SAVE:
                // TODO
                validateAll();
                if (isAllValidated) {
                    save();
                } else {
                    // TODO print could not save
                }
                save();
                break;
            case EXIT:
                // TODO confirm quit
                quit();
        }
    }

    private boolean processArrow(KeyType kt) throws IOException {
        // up and down cancel edits and move to next field
        // TODO don't cancel edits by default, but holding shift cancels edits
        resetEditingValue();
        switch (kt) {
            case ArrowUp:
            case ArrowDown:
                if (isEditing) {
                    stepField(kt == KeyType.ArrowDown);
                } else {
                    isEditing = true;
                }
                break;
            // left and right cancel edits and step actions
            case ArrowLeft:
            case ArrowRight:
                if (!isEditing) {
                    stepAction(kt == KeyType.ArrowRight);
                } else {
                    isEditing = false;
                }
                break;
            default:
                throw new IllegalArgumentException("KeyType is not an arrow key");
        }
        return true;
    }

    private boolean processEscape() {
        quit();
        return true;
    }

    private boolean processCharacter(char c) {
        if (isEditing) {
            editingValue.append(c);
            return true;
        } else {
            return false;
        }
    }
    private boolean processBackspace() {
        if (isEditing && editingValue.length() > 0) {
            editingValue.deleteCharAt(editingValue.length() - 1);
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
                return processCharacter(command.getCharacter());
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
        while (!finishedEditing) {
                // UI loop
            if (uiNeedsReprint) {
                printLayout();
                uiNeedsReprint = false;
            }
            KeyStroke command = null;
            // wait for keystroke
            while (command == null || !isRecognisedKeyType(command.getKeyType())) {
                command = terminal.readInput(); // blocking read
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

        final int totalCols = foodColumnsForDisplay.size() + ndColumnsForDisplay.size();
        // also fill the terminalRowForColumnIndex so we can set() it later
        for (int i = 0; i < totalCols; i++) {
            terminalRowForColumnIndex.add(0);
        }
    }

    public void init() throws IOException {
        terminal.enterPrivateMode();
        terminal.clearScreen();
        textGraphics = terminal.newTextGraphics();

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
    private static final int actionPaddingWidth = 15;

    private Column<?, ?> columnForColumnIndex(int index) {
        int numDisplayedFoodCols = foodColumnsForDisplay.size();
        if (index >= numDisplayedFoodCols) {
            return ndColumnsForDisplay.get(index - numDisplayedFoodCols);
        } else {
            return foodColumnsForDisplay.get(index);
        }
    }

    // returns end index
    private <M extends MacrosPersistable<M>> int printColumns(Collection<Column<M, ?>> columns,
                MacrosBuilder<M> builder, int initialColumnIndex) throws IOException {
        int columnIndex = initialColumnIndex;
        for (Column<M, ?> col: columns) {
            terminalRowForColumnIndex.set(columnIndex, terminalRow);
            print("|");
            print(colStrings.getName(col), columnNameWidth, true);
            print(": ");
            if (isEditing && columnIndex == currentField) {
                print(editingValue);
            } else {
                Object data = builder.getField(col);
                print(data == null ? "" : data);
            }
            // TODO validations
            newline();
            columnIndex++;
        }
        return columnIndex; // final column index
    }

    /*
     * TODO only repaint things that change:
     * -- line being edited, and previous line
     * -- action select indicator
     */

    private void printLayout() throws IOException {
        newScreen();
        terminal.setCursorVisible(false);
        println("== Macros Food Editor ==");
        newline();
        // actions
        print("Actions: ", actionPaddingWidth, false);
        for (Action a : Action.values()) {
            String indicator = (!isEditing && a == currentAction) ? " > " : "   ";
            print(String.format(" %s %s", indicator, a.name), actionPaddingWidth, false);
        }
        newline();
        newline();
        /*
        println("| Current command: " + currentAction);
        println("| Current column index: " + currentField);
        newline();
        newline();
         */

        println("| Food Details");
        newline();

        int numFoodColumns = printColumns(foodColumnsForDisplay, foodBuilder, 0);

        newline();
        println("| Nutrition Details");
        printColumns(ndColumnsForDisplay, nDataBuilder, numFoodColumns);

        if (isEditing) {
            terminal.setCursorVisible(true);
            // move cursor to relevant row, and columnNameWidth + 3 (3 for the "|" and ": ")
            int firstDataCol = columnNameWidth + 3;
            int activeColumnRow = terminalRowForColumnIndex.get(currentField);
            terminal.setCursorPosition(firstDataCol + editingValue.length(), activeColumnRow);
        }

        terminal.flush();



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
        Map<Column<Food, ?>, List<ValidationError>> foodErrors = foodBuilder.findAllErrors();
        Map<Column<NutritionData, ?>, List<ValidationError>> nDataErrors = nDataBuilder.findAllErrors();
        isAllValidated = foodErrors.isEmpty() && nDataErrors.isEmpty();
        // TODO print errors
    }

    private void save() {
        if (isAllValidated) {
            Food f = foodBuilder.build();
            NutritionData nd = nDataBuilder.build();
            try {
                ds.saveObject(f);
                // TODO get food ID, etc.
                ds.saveObject(nd);
            } catch (SQLException e) {
                // TODO print message

            }
        }
        // TODO print message if cannot save
    }

    private void quit() {
        finishedEditing = true;
    }

    private void stepField(boolean forward) {
        final int displayedFields = foodColumnsForDisplay.size() + ndColumnsForDisplay.size();
        if (forward) {
            if (++currentField >= displayedFields) {
                currentField = 0;
            }
        } else {
            if (--currentField < 0) {
                currentField = displayedFields - 1;
            }
        }
    }


}
