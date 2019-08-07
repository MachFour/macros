package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
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

    // XXX hacky - also needs to be an absolute path
    private static void checkDbLocationOverride(List<String> args) {
        String flagString = "--db==";
        Iterator<String> argIt = args.iterator();

        while (argIt.hasNext()) {
            String s = argIt.next();
            int dbArg = s.indexOf(flagString);
            if (dbArg == 0) {
                Config.DB_LOCATION = s.substring(flagString.length());
                argIt.remove(); // remove from arguments
                break;
            }
        }
    }

    public static void main(String[] args) {
        //try { System.in.read(); } catch (IOException e) { /* do nothing */ }

        // tell the SQLite JDBC driver where I've put the library. Otherwise it auto-extracts each time
        System.setProperty("org.sqlite.lib.path", "/home/max/devel/macros-java/lib");
        System.setProperty("org.sqlite.lib.name", "libsqlitejdbc.so");

        List<String> argList = new ArrayList<>(Arrays.asList(args)); // make it mutable
        Command c = argList.isEmpty() ? Commands.NO_ARGS : parseCommand(argList.get(0));

        checkDbLocationOverride(argList);

        // command args start from index 1
        c.doAction(argList);
    }
}
