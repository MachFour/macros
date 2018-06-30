package com.machfour.macros.cli;

import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.QtyUnit;

// records intermediate data during parsing of a food portion spec
class FoodPortionSpec {
    String foodIndexName;
    boolean isServingMode;
    // for non-serving mode
    double quantity;
    QtyUnit unit;
    // for serving mode. servingName = "" means default serving
    String servingName;
    double servingCount;
    int lineIdx;

    // holds the completed food portion object, only created if no other errors were encountered
    FoodPortion createdObject;
    // records an error at any stage of parsing
    String error;
}
