
CREATE TRIGGER check_servings_match_foods
    BEFORE INSERT ON FoodPortion
    WHEN (
        NEW.serving_id IS NOT NULL
        AND NOT EXISTS (
            SELECT * FROM Serving
            WHERE (id = NEW.serving_id AND food_id = NEW.food_id)
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Invalid food / serving combination');
    END;

CREATE TRIGGER check_servings_match_foods_2
    BEFORE UPDATE ON FoodPortion
    WHEN (
        NEW.serving_id IS NOT NULL
        AND NOT EXISTS (
            SELECT * FROM Serving
            WHERE (id = NEW.serving_id AND food_id = NEW.food_id)
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Invalid food / serving combination');
    END;

-- Timestamp triggers
-- we store timestamps in unix time.
-- Since strftime() always returns strings, but we declare the columns
-- with integer affinity, SQLite automatically does the type conversion.

-- Food
CREATE TRIGGER init_food_timestamp
    AFTER INSERT ON Food
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE Food
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_food_timestamp
    AFTER UPDATE ON Food
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE Food
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- Serving
CREATE TRIGGER init_serving_timestamp
    AFTER INSERT ON Serving
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE Serving
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_serving_timestamp
    AFTER UPDATE ON Serving
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE Serving
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- Meal
CREATE TRIGGER init_meal_timestamp
    AFTER INSERT ON Meal
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE Meal
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;
CREATE TRIGGER init_meal_time_column
    AFTER INSERT ON Meal
    WHEN (NEW.time = 0)
    BEGIN
        UPDATE Meal
        SET time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_meal_timestamp
    AFTER UPDATE ON MEAL
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE Meal
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- FoodPortion
CREATE TRIGGER init_food_portion_timestamp
    AFTER INSERT ON FoodPortion
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE FoodPortion
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_food_portion_timestamp
    AFTER UPDATE ON FoodPortion
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE FoodPortion
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- FoodCategory
CREATE TRIGGER init_food_category_timestamp
    AFTER INSERT ON FoodCategory
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE FoodCategory
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_food_category_timestamp
    AFTER UPDATE ON FoodCategory
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE FoodCategory
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- NutritionData
CREATE TRIGGER init_nutrition_data_timestamp
    AFTER INSERT ON NutritionData
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE NutritionData
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_nutrition_data_timestamp
    AFTER UPDATE ON NutritionData
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE NutritionData
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- Ingredient
CREATE TRIGGER init_ingredient_timestamp
    AFTER INSERT ON Ingredient
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE Ingredient
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_ingredient_timestamp
    AFTER UPDATE ON Ingredient
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE Ingredient
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- QuantityUnit
CREATE TRIGGER init_quantity_unit_timestamp
    AFTER INSERT ON QuantityUnit
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE QuantityUnit
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_quantity_unit_timestamp
    AFTER UPDATE ON QuantityUnit
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE QuantityUnit
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- RegularMeal
CREATE TRIGGER init_regular_meal_timestamp
    AFTER INSERT ON RegularMeal
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE RegularMeal
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_regular_meal_timestamp
    AFTER UPDATE ON RegularMeal
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE RegularMeal
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- FoodAttribute
CREATE TRIGGER init_food_attribute_timestamp
    AFTER INSERT ON FoodAttribute
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE FoodAttribute
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_food_attribute_timestamp
    AFTER UPDATE ON FoodAttribute
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE FoodAttribute
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- FoodAttribute
CREATE TRIGGER init_attribute_mapping_timestamp
    AFTER INSERT ON AttributeMapping
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE AttributeMapping
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_attribute_mapping_timestamp
    AFTER UPDATE ON AttributeMapping
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE AttributeMapping
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- vim: ts=4 et
