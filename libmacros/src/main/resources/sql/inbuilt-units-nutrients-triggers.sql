-- Prevent deleting inbuilt units and nutrients

CREATE TRIGGER prevent_delete_inbuilt_units
    BEFORE DELETE ON Unit
    WHEN (
        OLD.inbuilt = 1
    )
    BEGIN
        SELECT RAISE (ABORT, 'Cannot delete inbuilt units');
    END;

CREATE TRIGGER prevent_delete_inbuilt_nutrients
    BEFORE DELETE ON Nutrient
    WHEN (
        OLD.inbuilt = 1
    )
    BEGIN
        SELECT RAISE (ABORT, 'Cannot delete inbuilt nutrients');
    END;

-- vim: ts=4 et
