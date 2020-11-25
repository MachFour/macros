package com.machfour.macros.core

import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.validation.Validation
import com.machfour.macros.validation.ValidationError
import java.util.Collections


typealias ErrorList = MutableList<ValidationError>

class MacrosBuilder<M : MacrosEntity<M>> private constructor(table: Table<M>, fromInstance: M?) {

    constructor(table: Table<M>): this(table, null)
    constructor(fromInstance: M): this(fromInstance.table, fromInstance)

    var finishedInit = false

    var editInstance: M? = fromInstance
        set(value) {
            field = value
            // if this setter is called during init {}, we can skip this call
            if (finishedInit) {
                resetFields()
            }
        }

    // columns that are available for editing
    private val settableColumns: MutableSet<Column<M, *>> = LinkedHashSet(table.columns)
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
    private val draftData: ColumnData<M> = editInstance?.dataFullCopy ?: ColumnData(table)

    private val validationErrors = HashMap<Column<M, *>, ErrorList>(table.columns.size, 1f).also {
        // init with empty lists
        for (col in table.columns) {
            it[col] = ArrayList()
        }
    }

    init {
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

    private fun copyFromEditInstance(editInstance: M) {
        ColumnData.copyData(editInstance.data, draftData, settableColumns)
    }

    // only resets settable fields
    fun resetFields() {
        when (val it = editInstance) {
            null -> draftData.setDefaultData(settableColumns)
            else -> copyFromEditInstance(it)
        }
        validateAll()
    }

    // Sets a field to the given value, unless it has been marked unsettable
    // In the latter case, this function will do nothing
    fun <J> setField(col: Column<M, J>, value: J?) {
        if (unsettableColumns.contains(col)) {
            // TODO throw exception?
            return
        }
        assert(settableColumns.contains(col))
        val oldValue: J? = get(col)
        draftData.put(col, value)

        // don't revalidate unless the value changed
        val isChangedValue = oldValue != value
        // but if there was a type mismatch, we also don't revalidate because it would erase the error
        val isTypeMismatch = getErrorsInternal(col).contains(ValidationError.TYPE_MISMATCH)
        if (isChangedValue && !isTypeMismatch) {
            validateSingle(col)
        }
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
            setField(col, castValue)
        } catch (e: TypeCastException) {
            // record the type mismatch and then set field to null
            getErrorsInternal(col).run {
                clear()
                add(ValidationError.TYPE_MISMATCH)
            }
            setField(col, null)
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

    //
    private fun <J> getErrorsInternal(field: Column<M, J>): ErrorList {
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
        return Collections.unmodifiableList(getErrorsInternal(field))
    }

    // TODO should it be set to null or default value? Or edit value?
    // ... i.e. do we really need this method, or just a 'reset to initial/default/original editable instance value'?
    fun clearField(field: Column<M, *>) {
        setField(field, null)
    }

    /* Saves a list of columns whose non-null constraints have been violated, or an empty list otherwise,
     * into the validationErrors map.
     */
    private fun <J> validateSingle(col: Column<M, J>) {
        assert(validationErrors.containsKey(col)) { "ValidationErrors not initialised properly" }
        validationErrors.getValue(col).let { errorList ->
            errorList.clear()
            errorList.addAll(validate(draftData, col, customValidations))
        }
    }

    // TODO this reruns the custom validation once for each column - inefficient
    private fun validateAll() {
        for (col in validationErrors.keys) {
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
        check(!hasAnyInvalidFields()) { "Field values are not all valid" }
        val buildData = draftData.copy()
        val source = newObjectSource()
        return objectFactory.construct(buildData, source)
    }

    fun hasAnyInvalidFields(): Boolean {
        return validationErrors.values.any { it.isNotEmpty() }
    }

    fun canBuild(): Boolean {
        return !hasAnyInvalidFields()
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
            data: ColumnData<M>,
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
            data: ColumnData<M>,
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