package com.machfour.macros.cli;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.machfour.macros.cli.CliMain.PROGNAME;

class Help extends CommandImpl {
    private static final String NAME = "help";
    private static final String USAGE = String.format("%s %s <command>", PROGNAME, NAME);

    Help() {
        super(NAME, USAGE);
    }

    @Override
    public void printHelp() {
        out.println("################################");
        out.println("## Max's nutrition calculator ##");
        out.println("################################");
        out.println();
        out.println("Available commands:");
        for (Command m : Commands.COMMANDS) {
            if (m.isUserCommand()) {
                out.println(m.name());
            }
        }
        out.println();
        out.printf("For help using a particular command, run %s %s <command> or %s <command> --help\n", PROGNAME, NAME, PROGNAME);
    }

    @Override
    public void doAction(List<String> args) {
        // help on a particular action
        if (args.size() >= 2 && Commands.CMDS_BY_NAME.containsKey(args.get(1))) {
            Command forHelp = Commands.CMDS_BY_NAME.get(args.get(1));
            forHelp.printHelp();
        } else {
            printHelp();
        }
    }
}
