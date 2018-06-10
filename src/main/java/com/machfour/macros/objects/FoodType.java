package com.machfour.macros.objects;

import java.util.NoSuchElementException;

public enum FoodType {
    PRIMARY("primary"), COMPOSITE("composite"), USDA("usda"), NUTTAB("nuttab"), SPECIAL("special");

    final String name;

    FoodType(String name) {
        this.name = name;
    }

    public static FoodType fromString(String name) {
        switch (name) {
            case "primary":
                return PRIMARY;
            case "composite":
                return COMPOSITE;
            case "usda":
                return USDA;
            case "nuttab":
                return NUTTAB;
            case "special":
                return SPECIAL;
            default:
                throw new NoSuchElementException("No FoodType with name '" + name + "'.");
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
