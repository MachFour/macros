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

    private static final Mode IMPORT = new Import();
    private static final Mode INIT = new Init();
    private static final Mode EDIT = new Edit();
    private static final Mode READ = new Read();
    private static final Mode HELP = new Help();
    private static final Mode SEARCH = new SearchFood();
    private static final Mode LISTFOOD = new ListFood();
    private static final Mode TOTAL = new Total();
    private static final Mode PORTION = new Portion();
    private static final Mode NO_ARGS = new NoArgs();
    private static final Mode INVALID_MODE = new InvalidMode();
    static final Mode[] MODES = {
              EDIT
            , IMPORT
            , INIT
            , LISTFOOD
            , PORTION
            , READ
            , SEARCH
            , TOTAL
            , HELP
            , INVALID_MODE
            , NO_ARGS
    };
    static final Map<String, Mode> MODES_BY_NAME;

    static {
        MODES_BY_NAME = new HashMap<>();
        for (Mode m : MODES) {
            assert !MODES_BY_NAME.containsKey(m.name()): "Two modes have the same name";
            MODES_BY_NAME.put(m.name(), m);
        }
    }

    static class InvalidMode extends ModeImpl {
        private static final String NAME = "_invalidMode";
        @Override
        public String name() {
            return NAME;
        }
        @Override
        public void doAction(List<String> args) {
            OUT.printf("Mode not recognised: '%s'\n", args.get(0));
            OUT.println();
            NO_ARGS.doAction(Collections.emptyList());
        }
    }
    static class NoArgs extends ModeImpl {
        private static final String NAME = "_noArgs";
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public void doAction(List<String> args) {
            OUT.println("Please specify one of the following modes:");
            for (Mode m : MODES) {
                if (m.isUserMode()) {
                    OUT.println(m.name());
                }
            }
        }
    }

    private static @NotNull Mode parseMode(String modeString) {
        Mode defaultMode = INVALID_MODE;
        if (modeString == null) {
            return defaultMode;
        }
        String cleanedModeString = modeString.trim();
        if (cleanedModeString.startsWith("--")) {
            cleanedModeString = cleanedModeString.substring(2);
        }
        return MODES_BY_NAME.getOrDefault(cleanedModeString, defaultMode);
    }

    public static void main(String[] args) {
        //try { System.in.read(); } catch (IOException e) { /* do nothing */ }

        // tell the SQLite JDBC driver where I've put the library. Otherwise it auto-extracts each time
        System.setProperty("org.sqlite.lib.path", "/home/max/devel/macros-java/lib");
        System.setProperty("org.sqlite.lib.name", "libsqlitejdbc.so");

        Mode mode = args.length == 0 ? NO_ARGS : parseMode(args[0]);
        // mode args start from index 1
        mode.doAction(Arrays.asList(args));
    }
}
