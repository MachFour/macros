package com.machfour.macros.cli.interactive;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalFactory;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.MacrosBuilder;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.datatype.TypeCastException;
import com.machfour.macros.names.*;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.UnicodeUtils;
import com.machfour.macros.validation.ValidationError;
import com.sun.tools.javac.tree.DCTree;
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

    private static final int NUM_ACTIONS = Action.values().length;
    private static final Collection<Column<Food, ?>> FOOD_TABLE_COLUMNS = Food.table().columns();
    private static final Collection<Column<NutritionData, ?>> ND_TABLE_COLUMNS = NutritionData.table().columns();

    // Layout parameters
    private static final int actionPaddingWidth = 15;
    private static final int columnNameWidth = 28;
    // move cursor to relevant row, and columnNameWidth + 3 (3 for the "|" and ": ")
    private static final int fieldValueStartCol = columnNameWidth + 3;
    // TODO this will cause long inputs to be cut off if there's an error message
    private static final int fieldValueWidth = 20;
    private static final int actionRow = 2;
    //private static final int errorMsgStartCol = fieldValueStartCol + fieldValueWidth + 1;

    private final MacrosDataSource ds;
    private final MacrosBuilder<Food> foodBuilder;
    private final MacrosBuilder<NutritionData> nDataBuilder;
    private final ColumnStrings colStrings;
    private final Terminal terminal;

    private final StringBuilder editingValue;
    // whether the current value has been changed at all
    private boolean valueIsEdited;

    // maps column Indices to terminal rows
    private final List<Integer> terminalRowForColumnIndex;
    private final List<String> errorMessageForColumnIndex;
    private final List<Column<Food, ?>> foodColumnsForDisplay;
    // current highlighted (if not editing) or last highlighted (if editing) Action
    private final List<Column<NutritionData, ?>> ndColumnsForDisplay;
    private final int layoutWidth = 79;

    private TextGraphics textGraphics;
    // have all the fields been validated by the MacrosBuilders
    private boolean isAllValidated;
    // whether the currentAction or the currentField should be highlihghed
    private boolean isEditing;
    // which field (Column) is currently highlighted (if editing) or was last active

    private int currentField;
    private int lastField;
    private boolean currentFieldNeedsReprint;
    private boolean lastFieldNeedsReprint;
    // lastField will be reprinted automatically if it's not equal to currentField

    @NotNull
    private Action currentAction;
    private boolean actionRowNeedsReprint;
    // records whether we need to repaint the whole UI for some reason.
    private boolean uiNeedsFullReprint;
    // whether we should exit at next input loop iteration
    private boolean finishedEditing;
    // where the printing is happening

    // current position of cursor
    private int terminalRow;
    private int terminalCol;
    // used for layout
    private int charsLeftOnLine;

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
        int totalColumns = numFoodColumns + numNdColumns;

        this.foodColumnsForDisplay = new ArrayList<>(numFoodColumns);
        this.ndColumnsForDisplay = new ArrayList<>(numNdColumns);
        this.terminalRowForColumnIndex = new ArrayList<>(totalColumns);
        this.errorMessageForColumnIndex = new ArrayList<>(totalColumns);

        this.terminal = terminal;

        this.currentAction = Action.SAVE;
        this.editingValue = new StringBuilder();

        initVariables();
    }

    private static Terminal defaultTerminal() throws IOException {
        TerminalFactory tf = new DefaultTerminalFactory();
        return tf.createTerminal();
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


    private boolean isNDataField(int fieldIndex) {
        return (fieldIndex >= foodColumnsForDisplay.size());
    }

    private <M extends MacrosEntity<M>, J> void acceptColumnInput(MacrosBuilder<M> builder,
                                                                  Column<M, J> col, String input) {
        String message = null; // message to display on the field after entry
        try {
            builder.setFieldFromString(col, input);
            List<ValidationError> errors = builder.validateSingle(col);
            if (!errors.isEmpty()) {
                // TODO
                message = errors.toString();
            }
        } catch (TypeCastException e) {
            message = "    ** (should be " + col.getType() + ")";
        }
        errorMessageForColumnIndex.set(currentField, message);
    }

    private void processEnter() throws IOException {
        if (isEditing) {
            stepField(true, true);
        }
        // else we're choosing an action, so start it
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
                    actionRowNeedsReprint = true;
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
                actionRowNeedsReprint = true;
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
            valueIsEdited = true;
            currentFieldNeedsReprint = true;
        }
    }
    private void processBackspace() {
        if (isEditing && editingValue.length() > 0) {
            editingValue.deleteCharAt(editingValue.length() - 1);
            valueIsEdited = true;
            currentFieldNeedsReprint = true;
        }
    }
    // Return true if the UI needs update after the command has processed
    // (i.e. because something changed)
    private boolean processCommand(@NotNull KeyStroke command) throws IOException {
        // keyType is one of the 'recognised' keytypes now
        KeyType type = command.getKeyType();
        switch (type) {
            case Character:
                processCharacter(command.getCharacter());
                return false;
            case Backspace:
                processBackspace();
                return false;
            case ArrowLeft:
            case ArrowRight:
            case ArrowUp:
            case ArrowDown:
                processArrow(type);
                return false;
            case Enter:
                processEnter();
                // repaint UI only if an action was selected
                return !isEditing;
            case Escape:
                processEscape();
                return true;
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
                command = terminal.readInput(); // blocking read
            }
            uiNeedsFullReprint = processCommand(command);
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

    private void initVariables() {
        this.valueIsEdited = false;

        this.terminalRow = 0;
        this.terminalCol = 0;
        this.charsLeftOnLine = 0;
        this.currentAction = Action.SAVE;
        this.isEditing = true;

        this.currentField = 0;
        this.lastField = 0;
        this.currentFieldNeedsReprint = false;
        this.lastFieldNeedsReprint = false;

        this.actionRowNeedsReprint = true;
        this.isAllValidated = false;
        this.finishedEditing = false;
        this.uiNeedsFullReprint = true;

    }

    public void init() throws IOException {
        initVariables();

        terminal.enterPrivateMode();
        newScreen(true);
        initDisplayColumns();
    }

    public void deInit() throws IOException {
        terminal.exitPrivateMode();
        terminal.close();
    }

    private void newScreen(boolean clear) throws IOException {
        if (clear) {
            terminal.clearScreen();
        }
        // refresh text graphics
        textGraphics = terminal.newTextGraphics();

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
            print(editingValue);
        } else {
            print(builder.getFieldAsString(col));
        }
        print(" ");
        // move cursor (on same line) to print error message
        if (errorMessageForColumnIndex.get(columnIndex) != null) {
            print(errorMessageForColumnIndex.get(columnIndex));
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

    /*
     * TODO only repaint things that change:
     * -- line being edited, and previous line
     * -- action select indicator
     */

    private void printActionRow() throws IOException {
        print("Actions: ", actionPaddingWidth, false);
        for (Action a : Action.values()) {
            String indicator = (!isEditing && a == currentAction) ? " > " : "   ";
            print(String.format(" %s %s", indicator, a.name), actionPaddingWidth, false);
        }
    }

    private void moveToRowAndClear(int row) throws IOException {
        int width = textGraphics.getSize().getColumns();
        textGraphics.drawLine(0, row, width, row, ' ');
        terminal.setCursorPosition(0, row);
        terminalRow = row;
        terminalCol = 0;
    }

    // erase, then printActionRow
    private void reprintActionRow() throws IOException {
        moveToRowAndClear(actionRow);
        printActionRow();
    }

    @SuppressWarnings("unchecked")
    private void reprintSingleColumn(int columnIndex) throws IOException {
        int whichRow = terminalRowForColumnIndex.get(columnIndex);
        moveToRowAndClear(whichRow);
        if (isNDataField(columnIndex)) {
            Column<NutritionData, ?> col = (Column<NutritionData, ?>) columnForFieldIndex(columnIndex);
            printColumn(col, nDataBuilder, columnIndex);
        } else {
            Column<Food, ?> col = (Column<Food, ?>) columnForFieldIndex(columnIndex);
            printColumn(col, foodBuilder, columnIndex);
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
        terminal.setCursorVisible(false);
        if (uiNeedsFullReprint) {
            uiNeedsFullReprint = false;

            newScreen(true);
            println("== Macros Food Editor ==");
            newline();
            // actions
            printActionRow();
            newline();

            println("| Food Details");
            newline();

            int numFoodColumns = printColumns(foodColumnsForDisplay, foodBuilder, 0);

            newline();
            println("| Nutrition Details");
            printColumns(ndColumnsForDisplay, nDataBuilder, numFoodColumns);

        } else {
            newScreen(false);
            if (actionRowNeedsReprint) {
                actionRowNeedsReprint = false;
                reprintActionRow();
            }
            // reprint fields
            if (currentFieldNeedsReprint) {
                currentFieldNeedsReprint = false;
                reprintSingleColumn(currentField);

            }
            if (lastFieldNeedsReprint) {
                lastFieldNeedsReprint = false;
                reprintSingleColumn(lastField);
            }
        }

        terminal.flush();

        if (isEditing) {
            terminal.setCursorVisible(true);
            int activeColumnRow = terminalRowForColumnIndex.get(currentField);
            terminal.setCursorPosition(fieldValueStartCol + editingValue.length(), activeColumnRow);
        }
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
        lastField = currentField;
        if (valueIsEdited) {
            lastFieldNeedsReprint = true;
            if (save)
            // move to next field
            saveIntoCurrentField(editingValue.toString());
        }
        valueIsEdited = false;

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
        // was resetEditingValue();
        editingValue.delete(0, editingValue.length());
        editingValue.append(getCurrentFieldData());
    }

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


}
