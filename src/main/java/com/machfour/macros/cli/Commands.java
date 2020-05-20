package com.machfour.macros.cli;

import com.machfour.macros.cli.modes.*;
import com.machfour.macros.core.MacrosConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/*
 * Helper class that holds static instances of all the other commands
 */
public class Commands {
    // allows initialisation of command objects using a loop of constructors
    private interface CommandConstructor {
        Command newInstance();
    }
    private static final Map<String, Command> CMDS_BY_NAME = new LinkedHashMap<>();

    private static final CommandConstructor[] COMMAND_CONSTRUCTORS = {
              Help::new
            , Edit::new
            , Meals::new
            , ShowFood::new
            , AddFood::new
            , DeleteFood::new
            , Portion::new
            , NewMeal::new
            , Read::new
            , SearchFood::new
            , Total::new
            , Recipe::new
            , AllFoods::new
            , Import::new
            , Export::new
            , Restore::new
            , Init::new
              // hidden ones - command name is prepended with underscore
            , InvalidCommand::new
            , NoArgs::new
    };

    private static boolean initialised = false;

    // and then init commands
    public static void initCommands(@NotNull MacrosConfig config) {
        CommandImpl.setConfig(config);
        for (CommandConstructor cc : COMMAND_CONSTRUCTORS) {
            Command c = cc.newInstance();
            assert !CMDS_BY_NAME.containsKey(c.name()): "Two commands have the same name";
            CMDS_BY_NAME.put(c.name(), c);
        }
        initialised = true;
    }

    private static void checkInitialised() {
        if (!initialised) {
            throw new IllegalStateException("Commands not initialised");
        }
    }

    @Nullable
    public static Command getCommandByName(String name) {
        checkInitialised();
        return CMDS_BY_NAME.getOrDefault(name, null);
    }

    private static String cleanInput(@NotNull String s) {
        s = s.trim();
        return s.startsWith("--") ? s.substring(2) : s;
    }

    public static List<Command> getCommands() {
        checkInitialised();
        return new ArrayList<>(CMDS_BY_NAME.values());
    }

    /*
     * Parses the name of a command from the first element of args
     */
    @NotNull
    public static Command parseCommand(@NotNull String cmdArg) {
        checkInitialised();
        return CMDS_BY_NAME.getOrDefault(cleanInput(cmdArg), invalidCommand());
    }

    @NotNull
    public static Command noArgsCommand() {
        checkInitialised();
        Command noArgs = getCommandByName(NoArgs.NAME);
        assert noArgs != null;
        return noArgs;
    }

    @NotNull
    public static Command invalidCommand() {
        checkInitialised();
        Command invalid = getCommandByName(InvalidCommand.NAME);
        assert invalid != null;
        return invalid;
    }

    private Commands() {}

    /* Some more miscellaneous commands that don't (yet?) warrant their own class */

    private static class InvalidCommand extends CommandImpl {
        private static final String NAME = "_invalidCommand";

        InvalidCommand() {
            super(NAME);
        }

        @Override
        public int doAction(List<String> args) {
            out.printf("Command not recognised: '%s'\n\n", args.get(0));
            return noArgsCommand().doAction(Collections.emptyList());
        }
    }

    private static class NoArgs extends CommandImpl {
        private static final String NAME = "_noArgs";

        NoArgs() {
            super(NAME);
        }

        @Override
        public int doAction(List<String> args) {
            out.println("Please specify one of the following commands:");
            for (Command m : getCommands()) {
                if (m.isUserCommand()) {
                    out.println(m.name());
                }
            }
            return -1;
        }
    }
}
