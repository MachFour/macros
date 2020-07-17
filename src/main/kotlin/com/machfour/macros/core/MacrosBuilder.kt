package com.machfour.macros.core

import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.validation.ValidationError
import java.util.Collections;

class MacrosBuilder<M : MacrosEntity<M>> private constructor(
        private val table: Table<M>,
        private val editInstance: M? = null) {

    constructor(table: Table<M>) : this(table, null)
    constructor(editInstance: M) : this(editInstance.table, editInstance)

    companion object {
        /*
           Checks that:
           - Non null constraints as defined by the columns are upheld
           - If any violations are found, the affected column as well as an enum value describing the violation are recorded
             in a map, which is returned at the end, after all columns have been processed.
         */
        // method used by MacrosEntity
        fun <M : MacrosEntity<M>, J> validate(data: ColumnData<M>, col: Column<M, J>): List<ValidationError> {
            val errorList: MutableList<ValidationError> = ArrayList()
            if (data[col] == null && !col.isNullable /*&& col.defaultData() == null*/) {
                errorList.add(ValidationError.NON_NULL)
            }
            // TODO add custom validations
            // TODO add check for unique: needs DB access
            //List<Validation> validationsToPerform = field.getValidations();
            /*
            for (Validation v : validationsToPerform) {
                if (!v.validate(draftData, field)) {
                    failedValidations.add(v);
                }
            }
            */
            return errorList
        }

        // method used by MacrosEntity
        // returns a map of ONLY the columns with errors, mapping to list of validation errors
        fun <M : MacrosEntity<M>> validate(data: ColumnData<M>): Map<Column<M, *>, List<ValidationError>> {
            val allErrors: MutableMap<Column<M, *>, List<ValidationError>> = HashMap(data.columns.size, 1.0f)
            for (col in data.columns) {
                val colErrors = validate(data, col)
                if (colErrors.isNotEmpty()) {
                    allErrors[col] = colErrors
                }
            }
            return allErrors
        }
    }

    // columns that are available for editing
    private val settableColumns: MutableSet<Column<M, *>> = LinkedHashSet(table.columns)

    // columns that are not available for editing; initially empty
    private val unsettableColumns: MutableSet<Column<M, *>> = LinkedHashSet()

    // invariant: union of settableColumns and unsettableColumns is table.columns()
    private val objectFactory: Factory<M> = table.factory

    /*
     * If editing, fields are initialised to the field values of editInstance.
     * editInstance is assumed to have been created by the Database, and thus *should*
     * (barring a change in com.machfour.macros.validation rules) have all its entries valid.
     * If creating a new object, editInstance is null.
     */
    private var draftData: ColumnData<M> = editInstance?.allData?.copy() ?: ColumnData(table)
    private val validationErrors: MutableMap<Column<M, *>, MutableList<ValidationError>> = HashMap(table.columns.size, 1.0f)


    init {
        // init with empty lists
        for (col in table.columns) {
            validationErrors[col] = ArrayList()
        }
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
        if (editInstance != null) {
            ColumnData.copyData(editInstance.allData, draftData, settableColumns)
        } else {
            draftData.setDefaultData(settableColumns)
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
        val oldValue: J? = getField(col)
        draftData.put(col, value)
        // validation
        val isChangedValue = oldValue != value
        // if there was a type cast exception then there was an attempt to change the value
        // which failed ... so it's like a pseudo-changed value
        val wasTypeCastException = getErrorsInternal(col).contains(ValidationError.TYPE_MISMATCH)
        if (isChangedValue || wasTypeCastException) {
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
            // TODO this sets an error for the field... but technically its actual value wasn't changed.
            // but I guess it's okay because the user should set the value once more (correctly) anyway
            val errorList = getErrorsInternal(col)
            errorList.clear()
            errorList.add(ValidationError.TYPE_MISMATCH)
        }
    }

    fun <J> getField(col: Column<M, J>): J? {
        return draftData[col]
    }

    // null data represented as blank strings
    fun <J> getFieldAsString(col: Column<M, J>): String {
        val data = getField(col)
        return data?.toString() ?: ""
    }

    //
    private fun <J> getErrorsInternal(field: Column<M, J>): MutableList<ValidationError> {
        return validationErrors.getValue(field)
    }

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
        val errorList = validationErrors.getValue(col)
        errorList.clear()
        errorList.addAll(validate(draftData, col))
    }

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
        if (editInstance == null) {
            return ObjectSource.USER_NEW
        }
        return when (val editedObjectSource = editInstance.objectSource) {
            ObjectSource.DATABASE, ObjectSource.DB_EDIT -> ObjectSource.DB_EDIT
            // don't allow creating new inbuilt objects
            ObjectSource.INBUILT -> ObjectSource.COMPUTED
            else -> editedObjectSource
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

}