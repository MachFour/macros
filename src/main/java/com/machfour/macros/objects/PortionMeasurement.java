package com.machfour.macros.objects;

// abstracts QtyUnits and servings into a common interface that can be used to enter FoodPortions

public interface PortionMeasurement {

    QtyUnit baseUnit();

    // how many 1 of this 'measurement' is in the base unit
    double unitMultiplier();

    boolean isVolumeMeasurement();

    // whether this Measurement is actually a serving or a QtyUnit object
    // (dirty, I know)
    boolean isServing();

    String getName();


}

