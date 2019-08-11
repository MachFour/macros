package com.machfour.macros.cli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.PrintStream;

abstract class CommandImpl implements Command {

    @NotNull
    private final String name;
    @Nullable
    private final String usage;

    protected static PrintStream out = CliMain.OUT;
    protected static BufferedReader in = CliMain.IN;

    protected CommandImpl(@NotNull String name) {
        this(name, null);
    }
    protected CommandImpl(@NotNull String name, @Nullable String usage) {
        this.name = name;
        this.usage = usage;
    }

    protected void setPrintStream(@NotNull PrintStream newOut) {
        out = newOut;
    }

    protected void setInput(@NotNull BufferedReader newIn) {
        in = newIn;
    }

    @Override @NotNull
    public final String name() {
        return name;
    }

    @Override @NotNull
    public final String usage() {
        return (usage != null) ? usage : String.format("No help available for mode '%s'", name());
    }

    public void printHelp() {
        out.println(usage());
    }

    @Override
    public String toString() {
        return name();
    }
    @Override
    public boolean isUserCommand() {
        return !name().startsWith("_");
    }
}
