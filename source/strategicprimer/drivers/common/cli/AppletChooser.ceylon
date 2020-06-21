import ceylon.collection {
    MutableMap,
    HashMap
}
import lovelace.util.common {
    simpleMap
}
"A class to allow CLI drivers to present a menu of applets to the user."
shared class AppletChooser<AppletClass, AppletArgs=[]>(ICLIHelper cli, AppletClass* applets)
        given AppletClass satisfies Applet<AppletArgs> given AppletArgs satisfies Anything[] {
    Map<String, AppletClass> createCommandsMap() {
        MutableMap<String, AppletClass> retval = HashMap<String, AppletClass>();
        for (applet in applets) {
            for (command in applet.commands) {
                assert (!command in ["help", "quit", "?", "exit"]);
                assert (!command in retval.keys);
                retval[command] = applet;
            }
        }
        return simpleMap(*retval);
    }
    Map<String, AppletClass> commands = createCommandsMap();
    void usageMessage() {
        cli.println("The following commands are supported:");
        for (applet in applets) {
            cli.print(", ".join(applet.commands), ": ");
            cli.println(applet.description);
        }
        cli.println("help, ?: Print this list of commands.");
        cli.println("quit, exit: Exit the program.");
        cli.print("Any string that is the beginning of only one command is also ");
        cli.println("accepted for that command.");
    }
    "Ask the user to choose an applet. If the user chooses an applet, return it. If the
     user chooses \"quit\" or \"exit\", or an EOF condition occurs, return [[false]]. If
     the user asks for the usage messge, print it and return [[true]]. If the user's input
     is ambiguous or does not match any applet, print the usage message and return
     [[null]."
    shared AppletClass|Boolean? chooseApplet() {
        String? command = cli.inputString("Command:")?.lowercased;
        if (exists command) {
            {<String->AppletClass>*} matches =
                commands.filterKeys(shuffle(String.startsWith)(command));
            if ("quit".startsWith(command) || "exit".startsWith(command)) {
                if (matches.empty) {
                    return false;
                } else {
                    cli.println("That command was ambiguous between the following:");
                    cli.println(", ".join(["quit", "exit"]
                        .filter(shuffle(String.startsWith)(command))
                        .chain(matches.map(Entry.key))));
                    usageMessage();
                    return null;
                }
            } else if ("?" == command) {
                usageMessage();
                return true;
            } if ("help".startsWith(command)) {
                if (matches.empty) {
                    usageMessage();
                    return true;
                } else {
                    cli.println("That command was ambiguous between the following:");
                    cli.print("help, ");
                    cli.println(", ".join(matches.map(Entry.key)));
                    usageMessage();
                    return null;
                }
            }
            if (!matches.rest.empty) {
                cli.println("That command was ambiguous between the following: ");
                cli.println(", ".join(matches.map(Entry.key)));
                usageMessage();
                return null;
            } else if (exists first = matches.first) {
                return first.item;
            } else {
                cli.println("Unknown command.");
                usageMessage();
                return null;
            }
        } else {
            return false;
        }
    }
}
