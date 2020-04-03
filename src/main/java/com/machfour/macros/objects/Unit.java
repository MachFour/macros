package com.machfour.macros.objects;

import org.jetbrains.annotations.NotNull;

/*
 * Unit of measuring something, be that quantity, or a nutrient.
 */
public interface Unit {
    @NotNull
    String name();

    @NotNull
    String abbr();
}
