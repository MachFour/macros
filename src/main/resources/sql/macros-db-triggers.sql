
-- food quantity servings match foods
CREATE TRIGGER check_food_quantity_servings_match_foods
    BEFORE INSERT ON FoodQuantity
    WHEN (
        NEW.serving_id IS NOT NULL
        AND NOT EXISTS (
            SELECT * FROM Serving
            WHERE (id = NEW.serving_id AND food_id = NEW.food_id)
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Invalid food / serving combination for FoodQuantity');
    END;

CREATE TRIGGER check_food_quantity_servings_match_foods_2
    BEFORE UPDATE ON FoodQuantity
    WHEN (
        NEW.serving_id IS NOT NULL
        AND NOT EXISTS (
            SELECT * FROM Serving
            WHERE (id = NEW.serving_id AND food_id = NEW.food_id)
        )
    )
    BEGIN
        SELECT RAISE (ABORT, 'Invalid food / serving combination for FoodQuantity');
    END;

--CREATE TRIGGER check_valid_nutrient_value_units
--    BEFORE UPDATE ON NutrientValue
--    WHEN (
--        SELECT type_id FROM Unit
--        WHERE id = NEW.unit_id
--        -- IS NOT EQUAL TO
--        SELECT unit_type FROM Nutrient
--        WHERE id = NEW.nutrient_id
--    )
--    BEGIN
--        SELECT RAISE (ABORT, 'Unit has incorrect type for NutrientValue');
--    END;


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
    WHEN (NEW.start_time = 0)
    BEGIN
        UPDATE Meal
        SET start_time = strftime('%s', 'now')
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

-- FoodQuantity
CREATE TRIGGER init_food_quantity_timestamp
    AFTER INSERT ON FoodQuantity
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE FoodQuantity
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_food_quantity_timestamp
    AFTER UPDATE ON FoodQuantity
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE FoodQuantity
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

-- Nutrient
--CREATE TRIGGER init_nutrient_timestamp
--    AFTER INSERT ON Nutrient
--    WHEN (NEW.create_time = 0)
--    BEGIN
--        UPDATE Nutrient
--        SET create_time = strftime('%s', 'now')
--        WHERE id = NEW.id;
--    END;

--CREATE TRIGGER update_nutrient_timestamp
--    AFTER UPDATE ON Nutrient
--    WHEN (NEW.modify_time = OLD.modify_time
--        OR NEW.modify_time = 0)
--    BEGIN
--        UPDATE Nutrient
--        SET modify_time = strftime('%s', 'now')
--        WHERE id = NEW.id;
--    END;

-- NutrientValue
--CREATE TRIGGER init_nutrient_value_timestamp
--    AFTER INSERT ON NutrientValue
--    WHEN (NEW.create_time = 0)
--    BEGIN
--        UPDATE NutrientValue
--        SET create_time = strftime('%s', 'now')
--        WHERE id = NEW.id;
--    END;

--CREATE TRIGGER update_nutrient_value_timestamp
--    AFTER UPDATE ON NutrientValue
--    WHEN (NEW.modify_time = OLD.modify_time
--        OR NEW.modify_time = 0)
--    BEGIN
--        UPDATE NutrientValue
--        SET modify_time = strftime('%s', 'now')
--        WHERE id = NEW.id;
--    END;

-- Unit
CREATE TRIGGER init_unit_timestamp
    AFTER INSERT ON Unit
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE Unit
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_unit_timestamp
    AFTER UPDATE ON Unit
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE Unit
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

-- AttributeMapping
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
