INSERT INTO QuantityUnit (id, name, abbreviation, is_volume_unit, metric_equivalent) VALUES
      (1, 'grams', 'g', 0, 1)
    , (2, 'millilitres', 'ml', 1, 1)
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
