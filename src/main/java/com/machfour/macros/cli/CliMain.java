package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;

public class CliMain {
    static final PrintStream OUT = System.out;
    static final PrintStream ERR = System.err;
    static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));

    // XXX hacky - also needs to be an absolute path
    private static void checkDbLocationOverride(List<String> args) {
        String flagString = "--db=";
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
        // To insert a pause (until user presses Enter):
        //try { System.in.read(); } catch (IOException e) { /* do nothing */ }

        // tell the SQLite JDBC driver where I've put the library. Otherwise it auto-extracts each time
        System.setProperty("org.sqlite.lib.path", "/home/max/devel/macros-java/lib");
        System.setProperty("org.sqlite.lib.name", "libsqlitejdbc.so");

        assert args.length == 0 || args[0] != null;

        Command c = args.length == 0 ? Commands.noArgsCommand() : Commands.parseCommand(args[0]);
        List<String> argList = new ArrayList<>(Arrays.asList(args)); // make it mutable

        checkDbLocationOverride(argList);

        // command args start from index 1
        int retcode = c.doAction(argList);
        System.exit(retcode);
    }
}
