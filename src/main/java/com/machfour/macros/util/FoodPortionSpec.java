package com.machfour.macros.util;

import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.QtyUnit;

import java.util.Objects;

// records intermediate data during parsing of a food portion spec
public class FoodPortionSpec {
    public String foodIndexName;
    public boolean isServingMode;
    // for non-serving mode
    public double quantity;
    public QtyUnit unit;
    // for serving mode. servingName = "" means default serving
    public String servingName;
    public double servingCount;

    // context data
    public int lineIdx;
    // holds the completed food portion object, only created if no other errors were encountered
    public FoodPortion createdObject;
    // records an error at any stage of parsing
    public String error;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FoodPortionSpec)) {
            return false;
        }
        FoodPortionSpec fps = (FoodPortionSpec) o;
        return Objects.equals(foodIndexName, fps.foodIndexName)
                && isServingMode == fps.isServingMode
                && quantity == fps.quantity
                && Objects.equals(unit, fps.unit)
                && Objects.equals(servingName, fps.servingName)
                && servingCount == fps.servingCount;
        // other fields are included as they are (except lineIdx, which is metadata) able to be derived from the fields above
    }

    @Override
    public int hashCode() {
        return Objects.hash(foodIndexName, isServingMode, quantity, unit, servingName, servingCount);
    }
}
