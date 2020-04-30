package com.machfour.macros.cli.interactive;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.MacrosBuilder;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.names.*;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.UnicodeUtils;
import com.machfour.macros.validation.ValidationError;
import org.jetbrains.annotations.NotNull;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.input.KeyStroke;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FoodEditor {
    private static final int NUM_ACTIONS = Action.values().length;
    private static final Collection<Column<Food, ?>> FOOD_TABLE_COLUMNS = Food.table().columns();
    private static final Collection<Column<NutritionData, ?>> ND_TABLE_COLUMNS = NutritionData.table().columns();

    // Layout parameters
    private static final int actionPaddingWidth = 18;
    private static final int columnNameWidth = 28;
    // move cursor to relevant row, and columnNameWidth + 3 (3 for the "|" and ": ")
    private static final int fieldValueStartCol = columnNameWidth + 3;
    private static final int fieldValueWidth = 20;
    //private static final int errorMsgStartCol = fieldValueStartCol + fieldValueWidth + 1;

    private final MacrosDataSource ds;
    private final MacrosBuilder<Food> foodBuilder;
    private final MacrosBuilder<NutritionData> nDataBuilder;
    private final ColumnStrings colStrings;
    private final Screen screen;

    private final StringBuilder editingValue;

    // maps column Indices to terminal rows
    private final List<Integer> terminalRowForColumnIndex;
    private final List<String> errorMessageForColumnIndex;

    private final List<Column<Food, ?>> foodColumnsForDisplay;
    private final List<Column<NutritionData, ?>> ndColumnsForDisplay;
    private final int layoutWidth = 79;

    // whether the currentAction or the currentField should be highlighted
    private boolean isEditing;
    // which field (Column) is currently highlighted (if editing) or was last active
    private int currentField;

    // a message at the top of the screen, above the action row
    @NotNull
    private String statusLine1;
    @NotNull
    private String statusLine2;

    // current highlighted (if not editing) or last highlighted (if editing) Action
    @NotNull
    private Action currentAction;
    // whether we should exit at next input loop iteration
    private boolean finishedEditing;

    private TextGraphics textGraphics;

    // current position of cursor
    private int terminalRow;
    private int terminalCol;
    // used for layout
    private int charsLeftOnLine;

    public FoodEditor(@NotNull MacrosDataSource ds,
                      @NotNull MacrosBuilder<Food> foodBuilder,
                      @NotNull MacrosBuilder<NutritionData> nDataBuilder) throws IOException {
        this(ds, foodBuilder, nDataBuilder, DefaultColumnStrings.getInstance(), defaultScreen());
    }

    // TODO servings?
    public FoodEditor(@NotNull MacrosDataSource ds, @NotNull MacrosBuilder<Food> foodBuilder,
                      @NotNull MacrosBuilder<NutritionData> nDataBuilder,
                      @NotNull ColumnStrings colStrings,
                      @NotNull Screen screen) {
        this.ds = ds;
        this.foodBuilder = foodBuilder;
        this.nDataBuilder = nDataBuilder;
        this.colStrings = colStrings;

        int numFoodColumns = FOOD_TABLE_COLUMNS.size();
        int numNdColumns = ND_TABLE_COLUMNS.size();
        int totalColumns = numFoodColumns + numNdColumns;

        this.foodColumnsForDisplay = new ArrayList<>(numFoodColumns);
        this.ndColumnsForDisplay = new ArrayList<>(numNdColumns);
        this.terminalRowForColumnIndex = new ArrayList<>(totalColumns);
        this.errorMessageForColumnIndex = new ArrayList<>(totalColumns);

        this.screen = screen;

        this.currentAction = Action.SAVE;
        this.statusLine1 = "Welcome to the food editor.";
        this.statusLine2 = "Use the arrow keys to navigate, and enter to confirm an edit or select an action";
        this.editingValue = new StringBuilder();

        initVariables();
    }

    private static Screen defaultScreen() throws IOException {
        DefaultTerminalFactory tf = new DefaultTerminalFactory();
        return tf.createScreen();
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

    @NotNull
    private static String errorMessageHint() {
        return " *";
    }

    @Nullable
    private <M extends MacrosEntity<M>, J> String getErrorMessage(MacrosBuilder<M> b, Column<M, J> col) {
        String message = null;
        List<ValidationError> errors = b.getErrors(col);
        if (!errors.isEmpty()) {
            // just display first error
            switch (errors.get(0)) {
                case NON_NULL:
                    message = " ** Cannot be empty";
                    break;
                case TYPE_MISMATCH:
                    message = " ** Must be of type '" + col.getType() + "'";
                    break;
                default:
                    message = " ** Error : " + errors.get(0).toString();
                    break;
            }
        }
        return message;
    }


    private boolean isNDataField(int fieldIndex) {
        return (fieldIndex >= foodColumnsForDisplay.size());
    }

    private <M extends MacrosEntity<M>, J> void acceptColumnInput(MacrosBuilder<M> b, Column<M, J> col, String input) {
        b.setFieldFromString(col, input);
        String message = getErrorMessage(b, col);
        errorMessageForColumnIndex.set(currentField, message);
    }

    private void setStatus(@Nullable String line1, @Nullable String line2) {
        statusLine1 = line1 == null ? "" : line1;
        statusLine2 = line2 == null ? "" : line2;
    }
    private void setStatus(@Nullable String line1) {
        setStatus(line1, null);
    }

    private void processEnter() throws IOException {
        if (isEditing) {
            stepField(true, true);
        } else {
            // else we're choosing an action, so start it
            switch (currentAction) {
                case SAVE:
                    trySave();
                    break;
                case HELP:
                    setStatus("Enter the food details for each field below, then choose save",
                            "Use the up/down keys to navigate fields, and the left/right keys to navigate actions");
                    break;
                case MORE_FIELDS:
                    break;
                case RESET:
                    // TODO confirm
                    reset();
                    break;
                case EXIT:
                    // TODO confirm quit
                    quit();
            }
        }
    }

    private void reset() {
        foodBuilder.resetFields();
        nDataBuilder.resetFields();
    }

    private void processArrow(KeyType kt) {
        // up and down cancel edits and move to next field
        // TODO don't cancel edits by default, but holding shift cancels edits
        switch (kt) {
            case ArrowUp:
            case ArrowDown:
                if (isEditing) {
                    // handles setting of reprinting
                    stepField(kt == KeyType.ArrowDown, false);
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
    }

    private void processEscape() {
        quit();
    }

    private void processCharacter(char c) {
        if (isEditing) {
            editingValue.append(c);
        }
    }
    private void processBackspace() {
        if (isEditing && editingValue.length() > 0) {
            editingValue.deleteCharAt(editingValue.length() - 1);
        }
    }
    // Return true if the UI needs update after the command has processed
    // (i.e. because something changed)
    private void processCommand(@NotNull KeyStroke command) throws IOException {
        // keyType is one of the 'recognised' keytypes now
        KeyType type = command.getKeyType();
        switch (type) {
            case Character:
                processCharacter(command.getCharacter());
                break;
            case Backspace:
                processBackspace();
                break;
            case ArrowLeft:
            case ArrowRight:
            case ArrowUp:
            case ArrowDown:
                processArrow(type);
                break;
            case Enter:
                processEnter();
                break;
            case Escape:
                processEscape();
                break;
            default:
                throw new IllegalArgumentException("Unrecognised keytype: " + command.getKeyType());
        }
    }

    public void run() throws IOException {
        while (!finishedEditing) {
            // UI loop
            printLayout();
            KeyStroke command = null;
            // wait for keystroke
            while (command == null || !isRecognisedKeyType(command.getKeyType())) {
                command = screen.readInput(); // blocking read
            }
            processCommand(command);
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
        // fill the Lists with blank data so we can set() them later
        for (int i = 0; i < totalCols; i++) {
            terminalRowForColumnIndex.add(0);
            errorMessageForColumnIndex.add(null);
        }
    }

    private void setEditingValue(String initial) {
        editingValue.delete(0, editingValue.length());
        editingValue.append(initial);
    }

    private void initVariables() {
        this.terminalRow = 0;
        this.terminalCol = 0;
        this.charsLeftOnLine = 0;
        this.currentAction = Action.SAVE;
        this.isEditing = true;

        this.currentField = 0;

        this.finishedEditing = false;
    }

    public void init() throws IOException {
        initVariables();
        initDisplayColumns();
        setEditingValue(getCurrentFieldData());

        screen.startScreen();
    }

    public void deInit() throws IOException {
        screen.stopScreen();
        screen.close();
    }

    private void newScreen() {
        screen.clear();
        // refresh text graphics
        textGraphics = screen.newTextGraphics();

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

    private void newline() {
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

    private Column<?, ?> columnForFieldIndex(int index) {
        int numDisplayedFoodCols = foodColumnsForDisplay.size();
        if (index < numDisplayedFoodCols) {
            return foodColumnsForDisplay.get(index);
        } else {
            return ndColumnsForDisplay.get(index - numDisplayedFoodCols);
        }
    }

    private <M extends MacrosEntity<M>, J> void printColumn(Column<M, J> col, MacrosBuilder<M> builder, int columnIndex)
            throws IOException {
        print("|");
        print(colStrings.getName(col), columnNameWidth, true);
        print(": ");
        if (isEditing && columnIndex == currentField) {
            // print out only last fieldValueWidth chars, if the entry is too long
            String editValue = editingValue.toString();
            int len = editValue.length();
            if (len > fieldValueWidth) {
                editValue = editValue.substring(len - fieldValueWidth, len);
            }
            print(editValue, fieldValueWidth, false);
        } else {
            print(builder.getFieldAsString(col), fieldValueWidth, false);
        }
        print(" ");
        // move cursor (on same line) to print error message
        if (errorMessageForColumnIndex.get(columnIndex) != null) {
            if (columnIndex == currentField) {
                // print the full message
                print(errorMessageForColumnIndex.get(columnIndex));
            } else {
                // just print a star
                print(errorMessageHint());
            }
        }
        newline();
    }

    // returns end index
    private <M extends MacrosEntity<M>> int printColumns(Collection<Column<M, ?>> columns,
                MacrosBuilder<M> builder, int initialColumnIndex) throws IOException {
        int columnIndex = initialColumnIndex;
        for (Column<M, ?> col: columns) {
            terminalRowForColumnIndex.set(columnIndex, terminalRow);
            printColumn(col, builder, columnIndex);
            columnIndex++;
        }
        return columnIndex; // final column index
    }


    private void printActionRow() throws IOException {
        println("Actions: ", actionPaddingWidth, false);
        for (Action a : Action.values()) {
            String indicator = (!isEditing && a == currentAction) ? " > " : "   ";
            print(String.format(" %s %s", indicator, a.name), actionPaddingWidth, true);
        }
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
    private void printLayout() throws IOException {
        newScreen();
        println("== Macros Food Editor ==");
        newline();
        println(statusLine1);
        println(statusLine2);
        newline();
        // actions
        printActionRow();
        newline();
        newline();

        println("Food Details");
        println("|");

        int numFoodColumns = printColumns(foodColumnsForDisplay, foodBuilder, 0);

        newline();
        println("Nutrition Details");
        println("|");
        printColumns(ndColumnsForDisplay, nDataBuilder, numFoodColumns);

        if (isEditing) {
            int activeFieldRow = terminalRowForColumnIndex.get(currentField);
            // if the text entered is more than the fieldValueWidth, we truncate the display to the last
            // fieldValueWidth chars, which means that so the cursor should not advance across the screen
            int column = fieldValueStartCol + Math.min(editingValue.length(), fieldValueWidth);
            TerminalPosition newPos = new TerminalPosition(column, activeFieldRow);
            screen.setCursorPosition(newPos);
        }

        screen.refresh(Screen.RefreshType.DELTA);
    }

    private boolean isAllValid() {
        Map<Column<Food, ?>, List<ValidationError>> foodErrors = foodBuilder.getAllErrors();
        Map<Column<NutritionData, ?>, List<ValidationError>> nDataErrors = nDataBuilder.getAllErrors();
        return foodErrors.isEmpty() && nDataErrors.isEmpty();
    }

    private void trySave() {
        // TODO print out which columns
        if (isAllValid()) {
            Food f = foodBuilder.build();
            NutritionData nd = nDataBuilder.build();
            try {
                ds.saveObject(f);
                // TODO get food ID, etc.
                ds.saveObject(nd);
                setStatus("Successfully saved food and nutrition data", "");
            } catch (SQLException e) {
                setStatus("Could not save!", "SQL Exception: " + e.getLocalizedMessage());
            }
        } else {
            setStatus("Could not save!", "Check columns with * characters");
        }
    }

    private void quit() {
        finishedEditing = true;
    }

    @SuppressWarnings("unchecked")
    private void saveIntoCurrentField(String input) {
        // save and validate
        if (isNDataField(currentField)) {
            Column<NutritionData, ?> col = (Column<NutritionData, ?>)columnForFieldIndex(currentField);
            acceptColumnInput(nDataBuilder, col, input);
        } else {
            Column<Food, ?> col = (Column<Food, ?>)columnForFieldIndex(currentField);
            acceptColumnInput(foodBuilder, col, input);
        }
    }

    @SuppressWarnings("unchecked")
    private String getCurrentFieldData() {
        // save and validate
        if (isNDataField(currentField)) {
            Column<NutritionData, ?> col = (Column<NutritionData, ?>)columnForFieldIndex(currentField);
            return nDataBuilder.getFieldAsString(col);
        } else {
            Column<Food, ?> col = (Column<Food, ?>)columnForFieldIndex(currentField);
            return foodBuilder.getFieldAsString(col);
        }

    }

    private void stepField(boolean forward, boolean save) {
        final int displayedFields = foodColumnsForDisplay.size() + ndColumnsForDisplay.size();
        if (save) {
            // move to next field
            saveIntoCurrentField(editingValue.toString());
        }

        // advance current field
        if (forward) {
            if (++currentField >= displayedFields) {
                currentField = 0;
            }
        } else {
            if (--currentField < 0) {
                currentField = displayedFields - 1;
            }
        }
        setEditingValue(getCurrentFieldData());
    }

    private enum Action {
          MORE_FIELDS("Extra fields")
        , SAVE("Save")
        , RESET("Reset")
        , EXIT("Exit")
        , HELP("Help")
        ;

        final String name;

        Action(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String toString() {
            return name;
        }
    }


}
