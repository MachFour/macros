package com.machfour.macros.cli;

import java.io.PrintStream;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

class Help extends CommandImpl {
    private static final String NAME = "help";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        out.println("Max's nutrition calculator");
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
            forHelp.printHelp(OUT);
        } else {
            printHelp(OUT);
        }
    }
}
