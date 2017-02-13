import controller.map.drivers {
    SimpleDriver,
    DriverUsage,
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
    DriverUsage usageObject = DriverUsage(true, "-w", "--worker", ParamCount.atLeastOne,
        "Manage a player's workers in units",
        "Organize the members of a player's units.");
    usageObject.addSupportedOption("--current-turn=NN");
    usageObject.addSupportedOption("--print-empty");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options,
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
            startDriver(cli, options, WorkerModel(model));
        }
    }
}
"A command-line program to export a proto-strategy for a player from orders in a map."
object strategyExportCLI satisfies SimpleDriver {
    DriverUsage usageObject = DriverUsage(false, "-w", "--worker", ParamCount.one,
        "Export a proto-strategy",
        "Create a proto-strategy using orders stored in the map");
    usageObject.addSupportedOption("--current-turn=NN");
    usageObject.addSupportedOption("--print-empty");
    usageObject.addSupportedOption("--export=filename.txt");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options,
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
            startDriver(cli, options, WorkerModel(model));
        }
    }
}