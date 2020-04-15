package com.machfour.macros.cli;

import com.machfour.macros.cli.CliMain;
import com.machfour.macros.cli.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.security.cert.CertificateRevokedException;
import java.util.List;

public abstract class CommandImpl implements Command {

    @NotNull
    private final String name;
    @Nullable
    private final String usage;

    protected final PrintStream out;
    protected final PrintStream err;
    protected final BufferedReader in;

    protected CommandImpl(@NotNull String name) {
        this(name, null);
    }
    protected CommandImpl(@NotNull String name, @Nullable String usage) {
        this(name, usage, CliMain.OUT, CliMain.ERR, CliMain.IN);
    }

    private CommandImpl(@NotNull String name, @Nullable String usage, PrintStream out, PrintStream err, BufferedReader in) {
        this.name = name;
        this.usage = usage;
        this.out = out;
        this.err = err;
        this.in = in;
    }

    // can be overridden
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
    public String toString() {
        return name();
    }
    @Override
    public boolean isUserCommand() {
        return !name().startsWith("_");
    }
}
