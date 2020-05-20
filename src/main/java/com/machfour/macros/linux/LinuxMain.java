package com.machfour.macros.linux;

import com.machfour.macros.cli.Command;
import com.machfour.macros.cli.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LinuxMain {

    // XXX hacky - also needs to be an absolute path
    private static void checkDbLocationOverride(List<String> args, LinuxConfig config) {
        String flagString = "--db=";
        Iterator<String> argIt = args.iterator();

        while (argIt.hasNext()) {
            String s = argIt.next();
            int dbArg = s.indexOf(flagString);
            if (dbArg == 0) {
                config.setDbLocation(s.substring(flagString.length()));
                argIt.remove(); // remove from arguments
                break;
            }
        }
    }

    public static void main(String[] args) {
        // To insert a pause (until user presses Enter):
        //try { System.in.read(); } catch (IOException e) { /* do nothing */ }

        final LinuxConfig config = new LinuxConfig();

        // give the SQLite JDBC driver an extracted version of the native lib, otherwise it auto-extracts each time
        System.setProperty("org.sqlite.lib.path", LinuxConfig.SQLITE_NATIVE_LIB_DIR);
        System.setProperty("org.sqlite.lib.name", LinuxConfig.SQLITE_NATIVE_LIB_NAME);

        assert args.length == 0 || args[0] != null;

        // set up all the file paths
        Commands.initCommands(config);
        Command c = args.length == 0 ? Commands.noArgsCommand() : Commands.parseCommand(args[0]);

        List<String> argList = new ArrayList<>(Arrays.asList(args)); // make it mutable

        checkDbLocationOverride(argList, config);

        // command args start from index 1
        int retcode = c.doAction(argList);
        System.exit(retcode);
    }
}
