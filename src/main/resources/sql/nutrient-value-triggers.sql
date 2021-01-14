
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
CREATE TRIGGER check_valid_meal_nutrient_goal_value_units
    BEFORE INSERT ON MealNutrientGoalValue
    WHEN (
        -- select all possible units for the new unit type
        NEW.unit_id NOT IN (
            SELECT u.id FROM Nutrient AS n INNER JOIN Unit AS u 
                ON (n.unit_types | u.type_id != 0)
                WHERE n.id = NEW.nutrient_id
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Unit has incorrect type for MealNutrientGoalValue');
    END;

CREATE TRIGGER check_valid_meal_nutrient_goal_value_units_2
    BEFORE UPDATE ON MealNutrientGoalValue
    WHEN (
        NEW.unit_id NOT IN (
            SELECT u.id FROM Nutrient AS n INNER JOIN Unit AS u 
                ON (n.unit_types | u.type_id != 0)
                WHERE n.id = NEW.nutrient_id
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Unit has incorrect type for MealNutrientGoalValue');
    END;

CREATE TRIGGER check_valid_day_nutrient_goal_value_units
    BEFORE INSERT ON DayNutrientGoalValue
    WHEN (
        -- select all possible units for the new unit type
        NEW.unit_id NOT IN (
            SELECT u.id FROM Nutrient AS n INNER JOIN Unit AS u 
                ON (n.unit_types | u.type_id != 0)
                WHERE n.id = NEW.nutrient_id
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Unit has incorrect type for DayNutrientGoalValue');
    END;

CREATE TRIGGER check_valid_day_nutrient_goal_value_units_2
    BEFORE UPDATE ON DayNutrientGoalValue
    WHEN (
        NEW.unit_id NOT IN (
            SELECT u.id FROM Nutrient AS n INNER JOIN Unit AS u 
                ON (n.unit_types | u.type_id != 0)
                WHERE n.id = NEW.nutrient_id
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Unit has incorrect type for DayNutrientGoalValue');
    END;

-- vim: ts=4 et
