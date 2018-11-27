package com.machfour.macros.util;

import com.machfour.macros.objects.Meal;
import com.machfour.macros.util.DateStamp;

public class MealSpec {
    public String name;
    public DateStamp day;
    // whether parameters were actually given (true) or the default value used (false)
    public boolean mealSpecified;
    public boolean daySpecified;
    public Meal createdObject;
    public String error;
}
