package com.machfour.macros.cli.interactive

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.machfour.macros.cli.utils.displayLength
import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.formatting.fmt
import com.machfour.macros.names.DefaultDisplayStrings
import com.machfour.macros.names.DisplayStrings
import com.machfour.macros.nutrients.AllNutrients
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.NumNutrients
import com.machfour.macros.queries.saveObject
import com.machfour.macros.queries.saveObjects
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.units.LegacyNutrientUnits
import com.machfour.macros.validation.ValidationError
import java.io.IOException

// Layout parameters
private const val actionPadding = 18
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

// TODO servings?
class FoodEditor(
    //private static final int errorMsgStartCol = fieldValueStartCol + fieldValueWidth + 1;
    private val db: SqlDatabase,
    private val foodBuilder: MacrosBuilder<Food>,
    private val nutrientBuilder: MacrosBuilder<FoodNutrientValue> = MacrosBuilder(FoodNutrientValueTable),
    private val displayStrings: DisplayStrings = DefaultDisplayStrings,
    private val screen: Screen = defaultScreen()) {

    // stores built Nutrient Data objects
    private val nutrientData = FoodNutrientData()

    private val editingValue: StringBuilder

    // maps column Indices to terminal rows
    private val terminalRowForColumnIndex: MutableList<Int>
    private val errorMessageForColumnIndex: MutableList<String?>

    private val foodColumnsForDisplay: MutableList<Column<Food, *>>
    private val nutrientsForDisplay: MutableList<Nutrient>
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
            return foodErrors.isEmpty()
        }

    // save and validate
    private val currentFieldData: String
        @Suppress("UNCHECKED_CAST")
        get() {
            return if (isNDataField(currentField)) {
                val nutrient = getColumnOrNutrientWithIndex(currentField) as Nutrient
                nutrientData[nutrient].toString()
            } else {
                val col = getColumnOrNutrientWithIndex(currentField) as Column<Food, *>
                foodBuilder.getAsString(col)
            }
        }

    init {
        val numFoodColumns = FoodTable.columns.size
        val numNdColumns = NumNutrients
        val totalColumns = numFoodColumns + numNdColumns

        this.foodColumnsForDisplay = ArrayList(numFoodColumns)
        this.nutrientsForDisplay = ArrayList(numNdColumns)
        this.terminalRowForColumnIndex = ArrayList(totalColumns)
        this.errorMessageForColumnIndex = ArrayList(totalColumns)

        this.currentAction = Action.SAVE
        this.statusLine1 = "Welcome to the food editor."
        this.statusLine2 =
            "Use the arrow keys to navigate, and enter to confirm an edit or select an action"
        this.exceptionMessage = ""
        this.editingValue = StringBuilder()

        initVariables()
    }

    private fun stepAction(forward: Boolean) {
        var nextActionOrdinal = currentAction.ordinal
        if (forward) {
            if (++nextActionOrdinal >= Action.entries.size) {
                nextActionOrdinal -= Action.entries.size
            }
        } else {
            if (--nextActionOrdinal < 0) {
                nextActionOrdinal += Action.entries.size
            }
        }
        currentAction = Action.entries[nextActionOrdinal]
    }

    private fun <M : MacrosEntityImpl<M>, J: Any> getErrorMessage(b: MacrosBuilder<M>, col: Column<M, J>): String? {
        val errors = b.getErrors(col)
        return if (errors.isNotEmpty()) {
            // just display first error
            when (errors[0]) {
                ValidationError.NON_NULL -> " ** Cannot be empty"
                ValidationError.TYPE_MISMATCH -> " ** Must be of type '" + col.type + "'"
            }
        } else {
            null
        }
    }


    private fun isNDataField(fieldIndex: Int): Boolean {
        return fieldIndex >= foodColumnsForDisplay.size
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
            stepField(forward = true, save = true)
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
        nutrientData.clear()
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
        for (col in FoodTable.columns) {
            if (col.isUserEditable) {
                foodColumnsForDisplay.add(col)
            }
        }
        for (nutrient in AllNutrients) {
            // TODO display minimal set of nutrient columns first
            nutrientsForDisplay.add(nutrient)
        }

        val totalCols = foodColumnsForDisplay.size + nutrientsForDisplay.size
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
            var wideChars = s.displayLength()
            var displayLength = s.length + wideChars
            if (displayLength > width) {
                // string has to be truncated: subtract (displayLength - width) chars
                val maxIndex = s.length - (displayLength - width) // == width - wideChars
                s = s.substring(0, maxIndex)
                // recount the wide characters and display length
                wideChars = s.displayLength()
                displayLength = s.length + wideChars
            }
            check(displayLength <= width)
            return s.fmt(width - wideChars, !rightAlign)
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
        print(o, 0, rightAlign = false, newline = true)
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

    private fun getColumnOrNutrientWithIndex(index: Int): Any {
        val numDisplayedFoodCols = foodColumnsForDisplay.size
        return if (index < numDisplayedFoodCols) {
            foodColumnsForDisplay[index]
        } else {
            nutrientsForDisplay[index - numDisplayedFoodCols]
        }
    }


    @Throws(IOException::class)
    private fun printField(fieldName: String, fieldValue: String, columnIndex: Int) {
        print("|")
        print(fieldName, columnNameWidth, true)
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
            print(fieldValue, fieldValueWidth, false)
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
    private fun <M : MacrosEntityImpl<M>> printColumns(columns: Collection<Column<M, *>>, builder: MacrosBuilder<M>, startCol: Int): Int {
        var columnIndex = startCol
        for (col in columns) {
            terminalRowForColumnIndex[columnIndex] = terminalRow
            printField(displayStrings.getFullName(col), builder.getAsString(col), columnIndex)
            columnIndex++
        }
        return columnIndex // final column index
    }
    @Throws(IOException::class)
    private fun printNutrients(nutrients: Collection<Nutrient>, initialColumnIndex: Int): Int {
        var columnIndex = initialColumnIndex
        for (n in nutrients) {
            terminalRowForColumnIndex[columnIndex] = terminalRow
            printField(displayStrings.getFullName(n), nutrientData[n].toString(), columnIndex)
            columnIndex++
        }
        return columnIndex // final column index
    }


    @Throws(IOException::class)
    private fun printActionRow() {
        println("Actions: ", actionPadding, false)
        for (a in Action.entries) {
            val indicator = if (!isEditing && a == currentAction) " > " else "   "
            print(" $indicator ${a.niceName}", actionPadding, true)
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
        printNutrients(nutrientsForDisplay, numFoodColumns)

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
            try {
                try {
                    // gotta do it in one go
                    db.openConnection()
                    db.beginTransaction()

                    val id = saveObject(db, f)

                    // add ID to nutrient values
                    val completedNutrientValues = nutrientData.values.map {
                        it.dataFullCopy().run {
                            put(FoodNutrientValueTable.FOOD_ID, id)
                            FoodNutrientValue.factory.construct(this, ObjectSource.USER_NEW)
                        }
                    }

                    saveObjects(db, completedNutrientValues, ObjectSource.USER_NEW)
                    db.endTransaction()
                    setStatus("Successfully saved food and nutrition data")
                } catch (e: SqlException) {
                    db.rollbackTransaction()
                    throw e
                } finally {
                    // TODO if exception is thrown here after an exception thrown
                    // in the above try block, the one here will hide the previous.
                    db.closeConnection()
                }
            } catch (e: SqlException) {
                setStatus("Could not save!", "SQL Exception occurred (see bottom)")
                setExceptionMessage(e.localizedMessage)
            }

        } else {
            setStatus(
                line1 = "Could not save due to validation errors. Check the following columns:",
                line2 = "${foodBuilder.allErrors.keys} / ${nutrientBuilder.allErrors.keys})"
            )
        }
    }

    private fun quit() {
        finishedEditing = true
    }

    private fun saveIntoCurrentField(input: String) {
        // save and validate
        @Suppress("UNCHECKED_CAST")
        if (isNDataField(currentField)) {
            val nutrient = getColumnOrNutrientWithIndex(currentField) as Nutrient
            // try to build and save into NutrientData
            nutrientBuilder.run {
                resetFields()
                setField(FoodNutrientValueTable.UNIT_ID, LegacyNutrientUnits[nutrient].id)
                setField(FoodNutrientValueTable.NUTRIENT_ID, nutrient.id)
                setFieldFromString(FoodNutrientValueTable.VALUE, input)
                if (!hasInvalidFields) {
                    nutrientData[nutrient] = build()
                }
                errorMessageForColumnIndex[currentField] = getErrorMessage(this, FoodNutrientValueTable.VALUE)
            }
        } else {
            val col = getColumnOrNutrientWithIndex(currentField) as Column<Food, *>
            foodBuilder.setFieldFromString(col, input)
            errorMessageForColumnIndex[currentField] = getErrorMessage(foodBuilder, col)
        }
    }

    private fun stepField(forward: Boolean, save: Boolean) {
        val displayedFields = foodColumnsForDisplay.size + nutrientsForDisplay.size
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
}
