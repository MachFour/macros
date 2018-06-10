package com.machfour.macros.cli;

abstract class ModeImpl implements Mode {
    @Override
    public abstract String name();
    @Override
    public String toString() {
        return name();
    }
    @Override
    public boolean isUserMode() {
        return !name().startsWith("_");
    }
}
