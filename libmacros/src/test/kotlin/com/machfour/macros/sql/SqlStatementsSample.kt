package com.machfour.macros.sql

internal const val expectedStatements = """


PRAGMA foreign_keys = ON;
PRAGMA recursive_triggers = ON;


CREATE TABLE Unit (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , type_id INTEGER NOT NULL
 , name TEXT NOT NULL UNIQUE
 , abbreviation TEXT NOT NULL UNIQUE
 , metric_equivalent REAL NOT NULL
 , inbuilt INTEGER NOT NULL DEFAULT 0

 , CONSTRAINT boolean_inbuilt
 CHECK (inbuilt IN (0, 1))
 , CONSTRAINT positive_conversion
 CHECK (metric_equivalent > 0)
) STRICT;


CREATE TABLE Nutrient (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , name TEXT NOT NULL UNIQUE
 , unit_types INTEGER NOT NULL
 , inbuilt INTEGER NOT NULL DEFAULT 0
 , CONSTRAINT boolean_inbuilt
 CHECK (inbuilt IN (0, 1))
) STRICT;


CREATE TABLE Food (









 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , index_name TEXT NOT NULL UNIQUE
 , brand TEXT DEFAULT NULL
 , variety TEXT DEFAULT NULL
 , name TEXT NOT NULL
 , extra_desc TEXT DEFAULT NULL
 , notes TEXT DEFAULT NULL
 , category TEXT DEFAULT NULL
 , food_type TEXT NOT NULL DEFAULT 'primary'
 , usda_index INTEGER DEFAULT NULL UNIQUE
 , nuttab_index TEXT DEFAULT NULL UNIQUE
 , data_source TEXT DEFAULT NULL
 , data_notes TEXT DEFAULT NULL
 , density REAL DEFAULT NULL
 , search_relevance INTEGER DEFAULT NULL

 , CONSTRAINT full_name_identifiable
 UNIQUE (brand, variety, name, extra_desc)
) STRICT;


CREATE UNIQUE INDEX food_index ON Food (index_name);


CREATE TABLE FoodCategory (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , name TEXT NOT NULL UNIQUE
) STRICT;


CREATE TABLE FoodAttribute (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , name TEXT NOT NULL UNIQUE
) STRICT;


CREATE TABLE AttributeMapping (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , food_id INTEGER NOT NULL
 , attribute_id INTEGER NOT NULL

 , CONSTRAINT single_association
 UNIQUE (food_id, attribute_id)
 , CONSTRAINT valid_food
 FOREIGN KEY (food_id)
 REFERENCES Food (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
 , CONSTRAINT valid_attribute
 FOREIGN KEY (attribute_id)
 REFERENCES FoodAttribute (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
) STRICT;


CREATE TABLE Serving (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , name TEXT NOT NULL
 , notes TEXT DEFAULT NULL
 , quantity REAL NOT NULL
 , quantity_unit TEXT NOT NULL
 , is_default INTEGER DEFAULT NULL
 , food_id INTEGER NOT NULL

 , CONSTRAINT unique_qty_per_food
 UNIQUE (food_id, quantity, quantity_unit)
 , CONSTRAINT unique_name_per_food
 UNIQUE (food_id, name)
 , CONSTRAINT quantity_positive
 CHECK (quantity > 0)
 , CONSTRAINT valid_unit
 FOREIGN KEY (quantity_unit)
 REFERENCES Unit (abbreviation)
 ON UPDATE CASCADE
 ON DELETE RESTRICT
 , CONSTRAINT valid_food
 FOREIGN KEY (food_id)
 REFERENCES Food (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
 , CONSTRAINT unique_default_food
 UNIQUE (food_id, is_default)
 , CONSTRAINT null_or_true
 CHECK (is_default IS NULL OR is_default = 1)
) STRICT;


CREATE TABLE NutrientGoal (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , name TEXT NOT NULL
) STRICT;


CREATE TABLE NutrientGoalDayMapping (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , day TEXT NOT NULL UNIQUE
 , goal_id INTEGER NOT NULL

 , CONSTRAINT valid_nutrient_goal
 FOREIGN KEY (goal_id)
 REFERENCES NutrientGoal (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
) STRICT;


CREATE TABLE Meal (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , name TEXT NOT NULL
 , day TEXT NOT NULL DEFAULT (date('now', 'localtime'))
 , notes TEXT DEFAULT NULL
 , start_time INTEGER NOT NULL
 , duration INTEGER NOT NULL DEFAULT 0
 , goal_id INTEGER DEFAULT NULL

 , CONSTRAINT nonnegative_duration
 CHECK (duration >= 0)

 , CONSTRAINT valid_nutrient_goal
 FOREIGN KEY (goal_id)
 REFERENCES NutrientGoal (id)
 ON UPDATE CASCADE
 ON DELETE SET NULL
) STRICT;


CREATE TABLE FoodPortion (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , quantity REAL NOT NULL
 , quantity_unit TEXT NOT NULL
 , food_id INTEGER NOT NULL
 , nutrient_max_version INTEGER NOT NULL
 , serving_id INTEGER DEFAULT NULL
 , notes TEXT DEFAULT NULL


 , meal_id INTEGER NOT NULL
 , recipe_max_version INTEGER NOT NULL

 , CONSTRAINT valid_unit
 FOREIGN KEY (quantity_unit)
 REFERENCES Unit (abbreviation)
 ON UPDATE CASCADE
 ON DELETE RESTRICT
 , CONSTRAINT valid_food
 FOREIGN KEY (food_id)
 REFERENCES Food (id)
 ON DELETE RESTRICT
 ON UPDATE CASCADE
 , CONSTRAINT valid_serving
 FOREIGN KEY (serving_id)
 REFERENCES Serving (id)
 ON DELETE SET NULL
 ON UPDATE CASCADE
 , CONSTRAINT valid_nutrient_version
 CHECK (nutrient_max_version >= 1)


 , CONSTRAINT valid_meal
 FOREIGN KEY (meal_id)
 REFERENCES Meal (id)
 ON DELETE CASCADE
 ON UPDATE CASCADE
 , CONSTRAINT valid_recipe_version
 CHECK (recipe_max_version >= 1)
) STRICT;


CREATE TABLE Ingredient (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , quantity REAL NOT NULL
 , quantity_unit TEXT NOT NULL
 , food_id INTEGER NOT NULL
 , serving_id INTEGER DEFAULT NULL
 , nutrient_max_version INTEGER NOT NULL
 , notes TEXT DEFAULT NULL

 , parent_food_id INTEGER NOT NULL
 , recipe_version INTEGER NOT NULL

 , CONSTRAINT valid_unit
 FOREIGN KEY (quantity_unit)
 REFERENCES Unit (abbreviation)
 ON UPDATE CASCADE
 ON DELETE RESTRICT
 , CONSTRAINT valid_food
 FOREIGN KEY (food_id)
 REFERENCES Food (id)
 ON DELETE RESTRICT
 ON UPDATE CASCADE
 , CONSTRAINT valid_serving
 FOREIGN KEY (serving_id)
 REFERENCES Serving (id)
 ON DELETE SET NULL
 ON UPDATE CASCADE
 , CONSTRAINT valid_nutrient_version
 CHECK (nutrient_max_version >= 1)
 , CONSTRAINT valid_nutrient_version
 CHECK (nutrient_max_version >= 1)

 , CONSTRAINT valid_recipe_version
 CHECK (recipe_version >= 1)
 , CONSTRAINT valid_composite_food
 FOREIGN KEY (parent_food_id)
 REFERENCES Food (id)
 ON DELETE CASCADE
 ON UPDATE CASCADE
 , CONSTRAINT cannot_contain_itself
 CHECK (parent_food_id != id)
) STRICT;


CREATE TABLE FoodNutrientValue (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , nutrient_id INTEGER NOT NULL
 , value REAL NOT NULL
 , constraint_spec INTEGER NOT NULL DEFAULT 0
 , unit_id INTEGER NOT NULL

 , food_id INTEGER NOT NULL
 , version INTEGER NOT NULL

 , CONSTRAINT valid_nutrient
 FOREIGN KEY (nutrient_id)
 REFERENCES Nutrient (id)
 ON UPDATE CASCADE
 ON DELETE RESTRICT
 , CONSTRAINT valid_unit
 FOREIGN KEY (unit_id)
 REFERENCES Unit (id)
 ON UPDATE CASCADE
 ON DELETE RESTRICT
 , CONSTRAINT nonnegative_value
 CHECK (value >= 0)
 , CONSTRAINT valid_constraint_spec
 CHECK (constraint_spec IN (-1, 0, 1))

 , CONSTRAINT valid_food
 FOREIGN KEY (food_id)
 REFERENCES Food (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
 , CONSTRAINT single_nutrient_per_food
 UNIQUE (food_id, nutrient_id, version)
 , CONSTRAINT valid_version
 CHECK (version >= 1)
) STRICT;


CREATE TABLE NutrientGoalValue (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , nutrient_id INTEGER NOT NULL
 , value REAL NOT NULL
 , constraint_spec INTEGER NOT NULL DEFAULT 0
 , unit_id INTEGER NOT NULL

 , goal_id INTEGER NOT NULL

 , CONSTRAINT valid_nutrient
 FOREIGN KEY (nutrient_id)
 REFERENCES Nutrient (id)
 ON UPDATE CASCADE
 ON DELETE RESTRICT
 , CONSTRAINT valid_unit
 FOREIGN KEY (unit_id)
 REFERENCES Unit (id)
 ON UPDATE CASCADE
 ON DELETE RESTRICT
 , CONSTRAINT nonnegative_value
 CHECK (value >= 0)
 , CONSTRAINT valid_constraint_spec
 CHECK (constraint_spec IN (-1, 0, 1))
 , CONSTRAINT valid_goal
 FOREIGN KEY (goal_id)
 REFERENCES NutrientGoal (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
) STRICT;


CREATE TABLE RegularMeal (
 id INTEGER PRIMARY KEY ASC
 , create_time INTEGER NOT NULL DEFAULT 0
 , modify_time INTEGER NOT NULL DEFAULT 0
 , name TEXT NOT NULL
 , meal_id INTEGER NOT NULL UNIQUE

 , CONSTRAINT valid_meal
 FOREIGN KEY (meal_id)
 REFERENCES Meal (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
) STRICT;

"""

