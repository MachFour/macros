package com.machfour.macros.names;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.NutritionData;

/*
 * Interface to provide user-readable strings for Nutrition data columns
 */
public interface NutritionDataNames {

    public String getName(Column<NutritionData, Double> col);
}
