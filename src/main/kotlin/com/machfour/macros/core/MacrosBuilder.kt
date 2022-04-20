package com.machfour.macros.core

import com.machfour.macros.names.DisplayStrings
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.validation.MacrosValidator
import com.machfour.macros.validation.ValidationError
import com.machfour.macros.validation.tableValidator


class MacrosBuilder<M : MacrosEntity<M>> private constructor(
    private val table: Table<M>,
    private val validator: MacrosValidator<M> = tableValidator(table),
    fromInstance: M?
) {
    //fun interface ValueChangeListener<J> {
    //    fun onValueChanged(newValue: J?, errors: List<ValidationError>)
    //}

    constructor(
        table: Table<M>, validator: MacrosValidator<M> = tableValidator(table)
    ) : this(table, validator, null)

    constructor(
        fromInstance: M, validator: MacrosValidator<M> = tableValidator(fromInstance.table)
    ) : this(fromInstance.table, validator, fromInstance)

    var finishedInit = false

    var editInstance: M? = fromInstance
        set(value) {
            field = value
            if (value != null) {
                originalData = value.dataFullCopy()
            }
            // if this setter is called during init {}, we can skip this call
            if (finishedInit) {
                resetFields()
            }
        }

    // columns that are available for editing
    private val settableColumns: MutableSet<Column<M, *>> = LinkedHashSet()

    // columns that are not available for editing; initially empty
    private val unsettableColumns: MutableSet<Column<M, *>> = LinkedHashSet()

    /*
     * If editing, fields are initialised to the field values of editInstance.
     * editInstance is assumed to have been created by the Database, and thus *should*
     * (barring a change in validation rules) have all its entries valid.
     * If creating a new object, editInstance is null.
     */
    private var originalData: RowData<M> = editInstance?.dataFullCopy() ?: RowData(table)
    private val draftData: RowData<M> = editInstance?.dataFullCopy() ?: RowData(table)

    private val validationErrors =
        HashMap<Column<M, *>, MutableList<ValidationError>>(
            table.columns.size,
            1f
        ).also {
            // init with empty lists
            for (col in table.columns) {
                it[col] = ArrayList()
            }
        }

    // value change listeners
    //private val listeners: MutableMap<Column<M, *>, ValueChangeListener<*>> = HashMap()

    init {
        settableColumns.addAll(table.columns)

        // requires draftData to be initialised
        validateAll()
        finishedInit = true
    }

    // prevents setting value via setField or resetting value
    // it's up to the caller to ensure that the column has a valid value first!
    fun markFixed(col: Column<M, *>) {
        if (settableColumns.contains(col)) {
            settableColumns.remove(col)
            unsettableColumns.add(col)
        }
    }

    // only resets settable fields
    fun resetFields() {
        RowData.copyData(originalData, draftData, settableColumns)
        validateAll()
    }

    fun hasUnsavedData(): Boolean {
        return editInstance == null || originalData != draftData
    }

    //fun <J> setValueChangeListener(col: Column<M, J>, listener: ValueChangeListener<J>) {
    //    listeners[col] = listener
    //}

    //private fun <J> getListener(col: Column<M, J>): ValueChangeListener<J>? {
    //    return if (!listeners.containsKey(col)) null else listeners[col] as ValueChangeListener<J>
    //}

    //private fun <J> fireValueChangeListener(col: Column<M, J>) {
    //    val newValue = get(col)
    //    val errors = getErrorsInternal(col)
    //    getListener(col)?.onValueChanged(newValue, errors)
    //}

    // when setting null values, wasTypeMismatch differentiates between null as a value and null
    // as an error value during a setFromString
    private fun <J: Any> setFieldInternal(col: Column<M, J>, value: J?, wasTypeMismatch: Boolean) {
        if (unsettableColumns.contains(col)) {
            // TODO throw exception?
            return
        }
        assert(settableColumns.contains(col))
        val oldValue: J? = this[col]
        draftData.put(col, value)

        if (oldValue != value || wasTypeMismatch || hasErrors(col)) {
            validateSingle(col, wasTypeMismatch)
            //fireValueChangeListener(col)
        }
    }

    // Sets a field to the given value, unless it has been marked unsettable
    // In the latter case, this function will do nothing
    fun <J: Any> setField(col: Column<M, J>, value: J?) {
        // can't be a type mismatch because the value is already of the correct type
        setFieldInternal(col, value, wasTypeMismatch = false)
    }

    // empty strings treated as null
    fun <J: Any> setFieldFromString(col: Column<M, J>, value: String) {
        if (unsettableColumns.contains(col)) {
            // TODO throw exception?
            return
        }
        assert(settableColumns.contains(col))
        try {
            val castValue = col.type.fromRawString(value)
            setFieldInternal(col, castValue, wasTypeMismatch = false)
        } catch (e: TypeCastException) {
            setFieldInternal(col, null, wasTypeMismatch = true)
        }
    }

    operator fun <J: Any> get(col: Column<M, J>): J? {
        return draftData[col]
    }

    // null data represented as blank strings
    fun <J: Any> getAsString(col: Column<M, J>): String {
        return col.type.toString(this[col], "")
    }

    private fun <J: Any> getErrorsInternal(field: Column<M, J>): MutableList<ValidationError> {
        return checkNotNull(validationErrors[field]) { "Validation errors is missing entry for $field"}
    }

    fun <J: Any> hasNoErrors(field: Column<M, J>): Boolean = getErrorsInternal(field).isEmpty()
    fun <J: Any> hasErrors(field: Column<M, J>): Boolean = getErrorsInternal(field).isNotEmpty()

    /*
     * Returns an immutable list containing identifiers of each failing
     * ValidationError test for the given field,
     * or otherwise an empty list.
     */
    fun <J: Any> getErrors(field: Column<M, J>): List<ValidationError> {
        return getErrorsInternal(field)
    }

    // TODO should it be set to null or default value? Or edit value?
    // ... i.e. do we really need this method, or just a 'reset to initial/default/original editable instance value'?
    fun clearField(field: Column<M, *>) {
        setField(field, null)
    }

    /* Saves a list of columns whose non-null constraints have been violated, or an empty list otherwise,
     * into the validationErrors map.
     */
    private fun <J: Any> validateSingle(col: Column<M, J>, wasTypeMismatch: Boolean = false) {
        val errors = requireNotNull(validationErrors[col]) { "ValidationErrors not initialised properly" }
        with(errors) {
            clear()
            if (wasTypeMismatch) {
                add(ValidationError.TYPE_MISMATCH)
            } else {
                addAll(validator.validateSingle(draftData, col))
            }
        }
    }

    private fun validateAll() {
        // clear previous error values
        validationErrors.values.forEach { it.clear() }

        val newErrors = validator.validateData(draftData)

        // XXX note this will erase type mismatch errors
        for ((col, errors) in newErrors) {
            validationErrors[col] = errors.toMutableList()
        }
    }

    /*
     * Returns the subset of the validationErrors map with non-empty error lists
     */
    val allErrors: Map<Column<M, *>, List<ValidationError>>
        get() {
            return validationErrors.filter { it.value.isNotEmpty() }
        }

    // computes what the data source of the newly build object should be
    private fun newObjectSource(): ObjectSource {
        val editSource = when (val it = editInstance) {
            null -> ObjectSource.USER_NEW
            else -> it.source
        }
        return when (editSource) {
            // database objects are edited
            ObjectSource.DATABASE -> ObjectSource.DB_EDIT
            // don't allow creating new inbuilt objects
            ObjectSource.INBUILT -> ObjectSource.COMPUTED
            else -> editSource
        }
    }

    /*
     * If there are no validation errors, attempts to construct the object
     * using the current data. A copy of the data is used, so that this
     * Builder object's data may continue to be changed and other objects
     * created after a successful build.
     */
    fun build(overrideSource: ObjectSource? = null): M {
        check(!hasInvalidFields) { "Field values are not all valid (${invalidFields})" }
        val buildData = draftData.copy()
        val source = overrideSource ?: newObjectSource()
        return table.construct(buildData, source)
    }

    val invalidFields: List<Column<M, *>>
        get() = validationErrors.entries.filter { it.value.isNotEmpty() }.map { it.key }

    val hasInvalidFields: Boolean
        get() = validationErrors.values.any { it.isNotEmpty() }

    fun invalidFieldNames(displayStrings: DisplayStrings): List<String> {
        return invalidFields.map { displayStrings.getFullName(it) }
    }

    fun invalidFieldNamesString(displayStrings: DisplayStrings): String {
        return invalidFieldNames(displayStrings).joinToString(separator = " ")
    }

}