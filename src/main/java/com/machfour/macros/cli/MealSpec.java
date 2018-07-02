package com.machfour.macros.cli;

import com.machfour.macros.objects.Meal;
import com.machfour.macros.util.DateStamp;

class MealSpec {
    String name;
    DateStamp day;
    // whether parameters were actually given (true) or the default value used (false)
    boolean mealSpecified;
    boolean daySpecified;
    Meal createdObject;
    String error;
}
