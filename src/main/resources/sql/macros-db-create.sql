-- file syntax details [used for autoprocessing]
-- comments always require two dashes to begin them,
-- and last only until the end of the line
-- use spaces for indentation, not tabs

-- note that these pragmas need to be set every time a database connection is made
PRAGMA foreign_keys = ON;

PRAGMA recursive_triggers = ON;

-- Measures mass (weight), volume or energy
CREATE TABLE Unit (
      id                   INTEGER PRIMARY KEY ASC
    -- indicates which types can be converted between
    , type_id              INTEGER NOT NULL
    , name                 TEXT NOT NULL UNIQUE
    , abbreviation         TEXT NOT NULL UNIQUE
    -- quantity of 1 of this unit in g, ml, or kJ as appropriate
    , metric_equivalent    REAL NOT NULL
    , inbuilt              INTEGER NOT NULL DEFAULT 0
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    , CONSTRAINT boolean_inbuilt
        CHECK (inbuilt IN (0, 1))
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

    -- 'extra_desc' is any extra description of the food that can help to identify
    -- it in a list of similar alternatives. For example it may be a flavour (in olive oil)
    -- or preparation method (cooked, raw, peeled)

    -- 'attributes' are generally any qualifiers that are not specific to the
    -- food, or that identify it but aren't really part of the 'variety'.
    -- For example 'low fat', or 'raw', or 'organic'. Again there's no strict
    -- definition of what is a variety vs an attribute. But the useful thing
    -- about attributes is you can look up all foods sharing an attribute. For
    -- example, search everything tagged 'organic'.

    -- 'notes' contains any other useful information about the food. For example,
    -- information about the ingredients, or origin of the food.
    -- This information won't be displayed in a list form as there can be a lot of text
    -- so it shouldn't be relied upon in order to identify the food.

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
    , extra_desc           TEXT DEFAULT NULL
    , notes                TEXT DEFAULT NULL
    , category             TEXT NOT NULL DEFAULT 'uncategorised'
    , food_type            TEXT NOT NULL DEFAULT 'primary'
    -- miscellaneous metadata
    , usda_index           INTEGER DEFAULT NULL
    , nuttab_index         TEXT DEFAULT NULL
    -- old NutritionData fields
    , data_source          TEXT DEFAULT NULL
    , data_notes           TEXT DEFAULT NULL
    -- for liquids, how to convert between grams and mL
    -- in g/cm^3
    , density              REAL DEFAULT NULL
    -- timestamps are stored in unix time, and set via triggers
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    -- referencing the name of the food category makes importing foods a LOT easier
    , CONSTRAINT valid_category
        FOREIGN KEY (category)
        REFERENCES FoodCategory (name)
        ON UPDATE CASCADE
        ON DELETE SET DEFAULT
    , CONSTRAINT identifiable
        UNIQUE (brand, variety, name, extra_desc)
    , CONSTRAINT valid_food_type
        CHECK (food_type IN ('primary', 'composite', 'usda', 'nuttab', 'special'))
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
    , quantity             REAL NOT NULL
    -- unit abbreviation
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
        REFERENCES Unit (abbreviation)
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
    -- start time when meal was eaten. Defaults to creation time via trigger.
    -- Measured in unix time
    , start_time           INTEGER NOT NULL DEFAULT 0
    -- duration of meal, in seconds from the start_time
    , duration             INTEGER NOT NULL DEFAULT 0
    -- timestamps are updated via triggers
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0
    , notes                TEXT DEFAULT NULL

    -- table restriction
    --, CONSTRAINT valid_meal_description
    --    FOREIGN KEY (name)
    --    REFERENCES MealDescription (name)
    --    -- don't care, just keep old name
    --    ON DELETE NO ACTION
    --    ON UPDATE CASCADE
    -- , CONSTRAINT single_meal_per_day
    --    UNIQUE (day, name)
    , CONSTRAINT nonnegative_duration
        CHECK (duration >= 0)
);

-- Encompasses both FoodPortions in meals and Ingredients in Composite foods.
CREATE TABLE FoodQuantity (
      id                   INTEGER PRIMARY KEY ASC
    , quantity             REAL NOT NULL
    -- XXX what happens if the unit changes (i.e. a food once measured by volume
    -- is now measured as a solid (for nutrition info)?
    , quantity_unit        TEXT NOT NULL
    -- food / ingredient ID
    , food_id              INTEGER NOT NULL
    -- exactly one of these must be defined
    , meal_id              INTEGER DEFAULT NULL
    , parent_food_id    INTEGER DEFAULT NULL
    -- records whether user entered the quantity as a serving.
    , serving_id           INTEGER DEFAULT NULL
    -- for arbitrary text
    , notes                TEXT DEFAULT NULL
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

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
    , CONSTRAINT food_portion_xor_ingredient
        CHECK ((meal_id IS NULL) <> (parent_food_id IS NULL))
    -- when meal or parent food is deleted, records of foods contained
    -- are also deleted.
    , CONSTRAINT valid_meal
        FOREIGN KEY (meal_id)
        REFERENCES Meal (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
    , CONSTRAINT valid_composite_food
        FOREIGN KEY (parent_food_id)
        REFERENCES Food (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
    , CONSTRAINT cannot_contain_itself
        CHECK (parent_food_id != id)

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

CREATE TABLE Nutrient (
      id                   INTEGER PRIMARY KEY ASC
    , name                 TEXT NOT NULL UNIQUE
    , unit_types           INTEGER NOT NULL
    , inbuilt              INTEGER NOT NULL DEFAULT 0
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0
    , CONSTRAINT boolean_inbuilt
        CHECK (inbuilt IN (0, 1))
);

CREATE TABLE NutrientValue (
      id                   INTEGER PRIMARY KEY ASC
    , nutrient_id          INTEGER NOT NULL
    , food_id              INTEGER NOT NULL
    , value                REAL NOT NULL
    , unit_id              INTEGER NOT NULL
    , create_time          INTEGER NOT NULL DEFAULT 0
    , modify_time          INTEGER NOT NULL DEFAULT 0

    , CONSTRAINT valid_nutrient
        FOREIGN KEY (nutrient_id)
        REFERENCES Nutrient (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
    , CONSTRAINT valid_food
        FOREIGN KEY (food_id)
        REFERENCES Food (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
    , CONSTRAINT single_nutrient_per_food
        -- TODO this might change if versions are added
        UNIQUE (food_id, nutrient_id)
    , CONSTRAINT valid_unit
        -- have to make sure that unit has the correct type in code/trigger
        FOREIGN KEY (unit_id)
        REFERENCES Unit (id)
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
