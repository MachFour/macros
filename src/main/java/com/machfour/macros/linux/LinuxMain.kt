package com.machfour.macros.linux

import com.machfour.macros.cli.Commands
import kotlin.system.exitProcess

object LinuxMain {
    // XXX hacky - also needs to be an absolute path
    private fun checkDbLocationOverride(args: MutableList<String>, config: LinuxConfig) {
        val flagString = "--db="
        val argIt = args.iterator()
        while (argIt.hasNext()) {
            val s = argIt.next()
            val dbArg = s.indexOf(flagString)
            if (dbArg == 0) {
                config.dbLocation = s.substring(flagString.length)
                argIt.remove() // remove from arguments
                break
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // To insert a pause (until user presses Enter):
        //try { System.in.read(); } catch (IOException e) { /* do nothing */ }
        val config = LinuxConfig()

        // give the SQLite JDBC driver an extracted version of the native lib, otherwise it auto-extracts each time
        System.setProperty("org.sqlite.lib.path", LinuxConfig.SQLITE_NATIVE_LIB_DIR)
        System.setProperty("org.sqlite.lib.name", LinuxConfig.SQLITE_NATIVE_LIB_NAME)

        // set up all the file paths
        Commands.initCommands(config)
        val cmd = if (args.isEmpty()) Commands.noArgsCommand() else Commands.parseCommand(args[0])

        val argList = args.toMutableList() // make it mutable
        checkDbLocationOverride(argList, config)

        // command args start from index 1
        val retcode = cmd.doAction(argList)
        exitProcess(retcode)
    }
}