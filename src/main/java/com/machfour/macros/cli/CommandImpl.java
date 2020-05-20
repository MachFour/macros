package com.machfour.macros.cli;

import com.machfour.macros.core.MacrosConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

public abstract class CommandImpl implements Command {

    static void setConfig(MacrosConfig newConfig) {
        config = newConfig;
        out = config.outStream();
        err = config.errStream();
        in = config.inputReader();
    }

    protected static MacrosConfig config;
    protected static PrintStream out;
    protected static PrintStream err;
    protected static BufferedReader in;


    @NotNull
    private final String name;
    @Nullable
    private final String usage;

    protected CommandImpl(@NotNull String name) {
        this(name, null);
    }

    protected CommandImpl(@NotNull String name, @Nullable String usage) {
        this.name = name;
        this.usage = usage;
    }


    // can be overridden
    @Override
    public void doActionNoExitCode(List<String> args) {
        doAction(args);
    }

    @Override @NotNull
    public final String name() {
        return name;
    }

    @Override @NotNull
    public final String usage() {
        return (usage != null) ? usage : String.format("No help available for mode '%s'", name());
    }

    /*
     * Subclasses should override this to provide more detailed help than just the usage string
     */
    public void printHelp() {
        out.println(usage());
    }

    @Override
    public final String toString() {
        return name();
    }
    @Override
    public final boolean isUserCommand() {
        return isUserCommand(name());
    }

    // logic for deciding whether a command is user-facing
    static boolean isUserCommand(String name) {
        return !name.startsWith("_");
    }

}
