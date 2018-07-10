package com.machfour.macros.cli;

import java.io.PrintStream;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

class Help extends ModeImpl {
    private static final String NAME = "help";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        out.println("Max's nutrition calculator");
        out.println("Available modes:");
        for (Mode m : CliMain.MODES) {
            if (m.isUserMode()) {
                out.println(m.name());
            }
        }
        out.println();
        out.printf("For help using a particular mode, run %s %s <mode> or %s <mode> --help\n", PROGNAME, NAME, PROGNAME);
    }

    @Override
    public void doAction(List<String> args) {
        // help on a particular action
        if (args.size() >= 2 && CliMain.MODES_BY_NAME.containsKey(args.get(1))) {
            Mode forHelp = CliMain.MODES_BY_NAME.get(args.get(1));
            forHelp.printHelp(OUT);
        } else {
            printHelp(OUT);
        }
    }
}
