package com.machfour.macros.cli;

abstract class CommandImpl implements Command {
    @Override
    public abstract String name();
    @Override
    public String toString() {
        return name();
    }
    @Override
    public boolean isUserCommand() {
        return !name().startsWith("_");
    }
}
