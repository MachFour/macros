-- file syntax details [used for autoprocessing]
-- comments always require two dashes to begin them,
-- and last only until the end of the line
-- use spaces for indentation, not tabs

-- note that these pragmas need to be set every time a database connection is made
PRAGMA foreign_keys = ON;

PRAGMA recursive_triggers = ON;

CREATE TABLE QuantityUnit (
      id                   INTEGER PRIMARY KEY ASC
    , name                 TEXT NOT NULL UNIQUE
    , abbreviation         TEXT NOT NULL UNIQUE
    , is_volume_unit       INTEGER NOT NULL
    -- quantity of 1 of this unit in g or ml, as appropriate
    , metric_equivalent    REAL NOT NULL
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    , CONSTRAINT zero_or_one
        CHECK (is_volume_unit IN (0, 1))
    , CONSTRAINT positive_conversion
        CHECK (metric_equivalent > 0)
);

CREATE TABLE Food (
    -- Field meanings
    -- 'brand' is the manufacturer, if the food has a manufacurer that you know.
    -- This just helps to identify the food.

    -- 'name' is a generic name for the food.
    -- Avoid putting commercial names in the 'name' field, but you can if there's
    -- no good alternative. It just means you can't do cool things like comparing
    -- between different brands of the same food.

    -- 'variety' is a food-specific qualifier. For a commercial food, it might be
    -- the product name (and maybe even flavour, for something like potato chips)
    -- something like 'Gala' for an apple.

    -- 'variety_after_name' controls the formatting of the food. So you
    -- can do things like 'John West' 'tuna' 'in olive oil' (brand, name, variety)
    -- rather than, 'John West' 'olive oil' 'tuna' (brand, variety, name)
    -- which just looks weird.

    -- 'attributes' are generally any qualifiers that are not specific to the
    -- food, or that identify it but aren't really part of the 'variety'.
    -- For example 'low fat', or 'raw', or 'organic'. Again there's no strict
    -- definition of what is a variety vs an attribute. But the useful thing
    -- about attributes is you can look up all foods sharing an attribute. For
    -- example, search everything tagged 'organic'.

    -- 'notes' contains any other useful information about the food. For example,
    -- information about the ingredients, or preparation method (for tinned fish,
    -- whether they're in brine or oil), or whether the skin is included in the
    -- weight, for fruit, or whether meat is weighed uncooked vs cooked.

    -- 'category' attempts to group foods into broad categories. Feel free to come
    -- up with your own categorisation method, or use mine, or don't use any at all.

    -- The display formula for each food is
    -- <brand> <variety> <name> (<attribute1>, <attribute2>, ...) or
    -- <brand> <name> <variety> (<attribute1>, <attribute2>, ...)

    -- Notes are not normally displayed, so try to make the food recognisable
    -- without them

      id                   INTEGER PRIMARY KEY ASC
    , index_name           TEXT NOT NULL UNIQUE
    , brand                TEXT DEFAULT NULL
    , variety              TEXT DEFAULT NULL
    , name                 TEXT NOT NULL
    , variety_after_name   INTEGER NOT NULL DEFAULT 0
    , notes                TEXT DEFAULT NULL
    , category             TEXT NOT NULL DEFAULT "uncategorised"
    , food_type            TEXT NOT NULL DEFAULT "primary"
    -- miscellaneous metadata
    , usda_index           INTEGER DEFAULT NULL
    , nuttab_index         TEXT DEFAULT NULL
    -- timestamps are stored in unix time, and set via triggers
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    -- referencing the name of the food category makes importing foods a LOT easier
    , CONSTRAINT valid_category
        FOREIGN KEY (category)
        REFERENCES FoodCategory (name)
        ON UPDATE CASCADE
        ON DELETE SET DEFAULT
    , CONSTRAINT zero_one
        CHECK (variety_after_name IN (0, 1))
    , CONSTRAINT valid_food_type
        CHECK (food_type IN ("primary", "composite", "usda", "nuttab", "special"))
);

CREATE UNIQUE INDEX food_index ON Food (index_name);

CREATE TABLE FoodCategory (
      id                   INTEGER PRIMARY KEY ASC
    , name                 TEXT NOT NULL UNIQUE
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE FoodAttribute (
      id                   INTEGER PRIMARY KEY ASC
    , name                 TEXT NOT NULL UNIQUE
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE AttributeMapping (
      id                   INTEGER PRIMARY KEY ASC
    , food_id              INTEGER NOT NULL
    , attribute_id         INTEGER NOT NULL
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

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
);

-- so you can put in '1 tin' of tuna, not 95g.
CREATE TABLE Serving (
      id                   INTEGER PRIMARY KEY ASC
    -- name should ideally be pluralisable with 's'
    , name                 TEXT NOT NULL
    -- in g/mL, depending on quantity_unit
    , quantity             REAL NOT NULL
    -- grams (0) or ml (1)
    , quantity_unit        TEXT NOT NULL
    -- is this the default serving?
    -- 1 for true, NULL for false, see below.
    , is_default           INTEGER DEFAULT NULL
    -- which food this serving pertains to
    , food_id              INTEGER NOT NULL
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    , CONSTRAINT unique_qty_per_food
        UNIQUE (food_id, quantity, quantity_unit)
    -- otherwise file parsing doesn't work
    , CONSTRAINT unique_name_per_food
        UNIQUE (food_id, name)
    , CONSTRAINT quantity_positive
        CHECK (quantity > 0)
    , CONSTRAINT valid_unit
        FOREIGN KEY (quantity_unit)
        REFERENCES QuantityUnit (abbreviation)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
    , CONSTRAINT valid_food
        FOREIGN KEY (food_id)
        REFERENCES Food (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
    -- hacky solution uses 1 for true and NULL for false,
    -- because then UNIQUE constraints can be used to check
    -- that there's only one default serving per food
    , CONSTRAINT unique_default_food
        UNIQUE (food_id, is_default)
    , CONSTRAINT null_or_true
        CHECK (is_default IS NULL OR is_default = 1)
);

CREATE TABLE Meal (
      id                   INTEGER PRIMARY KEY ASC
    , name                 TEXT NOT NULL
    -- which day to associate the meal with. Dependent on user's time zone.
    -- only year, month and day stored, in ISO8601 format.
    , day                  TEXT NOT NULL DEFAULT (date('now', 'localtime'))
    -- timestamps are updated via triggers
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    -- table restriction
    --, CONSTRAINT valid_meal_description
    --    FOREIGN KEY (name)
    --    REFERENCES MealDescription (name)
    --    -- don't care, just keep old name
    --    ON DELETE NO ACTION
    --    ON UPDATE CASCADE
    , CONSTRAINT single_meal_per_day
        UNIQUE (day, name)
);

CREATE TABLE FoodPortion (
      id                   INTEGER PRIMARY KEY ASC
    , quantity             REAL NOT NULL
    -- XXX what happens if the measured_by_volume changes (i.e. a food once
    --   measured_by_volume is now measured as a solid (for nutrition info)?
    -- 0 for grams, 1 for mL
    , quantity_unit        INTEGER NOT NULL
    , food_id              INTEGER NOT NULL
    , meal_id              INTEGER NOT NULL
    -- records whether user entered the quantity as a serving.
    , serving_id           INTEGER DEFAULT NULL
    -- for arbitrary text
    , notes                TEXT DEFAULT NULL
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    , CONSTRAINT valid_unit
        FOREIGN KEY (quantity_unit)
        REFERENCES QuantityUnit (abbreviation)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
    , CONSTRAINT valid_food
        FOREIGN KEY (food_id)
        REFERENCES Food (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE

    -- when meal records are deleted, records of foods in that meal
    -- are also deleted.
    , CONSTRAINT valid_meal
        FOREIGN KEY (meal_id)
        REFERENCES Meal (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE

    -- we don't really care if servings are deleted, since the
    -- quantity is the important one anyway.
    -- can always be set to a different serving of that food
    , CONSTRAINT valid_serving
        FOREIGN KEY (serving_id)
        REFERENCES Serving (id)
        -- trigger to set only the serving to null
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE Ingredient (
      id                   INTEGER PRIMARY KEY ASC
    , composite_food_id    INTEGER NOT NULL
    , ingredient_food_id   INTEGER NOT NULL
    -- same rules for quantity fields as for FoodPortion
    , quantity             REAL NOT NULL
    , quantity_unit        INTEGER NOT NULL
    , serving_id           INTEGER DEFAULT NULL
    , notes                TEXT DEFAULT NULL
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    , CONSTRAINT cannot_contain_itself
        CHECK (composite_food_id != ingredient_food_id)
    , CONSTRAINT valid_unit
        FOREIGN KEY (quantity_unit)
        REFERENCES QuantityUnit (abbreviation)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
    , CONSTRAINT valid_ingredient
        FOREIGN KEY (ingredient_food_id)
        REFERENCES Food (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    , CONSTRAINT valid_composite_food
        FOREIGN KEY (composite_food_id)
        REFERENCES Food (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
    -- copied from FoodPortion
    , CONSTRAINT valid_serving
        FOREIGN KEY (serving_id)
        REFERENCES Serving (id)
        -- trigger to set only the serving to null
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE NutritionData (
      id                   INTEGER PRIMARY KEY ASC
    , food_id              INTEGER NOT NULL UNIQUE
    , data_source          TEXT DEFAULT NULL
    , quantity             REAL NOT NULL DEFAULT 100
    , quantity_unit        TEXT NOT NULL
    -- for liquids, how to convert between grams and mL
    , density              REAL DEFAULT NULL
    -- storing both kilojoules and calories is pretty redundant
    , kilojoules           REAL DEFAULT NULL
    , calories             REAL DEFAULT NULL
    , protein              REAL DEFAULT NULL
    , carbohydrate         REAL DEFAULT NULL
    , carbohydrate_by_diff REAL DEFAULT NULL
    , sugar                REAL DEFAULT NULL
    , sugar_alcohol        REAL DEFAULT NULL
    , starch               REAL DEFAULT NULL
    , fat                  REAL DEFAULT NULL
    , saturated_fat        REAL DEFAULT NULL
    , monounsaturated_fat  REAL DEFAULT NULL
    , polyunsaturated_fat  REAL DEFAULT NULL
    , omega_3              REAL DEFAULT NULL
    , omega_6              REAL DEFAULT NULL
    , fibre                REAL DEFAULT NULL
    , sodium               REAL DEFAULT NULL
    , salt                 REAL DEFAULT NULL
    , calcium              REAL DEFAULT NULL
    , iron                 REAL DEFAULT NULL
    , water                REAL DEFAULT NULL
    , alcohol              REAL DEFAULT NULL
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    , CONSTRAINT valid_food
        FOREIGN KEY (food_id)
        REFERENCES Food (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
    , CONSTRAINT quantity_positive
        CHECK (quantity > 0)
    , CONSTRAINT valid_unit
        FOREIGN KEY (quantity_unit)
        REFERENCES QuantityUnit (abbreviation)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE RegularMeal (
      id                   INTEGER PRIMARY KEY ASC
    , meal_id              INTEGER NOT NULL UNIQUE
    , name                 TEXT NOT NULL
    -- timestamps are stored in unix time, and set via triggers
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0
    -- don't let user delete a favourited meal; have to unfavourite first
    , CONSTRAINT valid_meal
        FOREIGN KEY (meal_id)
        REFERENCES Meal (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- vim: et ts=4
