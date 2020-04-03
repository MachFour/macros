package com.machfour.macros.names;

import com.machfour.macros.objects.Unit;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/*
 * Interface to provide user-readable strings for units used in nutrition data
 */
public interface UnitNamer {

    @NotNull
    String getName(@NotNull Unit unit);
    @NotNull
    String getAbbr(@NotNull Unit unit);

    @NotNull
    Collection<Unit> availableUnits();

}
