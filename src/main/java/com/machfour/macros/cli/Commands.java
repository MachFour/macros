package com.machfour.macros.cli;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Helper class that holds static instances of all the other commands
 */
public class Commands {
    static final Map<String, Command> CMDS_BY_NAME;
    private static final Command RECIPES = new Recipe();
    private static final Command DELETEFOOD = new DeleteFood();
    private static final Command IMPORT = new Import();
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
    static final Command[] COMMANDS = {
            EDIT
            , IMPORT
            , INIT
            , SHOWFOOD
            , DELETEFOOD
            , PORTION
            , NEWMEAL
            , LISTMEALS
            , READ
            , SEARCH
            , TOTAL
            , RECIPES
            , ALLFOODS
            , HELP
            , INVALID_COMMAND
            , NO_ARGS
    };

    static {
        CMDS_BY_NAME = new HashMap<>();
        for (Command m : Commands.COMMANDS) {
            assert !Commands.CMDS_BY_NAME.containsKey(m.name()): "Two commands have the same name";
            Commands.CMDS_BY_NAME.put(m.name(), m);
        }
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
