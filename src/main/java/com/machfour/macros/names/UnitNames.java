package com.machfour.macros.names;

import com.machfour.macros.objects.QtyUnit;

import org.jetbrains.annotations.NotNull;

/*
 * Interface to provide user-readable strings for units used in nutrition data
 */
public interface UnitNames {

    String getName(@NotNull QtyUnit unit);
    String getAbbr(@NotNull QtyUnit unit);

}
