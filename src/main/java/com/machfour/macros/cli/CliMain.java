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

    private static final Command IMPORT = new Import();
    private static final Command INIT = new Init();
    private static final Command EDIT = new Edit();
    private static final Command READ = new Read();
    private static final Command HELP = new Help();
    private static final Command NEWMEAL = new NewMeal();
    private static final Command SEARCH = new SearchFood();
    private static final Command LISTFOOD = new ListFood();
    private static final Command TOTAL = new Total();
    private static final Command PORTION = new Portion();
    private static final Command NO_ARGS = new NoArgs();
    private static final Command INVALID_COMMAND = new InvalidCommand();
    static final Command[] COMMANDS = {
            EDIT
            , IMPORT
            , INIT
            , LISTFOOD
            , PORTION
            , NEWMEAL
            , READ
            , SEARCH
            , TOTAL
            , HELP
            , INVALID_COMMAND
            , NO_ARGS
    };
    static final Map<String, Command> CMDS_BY_NAME;

    static {
        CMDS_BY_NAME = new HashMap<>();
        for (Command m : COMMANDS) {
            assert !CMDS_BY_NAME.containsKey(m.name()): "Two commands have the same name";
            CMDS_BY_NAME.put(m.name(), m);
        }
    }

    static class InvalidCommand extends CommandImpl {
        private static final String NAME = "_invalidCommand";
        @Override
        public String name() {
            return NAME;
        }
        @Override
        public void doAction(List<String> args) {
            OUT.printf("Command not recognised: '%s'\n", args.get(0));
            OUT.println();
            NO_ARGS.doAction(Collections.emptyList());
        }
    }
    static class NoArgs extends CommandImpl {
        private static final String NAME = "_noArgs";
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public void doAction(List<String> args) {
            OUT.println("Please specify one of the following commands:");
            for (Command m : COMMANDS) {
                if (m.isUserCommand()) {
                    OUT.println(m.name());
                }
            }
        }
    }

    private static @NotNull Command parseCommand(String modeString) {
        Command defaultCommand = INVALID_COMMAND;
        if (modeString == null) {
            return defaultCommand;
        }
        String cleanedCmdString = modeString.trim();
        if (cleanedCmdString.startsWith("--")) {
            cleanedCmdString = cleanedCmdString.substring(2);
        }
        return CMDS_BY_NAME.getOrDefault(cleanedCmdString, defaultCommand);
    }

    public static void main(String[] args) {
        //try { System.in.read(); } catch (IOException e) { /* do nothing */ }

        // tell the SQLite JDBC driver where I've put the library. Otherwise it auto-extracts each time
        System.setProperty("org.sqlite.lib.path", "/home/max/devel/macros-java/lib");
        System.setProperty("org.sqlite.lib.name", "libsqlitejdbc.so");

        Command c = args.length == 0 ? NO_ARGS : parseCommand(args[0]);
        // command args start from index 1
        c.doAction(Arrays.asList(args));
    }
}
