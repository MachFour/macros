
CREATE TRIGGER check_valid_food_nutrient_value_units
    BEFORE INSERT ON FoodNutrientValue
    WHEN (
        -- select all possible units for the new unit type
        NEW.unit_id NOT IN (
            SELECT u.id FROM Nutrient AS n INNER JOIN Unit AS u 
                ON (n.unit_types | u.type_id != 0)
                WHERE n.id = NEW.nutrient_id
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Unit has incorrect type for FoodNutrientValue');
    END;

CREATE TRIGGER check_valid_food_nutrient_value_units_2
    BEFORE UPDATE ON FoodNutrientValue
    WHEN (
        NEW.unit_id NOT IN (
            SELECT u.id FROM Nutrient AS n INNER JOIN Unit AS u 
                ON (n.unit_types | u.type_id != 0)
                WHERE n.id = NEW.nutrient_id
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Unit has incorrect type for FoodNutrientValue');
    END;

CREATE TRIGGER check_valid_nutrient_goal_value_units
    BEFORE INSERT ON NutrientGoalValue
    WHEN (
        -- select all possible units for the new unit type
        NEW.unit_id NOT IN (
            SELECT u.id FROM Nutrient AS n INNER JOIN Unit AS u 
                ON (n.unit_types | u.type_id != 0)
                WHERE n.id = NEW.nutrient_id
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Unit has incorrect type for NutrientGoalValue');
    END;

CREATE TRIGGER check_valid_nutrient_goal_value_units_2
    BEFORE UPDATE ON NutrientGoalValue
    WHEN (
        NEW.unit_id NOT IN (
            SELECT u.id FROM Nutrient AS n INNER JOIN Unit AS u 
                ON (n.unit_types | u.type_id != 0)
                WHERE n.id = NEW.nutrient_id
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Unit has incorrect type for NutrientGoalValue');
    END;

-- vim: ts=4 et
