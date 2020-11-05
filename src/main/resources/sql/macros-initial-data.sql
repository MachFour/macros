INSERT INTO Unit (id, type_id, name, abbreviation, metric_equivalent, inbuilt) VALUES
      (0, 1, 'grams', 'g', 1, 1)
    , (1, 1, 'milligrams', 'mg', 0.001, 1)
    , (2, 2, 'millilitres', 'ml', 1, 1)
    , (3, 2, 'litres', 'L', 1000, 0)
    , (4, 3, 'kilojoules', 'kj', 1, 1)
    , (5, 3, 'calories', 'kcal', 4.186, 1)
;

INSERT INTO Nutrient (id, name, unit_types, inbuilt) VALUES
      ( 0, "quantity", 3, 1)
    , ( 1, "energy", 4, 1)
    , ( 2, "protein", 1, 1)
    , ( 3, "carbohydrate", 1, 1)
    , ( 4, "carbohydrate_by_diff", 1, 1)
    , ( 5, "sugar", 1, 1)
    , ( 6, "sugar_alcohol", 1, 1)
    , ( 7, "starch", 1, 1)
    , ( 8, "fat", 1, 1)
    , ( 9, "saturated_fat", 1, 1)
    , (10, "monounsaturated_fat", 1, 1)
    , (11, "polyunsaturated_fat", 1, 1)
    , (12, "omega_3", 1, 1)
    , (13, "omega_6", 1, 1)
    , (14, "fibre", 1, 1)
    , (15, "sodium", 1, 1)
    , (16, "salt", 1, 0)
    , (17, "potassium", 1, 1)
    , (18, "calcium", 1, 1)
    , (19, "iron", 1, 1)
    , (20, "water", 1, 1)
    , (21, "alcohol", 1, 1)
    , (22, "caffeine", 1, 1)
;


--INSERT INTO FoodType (id, name) VALUES
--      (1, 'local')
--    , (2, 'composite')
--    , (3, 'USDA')
--    , (4, 'NUTTAB')
--;

--INSERT INTO MealDescription (name) VALUES
--      ('breakfast')
--    , ('brunch')
--    , ('lunch')
--    , ('dinner')
--    , ('snack')
--    , ('morning tea')
--    , ('afternoon tea')
--    , ('exercise')
--    , ('other')
--;

INSERT INTO FoodCategory (name) VALUES
      ('alcoholic beverages')
    , ('biscuits, crackers and chips')
    , ('chocolate')
    , ('condiments')
    , ('dairy')
    , ('desserts')
    , ('fish')
    , ('fruit and vegetables')
    , ('grains')
    , ('juices')
    , ('legumes')
    , ('meat')
    , ('miscellaneous')
    , ('nuts and seeds')
    , ('poultry and eggs')
    , ('preserved or dried foods')
    , ('recipes')
    , ('sauces, dips and spreads')
    , ('soft drinks')
    , ('soy products')
    , ('uncategorised')
;

-- vim: ts=4 et
