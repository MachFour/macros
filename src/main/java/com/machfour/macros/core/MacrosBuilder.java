package com.machfour.macros.core;

import com.machfour.macros.core.datatype.TypeCastException;
import com.machfour.macros.util.MiscUtils;
import com.machfour.macros.validation.ValidationError;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MacrosBuilder<M extends MacrosEntity<M>> {
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
    private final Map<Column<M, ?>, List<ValidationError>> validationErrors;

    public MacrosBuilder(@NotNull Table<M> table) {
        this(table, null);
    }

    public MacrosBuilder(@NotNull M editInstance) {
        this(editInstance.getTable(), editInstance);
    }

    private MacrosBuilder(@NotNull Table<M> t, @Nullable M editInstance) {
        this.table = t;
        this.objectFactory = t.getFactory();
        this.editInstance = editInstance;
        this.settableColumns = table.columns();
        this.validationErrors = new HashMap<>(settableColumns.size(), 1);
        initValidationErrorsMap();

        if (editInstance != null) {
            this.draftData = editInstance.getAllData().copy();
        } else {
            this.draftData = new ColumnData<>(t);
        }
        validateAll();
    }

    private void initValidationErrorsMap() {
        // init with empty lists
        for (Column<M, ?> col : settableColumns) {
            validationErrors.put(col, new ArrayList<>());
        }
    }

    public void resetFields() {
        if (editInstance != null) {
            ColumnData.copyData(editInstance.getAllData(), draftData, settableColumns);
        } else {
            draftData.setDefaultData(settableColumns);
        }
        validateAll();
    }


    public <J> void setField(Column<M, J> col, @Nullable J value) {
        J oldValue = getField(col);
        draftData.put(col, value);
        // validation
        if (!MiscUtils.objectsEquals(oldValue, value)) {
            validateSingle(col);
        }

    }

    // empty strings treated as null
    public <J> void setFieldFromString(Column<M, J> col, @NotNull String value) {
        try {
            J castValue = col.getType().fromString(value);
            setField(col, castValue);
        } catch (TypeCastException e) {
            handleTypeCastError(col);
        }
    }

    @Nullable
    public <J> J getField(Column<M, J> col) {
        return draftData.get(col);
    }

    @NotNull
    // null data represented as blank strings
    public <J> String getFieldAsString(Column<M, J> col) {
        J data = getField(col);
        return data == null ? "" : data.toString();
    }

    /*
     * Returns a list containing identifiers of each failing com.machfour.macros.validation test for the given field,
     * or otherwise an empty list.
     */
    public <J> List<ValidationError> getErrors(Column<M, J> field) {
        return validationErrors.get(field);
    }

    // TODO should it be set to null or default value? Or edit value?
    // ... i.e. do we really need this method, or just a 'reset to initial/default/original editable instance value'?
    public void clearField(Column<M, ?> field) {
        setField(field, null);
    }

    private <J> void handleTypeCastError(Column<M, J> col) {
        assert validationErrors.containsKey(col) : "ValidationErrors not initialised properly";
        List<ValidationError> errorList = validationErrors.get(col);
        errorList.clear();
        errorList.add(ValidationError.TYPE_MISMATCH);
    }

    /*
       Checks that:
       * Non null constraints as defined by the columns are upheld
       If any violations are found, the affected column as well as an enum value describing the violation are recorded
       in a map, which is returned at the end, after all columns have been processed.
     */
    // method used by MacrosEntity
    static <M extends MacrosEntity<M>, J> List<ValidationError> validate(ColumnData<M> data, Column<M, J> col) {
        List<ValidationError> errorList = new ArrayList<>();
        if (data.get(col) == null && !col.isNullable() && col.defaultData() == null) {
            errorList.add(ValidationError.NON_NULL);
        }
        // TODO add custom valiations
        //List<Validation> validationsToPerform = field.getValidations();
        /*
        for (Validation v : validationsToPerform) {
            if (!v.validate(draftData, field)) {
                failedValidations.add(v);
            }
        }
        */
        return errorList;

    }

    // method used by MacrosEntity
    // returns a map of ONLY the columns with errors, mapping to list of validation errors
    static <M extends MacrosEntity<M>, J> Map<Column<M, ?>, List<ValidationError>> validate(ColumnData<M> data) {
        Map<Column<M, ?>, List<ValidationError>> allErrors = new HashMap<>(data.getColumns().size(), 1.0f);
        for (Column<M, ?> col : data.getColumns()) {
            List<ValidationError> colErrors = validate(data, col);
            if (!colErrors.isEmpty()) {
                allErrors.put(col, colErrors);
            }
        }
        return allErrors;
    }

    /* Saves a list of columns whose non-null constraints have been violated, or an empty list otherwise,
     * into the validationErrors map.
     */
    private <J> void validateSingle(Column<M, J> col) {
        assert validationErrors.containsKey(col) : "ValidationErrors not initialised properly";
        List<ValidationError> errorList = validationErrors.get(col);
        errorList.clear();
        errorList.addAll(validate(draftData, col));
    }

    private void validateAll() {
        for (Column<M, ?> col : settableColumns) {
            validateSingle(col);
        }
    }

    /*
     * Returns the subset of the validationErrors map with non-empty error lists
     */
    public Map<Column<M, ?>, List<ValidationError>> getAllErrors() {
        Map<Column<M, ?>, List<ValidationError>> allValidationErrors = new LinkedHashMap<>(settableColumns.size(), 1);

        for (Column<M, ?> field : settableColumns) {
            List<ValidationError> fieldErrors = getErrors(field);
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
        if (hasAnyInvalidFields()) {
            throw new IllegalStateException("Field values are not all valid");
        }
        ColumnData<M> buildData = draftData.copy();
        ObjectSource source = newObjectSource();

        return objectFactory.construct(buildData, source);
    }

    public boolean hasAnyInvalidFields() {
        boolean anyInvalid = false;
        for (Column<M, ?> col : settableColumns) {
            assert validationErrors.containsKey(col) : "ValidationErrors not initialised properly";
            if (!validationErrors.get(col).isEmpty()) {
                anyInvalid = true;
                break;
            }
        }
        return anyInvalid;
    }

}

