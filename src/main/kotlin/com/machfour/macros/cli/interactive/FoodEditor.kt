package com.machfour.macros.cli.interactive

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.machfour.macros.core.Column
import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.Schema
import com.machfour.macros.names.ColumnStrings
import com.machfour.macros.names.DefaultColumnStrings
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.queries.FkCompletion
import com.machfour.macros.queries.Queries
import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.util.MiscUtils
import com.machfour.macros.util.UnicodeUtils
import com.machfour.macros.validation.ValidationError

import java.io.IOException
import java.sql.SQLException
import java.util.ArrayList

// TODO servings?
class FoodEditor constructor(
        //private static final int errorMsgStartCol = fieldValueStartCol + fieldValueWidth + 1;
        private val ds: MacrosDataSource, private val foodBuilder: MacrosBuilder<Food>,
        private val nDataBuilder: MacrosBuilder<NutritionData>,
        private val colStrings: ColumnStrings = DefaultColumnStrings.instance,
        private val screen: Screen = defaultScreen()) {

    companion object {
        private val NUM_ACTIONS = Action.values().size
        private val FOOD_TABLE_COLUMNS = Food.table().columns
        private val ND_TABLE_COLUMNS = NutritionData.table().columns

        // Layout parameters
        private const val actionPaddingWidth = 18
        private const val columnNameWidth = 28

        // move cursor to relevant row, and columnNameWidth + 3 (3 for the "|" and ": ")
        private const val fieldValueStartCol = columnNameWidth + 3
        private const val fieldValueWidth = 20

        @Throws(IOException::class)
        private fun defaultScreen(): Screen {
            val tf = DefaultTerminalFactory()
            return tf.createScreen()
        }

        private fun isRecognisedKeyType(kt: KeyType): Boolean {
            return when (kt) {
                // things we care about
                KeyType.Character,
                KeyType.Backspace,
                KeyType.ArrowLeft,
                KeyType.ArrowRight,
                KeyType.ArrowUp,
                KeyType.ArrowDown,
                KeyType.Enter,
                KeyType.Escape -> true
                // things we don't care about
                KeyType.MouseEvent,
                KeyType.Home,
                KeyType.Delete,
                KeyType.EOF,
                KeyType.ReverseTab,
                KeyType.Tab,
                KeyType.Insert,
                KeyType.CursorLocation -> false
                else -> false
            }
        }

        private fun errorMessageHint(): String {
            return " *"
        }
    }



    private val editingValue: StringBuilder

    // maps column Indices to terminal rows
    private val terminalRowForColumnIndex: MutableList<Int>
    private val errorMessageForColumnIndex: MutableList<String?>

    private val foodColumnsForDisplay: MutableList<Column<Food, *>>
    private val ndColumnsForDisplay: MutableList<Column<NutritionData, *>>
    private val layoutWidth = 79

    // whether the currentAction or the currentField should be highlighted
    private var isEditing: Boolean = false

    // which field (Column) is currently highlighted (if editing) or was last active
    private var currentField: Int = 0

    // a message at the top of the screen, above the action row
    private var statusLine1: String
    private var statusLine2: String
    private var exceptionMessage: String

    // current highlighted (if not editing) or last highlighted (if editing) Action
    private var currentAction: Action

    // whether we should exit at next input loop iteration
    private var finishedEditing: Boolean = false

    private var textGraphics: TextGraphics? = null

    // current position of cursor
    private var terminalRow: Int = 0
    private var terminalCol: Int = 0

    // used for layout
    private var charsLeftOnLine: Int = 0

    private val isAllValid: Boolean
        get() {
            val foodErrors = foodBuilder.allErrors
            val nDataErrors = nDataBuilder.allErrors
            return foodErrors.isEmpty() && nDataErrors.isEmpty()
        }

    // save and validate
    private val currentFieldData: String
        @Suppress("UNCHECKED_CAST")
        get() {
            return if (isNDataField(currentField)) {
                val col = columnForFieldIndex(currentField) as Column<NutritionData, *>
                nDataBuilder.getFieldAsString(col)
            } else {
                val col = columnForFieldIndex(currentField) as Column<Food, *>
                foodBuilder.getFieldAsString(col)
            }
        }

    init {

        val numFoodColumns = FOOD_TABLE_COLUMNS.size
        val numNdColumns = ND_TABLE_COLUMNS.size
        val totalColumns = numFoodColumns + numNdColumns

        this.foodColumnsForDisplay = ArrayList(numFoodColumns)
        this.ndColumnsForDisplay = ArrayList(numNdColumns)
        this.terminalRowForColumnIndex = ArrayList(totalColumns)
        this.errorMessageForColumnIndex = ArrayList(totalColumns)

        this.currentAction = Action.SAVE
        this.statusLine1 = "Welcome to the food editor."
        this.statusLine2 = "Use the arrow keys to navigate, and enter to confirm an edit or select an action"
        this.exceptionMessage = ""
        this.editingValue = StringBuilder()

        initVariables()
    }

    private fun stepAction(forward: Boolean) {
        var nextActionOrdinal = currentAction.ordinal
        if (forward) {
            if (++nextActionOrdinal >= NUM_ACTIONS) {
                nextActionOrdinal -= NUM_ACTIONS
            }
        } else {
            if (--nextActionOrdinal < 0) {
                nextActionOrdinal += NUM_ACTIONS
            }
        }
        currentAction = Action.values()[nextActionOrdinal]
    }

    private fun <M : MacrosEntity<M>, J> getErrorMessage(b: MacrosBuilder<M>, col: Column<M, J>): String? {
        val errors = b.getErrors(col)
        return if (errors.isNotEmpty()) {
            // just display first error
            when (errors[0]) {
                ValidationError.NON_NULL -> " ** Cannot be empty"
                ValidationError.TYPE_MISMATCH -> " ** Must be of type '" + col.type + "'"
                else -> " ** Error : " + errors[0].toString()
            }
        } else {
            null
        }
    }


    private fun isNDataField(fieldIndex: Int): Boolean {
        return fieldIndex >= foodColumnsForDisplay.size
    }

    private fun <M : MacrosEntity<M>, J> acceptColumnInput(b: MacrosBuilder<M>, col: Column<M, J>, input: String) {
        b.setFieldFromString(col, input)
        val message = getErrorMessage(b, col)
        errorMessageForColumnIndex[currentField] = message
    }

    private fun clearStatus() {
        statusLine1 = ""
        statusLine2 = ""
    }

    private fun clearExceptionMessage() {
        exceptionMessage = ""
    }

    private fun setStatus(line1: String, line2: String = "") {
        // replace newlines with spaces
        statusLine1 = line1.replace("\n".toRegex(), " ")
        statusLine2 = line2.replace("\n".toRegex(), " ")
    }

    // a long message to print at the end
    private fun setExceptionMessage(value: String) {
        this.exceptionMessage = value

    }


    @Throws(IOException::class)
    private fun processEnter() {
        if (isEditing) {
            stepField(true, true)
        } else {
            // else we're choosing an action, so start it
            when (currentAction) {
                Action.SAVE -> trySave()
                Action.HELP -> setStatus("Enter the food details for each field below, then choose save",
                        "Use the up/down keys to navigate fields, and the left/right keys to navigate actions")
                Action.MORE_FIELDS -> {
                }
                Action.RESET -> {
                    // TODO confirm
                    reset()
                }
                Action.EXIT -> {
                    // TODO confirm quit
                    quit()
                }
            }
        }
    }

    private fun reset() {
        foodBuilder.resetFields()
        nDataBuilder.resetFields()
        clearStatus()
        clearExceptionMessage()
    }

    // up and down saves edits and move to next field
    // holding shift cancels edits
    // left and right scrolls through actions
    private fun processArrow(key: KeyStroke) {
        val kt = key.keyType
        val isShiftHeld = key.isShiftDown
        when (kt) {
            KeyType.ArrowUp, KeyType.ArrowDown -> if (isEditing) {
                // Don't cancel edits by default, but holding shift cancels edits
                stepField(kt == KeyType.ArrowDown, !isShiftHeld)
            } else {
                isEditing = true
            }
            // left and right cancel edits and step actions
            KeyType.ArrowLeft, KeyType.ArrowRight -> if (!isEditing) {
                stepAction(kt == KeyType.ArrowRight)
            } else {
                isEditing = false
            }
            else -> throw IllegalArgumentException("KeyType is not an arrow key")
        }
    }

    private fun processEscape() {
        quit()
    }

    private fun processCharacter(c: Char) {
        if (isEditing) {
            editingValue.append(c)
        }
    }

    private fun processBackspace() {
        if (isEditing && editingValue.isNotEmpty()) {
            editingValue.deleteCharAt(editingValue.length - 1)
        }
    }

    // Return true if the UI needs update after the command has processed
    // (i.e. because something changed)
    @Throws(IOException::class)
    private fun processCommand(key: KeyStroke) {
        // keyType is one of the 'recognised' keytypes now
        when (key.keyType) {
            KeyType.Character -> processCharacter(key.character!!)
            KeyType.Backspace -> processBackspace()
            KeyType.ArrowLeft, KeyType.ArrowRight, KeyType.ArrowUp, KeyType.ArrowDown -> processArrow(key)
            KeyType.Enter -> processEnter()
            KeyType.Escape -> processEscape()
            else -> throw IllegalArgumentException("Unrecognised keytype: " + key.keyType)
        }
    }

    @Throws(IOException::class)
    fun run() {
        while (!finishedEditing) {
            // UI loop
            printLayout()
            var command: KeyStroke? = null
            // wait for keystroke
            while (command == null || !isRecognisedKeyType(command.keyType)) {
                command = screen.readInput() // blocking read
            }
            processCommand(command)
        }
    }

    // figure out which columns should be displayed.
    // Defaults to isUserEditable()
    private fun initDisplayColumns() {
        for (col in FOOD_TABLE_COLUMNS) {
            if (col.isUserEditable) {
                foodColumnsForDisplay.add(col)
            }
        }
        // TODO display minimal set of nutrient columns first
        for (col in ND_TABLE_COLUMNS) {
            if (col.isUserEditable) {
                ndColumnsForDisplay.add(col)
            }
        }

        val totalCols = foodColumnsForDisplay.size + ndColumnsForDisplay.size
        // fill the Lists with blank data so we can set() them later
        for (i in 0 until totalCols) {
            terminalRowForColumnIndex.add(0)
            errorMessageForColumnIndex.add(null)
        }
    }

    private fun setEditingValue(initial: String) {
        editingValue.delete(0, editingValue.length)
        editingValue.append(initial)
    }

    private fun initVariables() {
        this.terminalRow = 0
        this.terminalCol = 0
        this.charsLeftOnLine = 0
        this.currentAction = Action.SAVE
        this.isEditing = true

        this.currentField = 0

        this.finishedEditing = false
    }

    @Throws(IOException::class)
    fun init() {
        initVariables()
        initDisplayColumns()
        setEditingValue(currentFieldData)

        screen.startScreen()
    }

    @Throws(IOException::class)
    fun deInit() {
        screen.stopScreen()
        screen.close()
    }

    private fun newScreen() {
        screen.clear()
        // refresh text graphics
        textGraphics = screen.newTextGraphics()

        terminalRow = 0
        terminalCol = 0
    }


    // print string, left aligned in given width, or just print if width <= 0
    // Object o's toString() method must not contain any new lines or tab characters
    private fun padString(o: Any, width: Int, rightAlign: Boolean): String {
        var s = o.toString()
        require(!(s.contains("\n") || s.contains("\t"))) { "Object's toString() cannot contain newline or tab characters" }
        if (width <= 0) {
            return s
        } else {
            // account for wide characters - these take twice as much space
            var wideChars = UnicodeUtils.countDoubleWidthChars(s)
            var displayLength = s.length + wideChars
            if (displayLength > width) {
                // string has to be truncated: subtract (displayLength - width) chars
                val maxIndex = s.length - (displayLength - width) // == width - wideChars
                s = s.substring(0, maxIndex)
                // recount the wide characters and display length
                wideChars = UnicodeUtils.countDoubleWidthChars(s)
                displayLength = s.length + wideChars
            }
            assert(displayLength <= width)
            val align = if (rightAlign) "" else "-"
            return String.format("%" + align + (width - wideChars) + "s", s)
        }
    }

    private fun newline() {
        //TerminalPosition currentPos = terminal.getCursorPosition();
        //terminal.setCursorPosition(currentPos.withRelativeRow(2).getRow(), 0);
        //terminal.putCharacter('\n');
        terminalRow += 1
        terminalCol = 0
    }

    @Throws(IOException::class)
    private fun println(o: Any) {
        print(o, 0, false, true)
    }

    @Throws(IOException::class)
    private fun println(o: Any, width: Int, rightAlign: Boolean) {
        print(o, width, rightAlign, true)
    }

    @Throws(IOException::class)
    private fun print(o: Any, width: Int = 0, rightAlign: Boolean = false, newline: Boolean = false) {
        val padded = padString(o, width, rightAlign)
        charsLeftOnLine -= width
        textGraphics!!.putString(terminalCol, terminalRow, padded)
        terminalCol += padded.length

        if (newline) {
            newline()
        }
    }

    private fun columnForFieldIndex(index: Int): Column<*, *> {
        val numDisplayedFoodCols = foodColumnsForDisplay.size
        return if (index < numDisplayedFoodCols) {
            foodColumnsForDisplay[index]
        } else {
            ndColumnsForDisplay[index - numDisplayedFoodCols]
        }
    }

    @Throws(IOException::class)
    private fun <M : MacrosEntity<M>, J> printColumn(col: Column<M, J>, builder: MacrosBuilder<M>, columnIndex: Int) {
        print("|")
        print(colStrings.getName(col), columnNameWidth, true)
        print(": ")
        if (isEditing && columnIndex == currentField) {
            // print out only last fieldValueWidth chars, if the entry is too long
            var editValue = editingValue.toString()
            val len = editValue.length
            if (len > fieldValueWidth) {
                editValue = editValue.substring(len - fieldValueWidth, len)
            }
            print(editValue, fieldValueWidth, false)
        } else {
            print(builder.getFieldAsString(col), fieldValueWidth, false)
        }
        print(" ")
        // move cursor (on same line) to print error message
        if (errorMessageForColumnIndex[columnIndex] != null) {
            if (columnIndex == currentField) {
                // print the full message
                print(errorMessageForColumnIndex[columnIndex])
            } else {
                // just print a star
                print(errorMessageHint())
            }
        }
        newline()
    }

    // returns end index
    @Throws(IOException::class)
    private fun <M : MacrosEntity<M>> printColumns(columns: Collection<Column<M, *>>, builder: MacrosBuilder<M>, initialColumnIndex: Int): Int {
        var columnIndex = initialColumnIndex
        for (col in columns) {
            terminalRowForColumnIndex[columnIndex] = terminalRow
            printColumn(col, builder, columnIndex)
            columnIndex++
        }
        return columnIndex // final column index
    }


    @Throws(IOException::class)
    private fun printActionRow() {
        println("Actions: ", actionPaddingWidth, false)
        for (a in Action.values()) {
            val indicator = if (!isEditing && a == currentAction) " > " else "   "
            print(String.format(" %s %s", indicator, a.niceName), actionPaddingWidth, true)
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
    @Throws(IOException::class)
    private fun printLayout() {
        newScreen()
        println("== Macros Food Editor ==")
        newline()
        println(statusLine1)
        println(statusLine2)
        newline()
        // actions
        printActionRow()
        newline()
        newline()

        println("Food Details")
        println("|")

        val numFoodColumns = printColumns(foodColumnsForDisplay, foodBuilder, 0)

        newline()
        println("Nutrition Details")
        println("|")
        printColumns(ndColumnsForDisplay, nDataBuilder, numFoodColumns)

        if (exceptionMessage.isNotEmpty()) {
            newline()
            newline()
            println("*****************************************")
            val exceptionLines = exceptionMessage.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (line in exceptionLines) {
                println(line)
            }
        }

        if (isEditing) {
            val activeFieldRow = terminalRowForColumnIndex[currentField]
            // if the text entered is more than the fieldValueWidth, we truncate the display to the last
            // fieldValueWidth chars, which means that so the cursor should not advance across the screen
            val column = fieldValueStartCol + editingValue.length.coerceAtMost(fieldValueWidth)
            val newPos = TerminalPosition(column, activeFieldRow)
            screen.cursorPosition = newPos
        }

        screen.refresh(Screen.RefreshType.DELTA)
    }

    private fun trySave() {
        // TODO print out which columns
        if (isAllValid) {
            val f = foodBuilder.build()
            val nd = nDataBuilder.build()
            val indexName = f.indexName
            try {
                try {
                    // link the food to the nd
                    nd.setFkParentNaturalKey(Schema.NutritionDataTable.FOOD_ID, Schema.FoodTable.INDEX_NAME, indexName)

                    // gotta do it in one go
                    ds.openConnection()
                    ds.beginTransaction()
                    Queries.saveObject(ds, f)

                    // get the food ID into the FOOD_ID field of the NutritionData
                    val completedNdata = FkCompletion.completeForeignKeys(ds, listOf(nd), Schema.NutritionDataTable.FOOD_ID)

                    assert(completedNdata.size == 1) { "Completed nutrition data did not have size 1" }

                    Queries.saveObject(ds, completedNdata[0])
                    ds.endTransaction()
                    setStatus("Successfully saved food and nutrition data")
                } finally {
                    // TODO if exception is thrown here after an exception thrown
                    // in the above try block, the one here will hide the previous.
                    ds.closeConnection()
                }
            } catch (e: SQLException) {
                setStatus("Could not save!", "SQL Exception occurred (see bottom)")
                setExceptionMessage(e.localizedMessage)
            }

        } else {
            setStatus("Could not save due to validation errors. Check the following columns:",
                    "${foodBuilder.allErrors.keys} / ${nDataBuilder.allErrors.keys})")
        }
    }

    private fun quit() {
        finishedEditing = true
    }

    private fun saveIntoCurrentField(input: String) {
        // save and validate
        @Suppress("UNCHECKED_CAST")
        if (isNDataField(currentField)) {
            val col = columnForFieldIndex(currentField) as Column<NutritionData, *>
            acceptColumnInput(nDataBuilder, col, input)
        } else {
            val col = columnForFieldIndex(currentField) as Column<Food, *>
            acceptColumnInput(foodBuilder, col, input)
        }
    }

    private fun stepField(forward: Boolean, save: Boolean) {
        val displayedFields = foodColumnsForDisplay.size + ndColumnsForDisplay.size
        if (save) {
            // move to next field
            saveIntoCurrentField(editingValue.toString())
        }

        // advance current field
        if (forward) {
            if (++currentField >= displayedFields) {
                currentField = 0
            }
        } else {
            if (--currentField < 0) {
                currentField = displayedFields - 1
            }
        }
        setEditingValue(currentFieldData)
    }

    private enum class Action constructor(val niceName: String) {
        MORE_FIELDS("Extra fields"), //TODO
        SAVE("Save"),
        RESET("Reset"),
        EXIT("Exit"),
        HELP("Help");

        override fun toString(): String {
            return niceName
        }
    }
}
