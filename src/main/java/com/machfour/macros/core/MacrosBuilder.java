package com.machfour.macros.core;

import com.machfour.macros.validation.Validation;
import com.machfour.macros.validation.ValidationError;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
        isValidValue = new HashMap<>(settableColumns.size(), 1);
        if (editInstance != null) {
            draftData = editInstance.getAllData().copy();
        } else {
            draftData = new ColumnData<>(t);
        }
        recheckValidValues();
    }

    private void resetFields() {
        if (editInstance != null) {
            ColumnData.copyData(editInstance.getAllData(), draftData, settableColumns);
        } else {
            draftData.setDefaultData(settableColumns);
        }
        recheckValidValues();
    }

    public <J> void setField(Column<M, J> col, J value) {
        draftData.put(col, value);
        recheckValidValues(Collections.singleton(col));
    }

    @Nullable
    public <J> J getField(Column<M, J> col) {
        return draftData.get(col);
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

    private void recheckValidValues(Collection<Column<M, ?>> columns) {
        for (Column<M, ?> field : columns) {
            isValidValue.put(field, validateSingle(field).isEmpty());
        }
    }

    private void recheckValidValues() {
        recheckValidValues(settableColumns);

    }

    // TODO should it be set to null or default value? Or edit value?
    // ... i.e. do we really need this method, or just a 'reset to initial/default/original editable instance value'?
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
    @NotNull
    private static <M, J> List<ValidationError> findErrors(ColumnData<M> data, Column<M, J> col) {
        List<ValidationError> errors = new ArrayList<>();
        if (data.get(col) == null && !col.isNullable() && col.defaultData() == null) {
            errors.add(ValidationError.NON_NULL);
        }
        return errors;
    }

    public static <M> Map<Column<M, ?>, List<ValidationError>> validate(ColumnData<M> data) {
        List<Column<M, ?>> required = data.getTable().columns();
        // TODO should this be a list
        Map<Column<M, ?>, List<ValidationError>> badMappings = new HashMap<>(required.size());
        for (Column<M, ?> col : required) {
            List<ValidationError> errors = findErrors(data, col);
            if (!errors.isEmpty()) {
                badMappings.put(col, errors);
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

    // computes what the data source of the newly build object should be
    private ObjectSource newObjectSource() {
        if (editInstance == null) {
            return ObjectSource.USER_NEW;
        }
        ObjectSource editedObjectSource = editInstance.getObjectSource();

        switch (editedObjectSource) {
            case DATABASE:
            case DB_EDIT:
                return ObjectSource.DB_EDIT;
            case INBUILT:
                // don't allow creating new inbuilt objects
                return ObjectSource.COMPUTED;
            default:
                return editedObjectSource;
        }

    }

    /*
     * If there are no validation errors, attempts to construct the object
     * using the current data. A copy of the data is used, so that this
     * Builder object's data may continue to be changed and other objects
     * created after a successful build.
     */
    public M build() {
        if (!canConstruct()) {
            throw new IllegalStateException("Field values are not all valid");
        }
        ColumnData<M> buildData = draftData.copy();
        ObjectSource source = newObjectSource();

        return objectFactory.construct(buildData, source);
    }

    public boolean canConstruct() {
        boolean canConstruct = true;
        for (Column<M, ?> s : settableColumns) {
            if (!isValidValue.get(s)) {
                canConstruct = false;
            }
        }
        return canConstruct;
    }

}

