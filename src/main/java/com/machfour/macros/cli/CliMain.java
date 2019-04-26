package com.machfour.macros.cli;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;

public class CliMain {
    static final String PROGNAME = "macros";
    static final PrintStream OUT = System.out;
    static final PrintStream ERR = System.err;
    static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));

    private static String cleanInput(@NotNull String s) {
        s = s.trim();
        return s.startsWith("--") ? s.substring(2) : s;
    }

    private static @NotNull Command parseCommand(@NotNull String userInput) {
        return Commands.CMDS_BY_NAME.getOrDefault(cleanInput(userInput), Commands.INVALID_COMMAND);
    }

    public static void main(String[] args) {
        //try { System.in.read(); } catch (IOException e) { /* do nothing */ }

        // tell the SQLite JDBC driver where I've put the library. Otherwise it auto-extracts each time
        System.setProperty("org.sqlite.lib.path", "/home/max/devel/macros-java/lib");
        System.setProperty("org.sqlite.lib.name", "libsqlitejdbc.so");

        Command c = args.length == 0 ? Commands.NO_ARGS : parseCommand(args[0]);
        // command args start from index 1
        c.doAction(Arrays.asList(args));
    }
}
