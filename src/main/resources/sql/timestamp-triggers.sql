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

-- Nutrient
CREATE TRIGGER init_nutrient_timestamp
    AFTER INSERT ON Nutrient
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE Nutrient
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_nutrient_timestamp
    AFTER UPDATE ON Nutrient
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE Nutrient
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- FoodNutrientValue
CREATE TRIGGER init_food_nutrient_value_timestamp
    AFTER INSERT ON FoodNutrientValue
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE FoodNutrientValue
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_food_nutrient_value_timestamp
    AFTER UPDATE ON FoodNutrientValue
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE FoodNutrientValue
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- MealNutrientGoalValue
CREATE TRIGGER init_meal_nutrient_goal_value_timestamp
    AFTER INSERT ON MealNutrientGoalValue
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE MealNutrientGoalValue
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_meal_nutrient_goal_value_timestamp
    AFTER UPDATE ON MealNutrientGoalValue
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE MealNutrientGoalValue
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

-- DayNutrientGoalValue
CREATE TRIGGER init_day_nutrient_goal_value_timestamp
    AFTER INSERT ON DayNutrientGoalValue
    WHEN (NEW.create_time = 0)
    BEGIN
        UPDATE DayNutrientGoalValue
        SET create_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;

CREATE TRIGGER update_day_nutrient_goal_value_timestamp
    AFTER UPDATE ON DayNutrientGoalValue
    WHEN (NEW.modify_time = OLD.modify_time
        OR NEW.modify_time = 0)
    BEGIN
        UPDATE DayNutrientGoalValue
        SET modify_time = strftime('%s', 'now')
        WHERE id = NEW.id;
    END;
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
