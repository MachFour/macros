package com.machfour.macros.core

import com.machfour.macros.names.ColumnStrings
import com.machfour.macros.orm.Factory
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.validation.Validation
import com.machfour.macros.validation.ValidationError


class MacrosBuilder<M : MacrosEntity<M>> private constructor(table: Table<M>, fromInstance: M?) {
    //fun interface ValueChangeListener<J> {
    //    fun onValueChanged(newValue: J?, errors: List<ValidationError>)
    //}

    constructor(table: Table<M>) : this(table, null)
    constructor(fromInstance: M) : this(fromInstance.table, fromInstance)

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

    // invariant: settableColumns ⋃ unsettableColumns == table.columns()
    private val objectFactory: Factory<M> = table.factory

    private val customValidations: MutableList<Validation<M>> = ArrayList()

    /*
     * If editing, fields are initialised to the field values of editInstance.
     * editInstance is assumed to have been created by the Database, and thus *should*
     * (barring a change in com.machfour.macros.validation rules) have all its entries valid.
     * If creating a new object, editInstance is null.
     */
    private var originalData: RowData<M> = editInstance?.dataFullCopy() ?: RowData(table)
    private val draftData: RowData<M> = editInstance?.dataFullCopy() ?: RowData(table)

    private val validationErrors = HashMap<Column<M, *>, MVErrorList>(table.columns.size, 1f).also {
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

    fun addValidation(v: Validation<M>) {
        customValidations += v
        validateAll()
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
    private fun <J> setFieldInternal(col: Column<M, J>, value: J?, wasTypeMismatch: Boolean) {
        if (unsettableColumns.contains(col)) {
            // TODO throw exception?
            return
        }
        assert(settableColumns.contains(col))
        val oldValue: J? = get(col)
        draftData.put(col, value)

        if (oldValue != value || wasTypeMismatch || hasErrors(col)) {
            validateSingle(col, wasTypeMismatch)
            //fireValueChangeListener(col)
        }
    }

    // Sets a field to the given value, unless it has been marked unsettable
    // In the latter case, this function will do nothing
    fun <J> setField(col: Column<M, J>, value: J?) {
        // can't be a type mismatch because the value is already of the correct type
        setFieldInternal(col, value, wasTypeMismatch = false)
    }

    // empty strings treated as null
    fun <J> setFieldFromString(col: Column<M, J>, value: String) {
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

    fun <J> get(col: Column<M, J>): J? {
        return draftData[col]
    }

    // null data represented as blank strings
    fun <J> getAsString(col: Column<M, J>): String {
        val data = get(col)
        return data?.toString() ?: ""
    }

    private fun <J> getErrorsInternal(field: Column<M, J>): MVErrorList {
        return validationErrors.getValue(field)
    }

    fun <J> hasNoErrors(field: Column<M, J>): Boolean = getErrorsInternal(field).isEmpty()
    fun <J> hasErrors(field: Column<M, J>): Boolean = getErrorsInternal(field).isNotEmpty()

    /*
     * Returns an immutable list containing identifiers of each failing
     * com.machfour.macros.validation.ValidationError test for the given field,
     * or otherwise an empty list.
     */
    fun <J> getErrors(field: Column<M, J>): List<ValidationError> {
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
    private fun <J> validateSingle(col: Column<M, J>, wasTypeMismatch: Boolean = false) {
        assert(validationErrors.containsKey(col)) { "ValidationErrors not initialised properly" }
        validationErrors.getValue(col).let {
            it.clear()
            if (wasTypeMismatch) {
                it.add(ValidationError.TYPE_MISMATCH)
            } else {
                val errors = validate(draftData, col, customValidations)
                it.addAll(errors)
            }

        }
    }

    // TODO this reruns the custom validation once for each column - inefficient
    private fun validateAll() {
        for (col in validationErrors.keys) {
            // XXX note this will erase type mismatch errors
            validateSingle(col)
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
            else -> it.objectSource
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
    fun build(): M {
        check(!hasAnyInvalidFields) { "Field values are not all valid (${invalidFields})" }
        val buildData = draftData.copy()
        val source = newObjectSource()
        return objectFactory.construct(buildData, source)
    }

    val invalidFields: List<Column<M, *>>
        get() = validationErrors.entries.filter { it.value.isNotEmpty() }.map { it.key }

    fun invalidFieldNames(colStrings: ColumnStrings): List<String> {
        return invalidFields.map { colStrings.getFullName(it) }
    }

    fun invalidFieldNamesString(colStrings: ColumnStrings): String {
        return invalidFieldNames(colStrings).toString()
    }

    val hasAnyInvalidFields: Boolean
        get() = validationErrors.values.any { it.isNotEmpty() }

    fun canBuild(): Boolean {
        return !hasAnyInvalidFields
    }

    companion object {
        /*
           Checks that:
           - Non null constraints as defined by the columns are upheld
           - If any violations are found, the affected column as well as an enum value describing the violation are recorded
             in a map, which is returned at the end, after all columns have been processed.
         */
        // method used by MacrosEntity
        fun <M : MacrosEntity<M>, J> validate(
            data: RowData<M>,
            col: Column<M, J>,
            customValidations: List<Validation<M>> = emptyList()
        ): List<ValidationError> {
            val errorList: MutableList<ValidationError> = ArrayList()
            if (data[col] == null && !col.isNullable /*&& col.defaultData() == null*/) {
                errorList.add(ValidationError.NON_NULL)
            }
            // TODO add check for unique: needs DB access
            customValidations
                .map { it.validate(data) }
                .filter { it.containsKey(col) }
                .forEach { errorList.addAll(it.getValue(col)) }

            return errorList
        }

        // method used by MacrosEntity
        // returns a map of ONLY the columns with errors, mapping to list of validation errors
        fun <M : MacrosEntity<M>> validate(
            data: RowData<M>,
            customValidation: Validation<M>? = null
        ): Map<Column<M, *>, List<ValidationError>> {
            val allErrors: MutableMap<Column<M, *>, List<ValidationError>> = HashMap(data.columns.size, 1.0f)
            for (col in data.columns) {
                val colErrors = validate(data, col)
                if (colErrors.isNotEmpty()) {
                    allErrors[col] = colErrors
                }
            }
            if (customValidation != null) {
                allErrors.putAll(customValidation.validate(data))
            }
            return allErrors
        }

    }

}