import controller.map.drivers {
    SimpleCLIDriver,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    DriverFailedException,
    SimpleDriver
}
import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    WindowCloser
}
import model.misc {
    IDriverModel
}
import model.exploration {
    IExplorationModel,
    ExplorationModel
}
import java.io {
    IOException
}
import view.exploration {
    ExplorationCLI,
    ExplorationFrame
}
import javax.swing {
    SwingUtilities
}
import view.util {
    AboutDialog,
    SystemOut
}
import model.exploration.old {
    ExplorationRunner,
    EncounterTable
}
import lovelace.util.common {
    todo
}
import controller.exploration {
    TableLoader
}
import ceylon.collection {
    HashSet,
    MutableSet
}
import java.lang {
    ObjectArray, JString=String
}
"A CLI to help running exploration."
object explorationCLI satisfies SimpleCLIDriver {
    DriverUsage usageObject = DriverUsage(false, "-x", "--explore", ParamCount.atLeastOne,
        "Run exploration.",
        "Move a unit around the map, updating the player's map with what it sees.");
    usageObject.addSupportedOption("--current-turn=NN");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        try {
            ExplorationCLI eCLI = ExplorationCLI(explorationModel, cli);
            if (exists player = eCLI.choosePlayer(), exists unit = eCLI.chooseUnit(player)) {
                explorationModel.selectUnit(unit);
                eCLI.moveUntilDone();
            }
        } catch (IOException except) {
            throw DriverFailedException("I/O error interacting with user", except);
        }
    }
}
"An object to start the exploration GUI."
object explorationGUI satisfies SimpleDriver {
    DriverUsage usageObject = DriverUsage(true, "-x", "--explore", ParamCount.atLeastOne,
        "Run exploration.",
        "Move a unit around the map, updating the player's map with what it sees.");
    usageObject.addSupportedOption("--current-turn=NN");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(explorationModel, options, cli), "load", "save",
            "save as", "new", "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer");
        menuHandler.register((event) => process.exit(0), "quit");
        SwingUtilities.invokeLater(() {
            ExplorationFrame frame = ExplorationFrame(explorationModel, menuHandler);
            menuHandler.register(WindowCloser(frame), "close");
            menuHandler.register((event) =>
                AboutDialog(frame, frame.windowName).setVisible(true), "about");
            frame.setVisible(true);
        });
    }
}
"""A driver to help debug "exploration tables", which were the second "exploration results" framework
   I implemented."""
object tableDebugger satisfies SimpleCLIDriver {
    IDriverUsage usageObject = DriverUsage(false, "-T", "--table-debug", ParamCount.none,
        "Debug old-model encounter tables",
        "See whether old-model encounter tables refer to a nonexistent table");
    ExplorationRunner runner = ExplorationRunner();
    TableLoader.loadAllTables("tables", runner);
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        log.warn("tableDebugger doesn't need a driver model");
        startDriver();
    }
    "Print all possible results from a table."
    void debugSingleTable(
            "The string to print before each result (passed from the calling table)"
            String before,
            "The string to print after each result (passed from the calling table)"
            String after,
            "The table to debug"
            EncounterTable table,
            "The name of that table"
            String tableName,
            "The stream to write to"
            Anything(String) ostream,
            "The set of tables already on the stack, to prevent infinite recursion"
            todo("Use plain {EncounterTable*} instead of a Set?")
            MutableSet<EncounterTable> set) {
        if (set.contains(table)) {
            ostream("table ``tableName`` is already on the stack, skipping ...");
            ostream("The cause was: ``before``#``tableName``#``after``");
            return;
        }
        set.add(table);
        for (item in table.allEvents()) {
            if (item.contains("#")) {
                // FIXME: This relies on java.lang.String.split(), not ceylon.lang.String
                ObjectArray<JString> parsed = item.split("#", 3);
                String callee = (parsed[1] else nothing).string;
                debugSingleTable("``before````parsed[0] else ""``",
                    "``parsed[2] else ""````after``", runner.getTable(callee), callee,
                    ostream, set);
            } else {
                ostream("``before````item````after``");
            }
        }
        set.remove(table);
    }
    todo("If a CLIHelper was passed in, write to it")
    shared actual void startDriver() {
        runner.verboseRecursiveCheck(SystemOut.sysOut);
        EncounterTable mainTable = runner.getTable("main");
        debugSingleTable("", "", mainTable, "main",
            (string) => SystemOut.sysOut.println(string), HashSet<EncounterTable>());
    }
}