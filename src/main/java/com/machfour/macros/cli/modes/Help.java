package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.Command;
import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.Commands;

import java.util.List;

import static com.machfour.macros.linux.Config.PROGNAME;

public class Help extends CommandImpl {
    private static final String NAME = "help";
    private static final String USAGE = String.format("%s %s <command>", PROGNAME, NAME);

    public Help() {
        super(NAME, USAGE);
    }

    @Override
    public void printHelp() {
        out.println("################################");
        out.println("## Max's nutrition calculator ##");
        out.println("################################");
        out.println();
        out.println("Available commands:");
        for (Command m : Commands.getCommands()) {
            if (m.isUserCommand()) {
                out.println(m.name());
            }
        }
        out.println();
        out.printf("For help using a particular command, run %s %s <command> or %s <command> --help\n", PROGNAME, NAME, PROGNAME);
    }

    @Override
    public int doAction(List<String> args) {
        // help on a particular action
        Command forHelp = args.size() >= 2 ? Commands.getCommandByName(args.get(1)) : null;
        if (forHelp != null) {
            forHelp.printHelp();
        } else {
            // either no command specified or no command with given name
            printHelp();
        }
        return 0;
    }
}
