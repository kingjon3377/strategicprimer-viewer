import controller.map.drivers {
    SimpleDriver,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions
}
import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    PlayerChangeMenuListener,
    WindowCloser
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