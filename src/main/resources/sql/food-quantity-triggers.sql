-- food portion servings match foods
CREATE TRIGGER check_food_portion_servings_match_foods
    BEFORE INSERT ON FoodPortion
    WHEN (
        NEW.serving_id IS NOT NULL
        AND NOT EXISTS (
            SELECT * FROM Serving
            WHERE (id = NEW.serving_id AND food_id = NEW.food_id)
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Invalid food / serving combination for FoodPortion');
    END;

CREATE TRIGGER check_food_portion_servings_match_foods_2
    BEFORE UPDATE ON FoodPortion
    WHEN (
        NEW.serving_id IS NOT NULL
        AND NOT EXISTS (
            SELECT * FROM Serving
            WHERE (id = NEW.serving_id AND food_id = NEW.food_id)
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Invalid food / serving combination for FoodPortion');
    END;

-- ingredient servings match foods
CREATE TRIGGER check_ingredient_servings_match_foods
    BEFORE INSERT ON Ingredient
    WHEN (
            NEW.serving_id IS NOT NULL
            AND NOT EXISTS (
                SELECT * FROM Serving
                WHERE (id = NEW.serving_id AND food_id = NEW.food_id)
            )
        )
BEGIN
    SELECT RAISE (ABORT, 'Invalid food / serving combination for Ingredient');
END;

CREATE TRIGGER check_ingredient_servings_match_foods_2
    BEFORE UPDATE ON Ingredient
    WHEN (
            NEW.serving_id IS NOT NULL
            AND NOT EXISTS (
                SELECT * FROM Serving
                WHERE (id = NEW.serving_id AND food_id = NEW.food_id)
            )
        )
BEGIN
    SELECT RAISE (ABORT, 'Invalid food / serving combination for Ingredient');
END;

-- vim: ts=4 et
