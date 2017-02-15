import controller.map.drivers {
    ParamCount,
    IDriverUsage,
    SPOptions,
    DriverFailedException
}
import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    PlayerChangeMenuListener,
    WindowCloser,
    StrategyExporter
}
import model.misc {
    IDriverModel
}
import model.workermgmt {
    IWorkerModel,
    WorkerModel
}
import javax.swing {
    SwingUtilities
}
import view.worker {
    WorkerMgmtFrame
}
import view.util {
    AboutDialog
}
import java.nio.file {
    JPaths = Paths
}
import ceylon.interop.java {
    JavaIterable
}
import model.map.fixtures {
    UnitMember
}
import java.lang {
    IllegalStateException
}
"A driver to start the worker management GUI."
object workerGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-w";
        longOption = "--worker";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Manage a player's workers in units";
        longDescription = "Organize the members of a player's units.";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(IOHandler(model, options, cli), "load", "save",
                "save as", "new", "load secondary", "save all", "open in map viewer",
                "open secondary map in map viewer");
            PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
            menuHandler.register(pcml, "change current player");
            menuHandler.register((event) => process.exit(0), "quit");
            SwingUtilities.invokeLater(() {
                WorkerMgmtFrame frame = WorkerMgmtFrame(options, model, menuHandler);
                pcml.addPlayerChangeListener(frame);
                menuHandler.register((event) => frame.playerChanged(
                        model.map.currentPlayer, model.map.currentPlayer),
                    "reload tree");
                menuHandler.register(WindowCloser(frame), "close");
                menuHandler.register((event) =>
                    AboutDialog(frame, frame.windowName).setVisible(true), "about");
                frame.setVisible(true);
            });
        } else {
            startDriverOnModel(cli, options, WorkerModel(model));
        }
    }
}
"A command-line program to export a proto-strategy for a player from orders in a map."
object strategyExportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-w";
        longOption = "--worker";
        paramsWanted = ParamCount.one;
        shortDescription = "Export a proto-strategy";
        longDescription = "Create a proto-strategy using orders stored in the map";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty", "--export=filename.txt" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            if (options.hasOption("--export")) {
                StrategyExporter(model).writeStrategy(JPaths.get(
                        options.getArgument("--export")), options,
                    JavaIterable<UnitMember>({}));
            } else {
                throw DriverFailedException("--export option is required",
                    IllegalStateException("--export option is required"));
            }
        } else {
            startDriverOnModel(cli, options, WorkerModel(model));
        }
    }
}