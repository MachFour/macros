package com.machfour.macros.cli;

import com.machfour.macros.cli.modes.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Helper class that holds static instances of all the other commands
 */
public class Commands {
    public static final Map<String, Command> CMDS_BY_NAME;

    private static final Command RECIPES = new Recipe();
    private static final Command ADDFOOD = new AddFood();
    private static final Command DELETEFOOD = new DeleteFood();
    private static final Command IMPORT = new Import();
    private static final Command EXPORT = new Export();
    private static final Command INIT = new Init();
    private static final Command EDIT = new Edit();
    private static final Command READ = new Read();
    private static final Command HELP = new Help();
    private static final Command NEWMEAL = new NewMeal();
    private static final Command SEARCH = new SearchFood();
    private static final Command SHOWFOOD = new ShowFood();
    private static final Command TOTAL = new Total();
    private static final Command PORTION = new Portion();
    private static final Command ALLFOODS = new AllFoods();
    private static final Command LISTMEALS = new Meals();

    static final Command NO_ARGS = new NoArgs();
    static final Command INVALID_COMMAND = new InvalidCommand();
    public static final Command[] COMMANDS = {
              HELP
            , EDIT
            , LISTMEALS
            , SHOWFOOD
            , ADDFOOD
            , DELETEFOOD
            , PORTION
            , NEWMEAL
            , READ
            , SEARCH
            , TOTAL
            , RECIPES
            , ALLFOODS
            , IMPORT
            , EXPORT
            , INIT
              // hidden ones - command name is prepended with underscore
            , INVALID_COMMAND
            , NO_ARGS
    };

    static {
        Map<String, Command> _cmdsByName = new HashMap<>();
        for (Command m : COMMANDS) {
            assert !_cmdsByName.containsKey(m.name()): "Two commands have the same name";
            _cmdsByName.put(m.name(), m);
        }
        CMDS_BY_NAME = Collections.unmodifiableMap(_cmdsByName);
    }


    private Commands() {}

    /* Some more miscellaneous commands that don't (yet?) warrant their own class */

    static class InvalidCommand extends CommandImpl {
        private static final String NAME = "_invalidCommand";

        InvalidCommand() {
            super(NAME);
        }

        @Override
        public void doAction(List<String> args) {
            out.printf("Command not recognised: '%s'\n\n", args.get(0));
            NO_ARGS.doAction(Collections.emptyList());
        }
    }

    static class NoArgs extends CommandImpl {
        private static final String NAME = "_noArgs";

        NoArgs() {
            super(NAME);
        }

        @Override
        public void doAction(List<String> args) {
            out.println("Please specify one of the following commands:");
            for (Command m : COMMANDS) {
                if (m.isUserCommand()) {
                    out.println(m.name());
                }
            }
        }
    }
}
