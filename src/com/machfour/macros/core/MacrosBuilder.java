package com.machfour.macros.core;

import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.validation.Validation;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MacrosBuilder<T extends MacrosPersistable<T>> {
    private final List<Column<?>> settableColumns;
    private final Table<T> table;

    private final T editInstance;

    /*
     * If editing, fields are initialised to the field values of editInstance.
     * editInstance is assumed to have been created by the Database, and thus *should*
     * (barring a change in com.machfour.macros.validation rules) have all its entries valid.
     * If creating a new object, editInstance is null.
     */
    private final ColumnData draftData;
    private final Map<Column<?>, Boolean> isValidValue;

    public MacrosBuilder(@NotNull Table<T> table) {
        this(table, null);
    }

    public MacrosBuilder(@NotNull T editInstance) {
        this(editInstance.getTable(), editInstance);
    }

    private MacrosBuilder(@NotNull Table<T> t, @Nullable T editInstance) {
        table = t;
        this.editInstance = editInstance;
        settableColumns = table.columns();
        draftData = new ColumnData<>(t);
        isValidValue = new HashMap<>(settableColumns.size(), 1);
        resetFields();
    }

    /*
     * Sets each currently defined field in settableColumns to the value given by
     * converting the instance to its stringValues representation.
     * An exception will be thrown if the instance does not contain the required keys
     */
    private void resetFields() {
        if (editInstance != null) {
            ColumnData instanceValues = editInstance.getAllData();
            for (Column<?> field : settableColumns) {
                setFieldUnchecked(field, instanceValues.unboxColumn(field));
            }
        } else {
            for (Column<?> field : settableColumns) {
                setFieldUnchecked(field, null);
            }
        }
    }

    /*
     * Sets the given column value to (a String representation of) the given value.
     * value cannot be null; an exception will be thrown if so
     */
    public <C> void setField(Column<C> col, C value) {
        assert (settableColumns.contains(col)) : "Invalid field set";
        draftData.putData(col, value);
        isValidValue.put(col, checkErrors(col).isEmpty());
    }

    /*
     * Sets the given field to (a String representation of) the given value.
     * value cannot be null; an exception will be thrown if so
     */
    private <C> void setFieldUnchecked(Column<?> col, C value) {
        assert (settableColumns.contains(col)) : "Invalid field set";
        Column<C> typedColumn = (Column<C>) col;
        draftData.putData(typedColumn, value);
        isValidValue.put(typedColumn, checkErrors(col).isEmpty());
    }

    /*
     * Validates the given field according to its currently set value.
     * Returns a list containing identifiers of each failing com.machfour.macros.validation test for the given field,
     * or otherwise an empty list.
     */
    public List<Validation> checkErrors(Column<?> field) {
        assert (settableColumns.contains(field)) : "Invalid field to validate";
        List<Validation> validationsToPerform = field.getValidations();
        List<Validation> failedValidations = new ArrayList<>(validationsToPerform.size());

        // TODO
        /*
        for (Validation v : validationsToPerform) {
            if (!v.validate(draftData, field)) {
                failedValidations.add(v);
            }
        }
        */
        return failedValidations;
    }

    public void clearField(Column<?> field) {
        setField(field, null);
    }

    /*
     * Returns a mapping from each fields whose value failed one or more com.machfour.macros.validation tests, to a list
     * of those failing tests.
     */
    public Map<Column, List<Validation>> findAllErrors() {
        Map<Column, List<Validation>> allValidationErrors = new HashMap<>(settableColumns.size(), 1);

        for (Column field : settableColumns) {
            List<Validation> fieldErrors = checkErrors(field);
            if (!fieldErrors.isEmpty()) {
                allValidationErrors.put(field, fieldErrors);
            }

        }
        return allValidationErrors;
    }

    public T build() {
        if (!canConstruct()) {
            throw new IllegalStateException("Field values are not all valid");
        }
        return null; // TODO buildFromStringValues(stringValues, editInstance);
    }

    public boolean canConstruct() {
        boolean canConstruct = true;
        for (Column s : settableColumns) {
            if (!isValidValue.get(s)) {
                canConstruct = false;
            }
        }
        return canConstruct;
    }

}

