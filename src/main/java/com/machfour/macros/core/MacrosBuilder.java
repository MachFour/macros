package com.machfour.macros.core;

import com.machfour.macros.validation.Validation;
import com.machfour.macros.validation.ValidationError;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MacrosBuilder<M extends MacrosPersistable<M>> {
    private final List<Column<M, ?>> settableColumns;
    private final Table<M> table;
    private final Factory<M> objectFactory;

    private final M editInstance;

    /*
     * If editing, fields are initialised to the field values of editInstance.
     * editInstance is assumed to have been created by the Database, and thus *should*
     * (barring a change in com.machfour.macros.validation rules) have all its entries valid.
     * If creating a new object, editInstance is null.
     */
    private final ColumnData<M> draftData;
    private final Map<Column<M, ?>, Boolean> isValidValue;

    public MacrosBuilder(@NotNull Table<M> table) {
        this(table, null);
    }

    public MacrosBuilder(@NotNull M editInstance) {
        this(editInstance.getTable(), editInstance);
    }

    private MacrosBuilder(@NotNull Table<M> t, @Nullable M editInstance) {
        table = t;
        objectFactory = t.getFactory();
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
            ColumnData.copyData(editInstance.getAllData(), draftData, settableColumns);
        }
        for (Column<M, ?> field : settableColumns) {
            isValidValue.put(field, validateSingle(field).isEmpty());
        }
    }

    /*
     * Sets the given column value to (a String representation of) the given value.
     * value cannot be null; an exception will be thrown if so
     */
    public <J> void setField(Column<M, J> col, J value) {
        draftData.put(col, value);
        isValidValue.put(col, validateSingle(col).isEmpty());
    }

    /*
     * Validates the given field according to its currently set value.
     * Returns a list containing identifiers of each failing com.machfour.macros.validation test for the given field,
     * or otherwise an empty list.
     */
    public <J> List<ValidationError> validateSingle(Column<M, J> field) {
        List<ValidationError> failedValidations = findErrors(draftData, field);

        // TODO add custom valiations
        //List<Validation> validationsToPerform = field.getValidations();
        /*
        for (Validation v : validationsToPerform) {
            if (!v.validate(draftData, field)) {
                failedValidations.add(v);
            }
        }
        */
        return failedValidations;
    }

    public void clearField(Column<M, ?> field) {
        setField(field, null);
    }


    /*
       Checks that:
       * Non null constraints as defined by the columns are upheld
       If any violations are found, the affected column as well as an enum value describing the violation are recorded
       in a map, which is returned at the end, after all columns have been processed.
       Returns a list of columns whose non-null constraints have been violated, or an empty list otherwise
       Note that if the assertion passes, then dataMap has the correct columns as keys
     */
    private static <M, J> List<ValidationError> findErrors(ColumnData<M> data, Column<M, J> col) {
        List<ValidationError> errors = new ArrayList<>();
        if (data.get(col) == null && !col.isNullable()) {
            errors.add(ValidationError.NON_NULL);
        }
        return errors;
    }

    public static <M> Map<Column<M, ?>, ValidationError> validate(ColumnData<M> data) {
        List<Column<M, ?>> required = data.getTable().columns();
        // TODO should this be a list
        Map<Column<M, ?>, ValidationError> badMappings = new HashMap<>(required.size());
        for (Column<M, ?> col : required) {
            List<ValidationError> errors = findErrors(data, col);
            if (!errors.isEmpty()) {
                // just add first errors
                badMappings.put(col, errors.get(0));
            }
        }
        return badMappings;
    }
    /*
     * Returns a mapping from each fields whose value failed one or more com.machfour.macros.validation tests, to a list
     * of those failing tests.
     */
    public Map<Column<M, ?>, List<ValidationError>> findAllErrors() {
        Map<Column<M, ?>, List<ValidationError>> allValidationErrors = new HashMap<>(settableColumns.size(), 1);

        for (Column<M, ?> field : settableColumns) {
            List<ValidationError> fieldErrors = validateSingle(field);
            if (!fieldErrors.isEmpty()) {
                allValidationErrors.put(field, fieldErrors);
            }

        }
        return allValidationErrors;
    }

    public M build() {
        if (!canConstruct()) {
            throw new IllegalStateException("Field values are not all valid");
        }
        return objectFactory.construct(draftData, ObjectSource.USER_NEW);
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

