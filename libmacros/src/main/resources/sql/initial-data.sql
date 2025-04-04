-- Note: must match Units.kt!
INSERT INTO Unit (id, type_id, name, abbreviation, metric_equivalent, inbuilt) VALUES
      (0, 1, 'grams', 'g', 1, 1)
    , (1, 1, 'milligrams', 'mg', 0.001, 1)
    , (2, 2, 'millilitres', 'ml', 1, 1)
    , (3, 2, 'litres', 'L', 1000, 0)
    , (4, 4, 'kilojoules', 'kj', 1, 1)
    , (5, 4, 'calories', 'kcal', 4.186, 1)
    , (6, 1, 'ounces', 'oz', 28.349523125, 1)
    , (7, 2, 'fluid ounces', 'fl oz', 29.5735295625, 1)
    ;


-- Note: must match Nutrients.kt!
INSERT INTO Nutrient (id, name, unit_types, inbuilt) VALUES
      ( 0, 'quantity', 3, 1)
    , ( 1, 'energy', 4, 1)
    , ( 2, 'protein', 1, 1)
    , ( 3, 'fat', 1, 1)
    , ( 4, 'saturated_fat', 1, 1)
    , ( 5, 'carbohydrate', 1, 1)
    , ( 6, 'sugar', 1, 1)
    , ( 7, 'fibre', 1, 1)
    , ( 8, 'sodium', 1, 1)
    , ( 9, 'potassium', 1, 1)
    , (10, 'calcium', 1, 1)
    , (11, 'iron', 1, 1)
    , (12, 'monounsaturated_fat', 1, 1)
    , (13, 'polyunsaturated_fat', 1, 1)
    , (14, 'omega_3', 1, 1)
    , (15, 'omega_6', 1, 1)
    , (16, 'starch', 1, 1)
    , (17, 'salt', 1, 1)
    , (18, 'water', 3, 1)
    , (19, 'carbohydrate_by_diff', 1, 1)
    , (20, 'alcohol', 3, 1)
    , (21, 'sugar_alcohol', 1, 1)
    , (22, 'caffeine', 1, 1)
    , (23, 'erythritol', 1, 1)
    , (24, 'glycerol', 1, 1)
    , (25, 'isomalt', 1, 1)
    , (26, 'lactitol', 1, 1)
    , (27, 'maltitol', 1, 1)
    , (28, 'mannitol', 1, 1)
    , (29, 'sorbitol', 1, 1)
    , (30, 'xylitol', 1, 1)
    ;


-- old data which has wrong unit types for water/alcohol
-- and wrong order compared to Nutrients.kt!!
--INSERT INTO Nutrient (id, name, unit_types, inbuilt) VALUES
--      ( 0, 'quantity', 3, 1)
--    , ( 1, 'energy', 4, 1)
--    , ( 2, 'protein', 1, 1)
--    , ( 3, 'carbohydrate', 1, 1)
--    , ( 4, 'carbohydrate_by_diff', 1, 1)
--    , ( 5, 'sugar', 1, 1)
--    , ( 6, 'sugar_alcohol', 1, 1)
--    , ( 7, 'starch', 1, 1)
--    , ( 8, 'fat', 1, 1)
--    , ( 9, 'saturated_fat', 1, 1)
--    , (10, 'monounsaturated_fat', 1, 1)
--    , (11, 'polyunsaturated_fat', 1, 1)
--    , (12, 'omega_3', 1, 1)
--    , (13, 'omega_6', 1, 1)
--    , (14, 'fibre', 1, 1)
--    , (15, 'sodium', 1, 1)
--    , (16, 'salt', 1, 1)
--    , (17, 'potassium', 1, 1)
--    , (18, 'calcium', 1, 1)
--    , (19, 'iron', 1, 1)
--    , (20, 'water', 1, 1)
--    , (21, 'alcohol', 1, 1)
--    , (22, 'caffeine', 1, 1)
--    , (23, 'erythritol', 1, 1)
--    , (24, 'glycerol', 1, 1)
--    , (25, 'isomalt', 1, 1)
--    , (26, 'lactitol', 1, 1)
--    , (27, 'maltitol', 1, 1)
--    , (28, 'mannitol', 1, 1)
--    , (29, 'sorbitol', 1, 1)
--    , (30, 'xylitol', 1, 1)
--;


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
    , ('pasta')
    , ('poultry and eggs')
    , ('preserved or dried foods')
    , ('recipes')
    , ('sauces, dips and spreads')
    , ('soft drinks')
    , ('soy products')
    , ('sweets')
    , ('uncategorised')
    ;

-- vim: ts=4 et
