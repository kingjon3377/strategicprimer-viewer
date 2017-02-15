import controller.map.drivers {
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions
}
import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    WindowCloser,
    FindHandler
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import model.viewer {
    IViewerModel,
    ViewerModel
}
import view.map.main {
    ZoomListener,
    ViewerFrame,
    SelectTileDialog
}
import javax.swing {
    SwingUtilities
}
import view.util {
    AboutDialog
}
"A driver to start the map viewer."
object viewerGUI satisfies SimpleDriver {
    DriverUsage usageObject = DriverUsage(true, "-m", "--map", ParamCount.one,
        "Map viewer", "Look at the map visually. This is probably the app you want.");
    usageObject.addSupportedOption("--current-turn=NN");
    shared actual IDriverUsage usage = usageObject;
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IViewerModel model) {
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(IOHandler(model, options, cli), "load", "save",
                "save as", "new", "load secondary", "save all", "open in map viewer",
                "open secondary map in map viewer");
            menuHandler.register((event) => process.exit(0), "quit");
            menuHandler.register((event) => model.zoomIn(), "zoom in");
            menuHandler.register((event) => model.zoomOut(), "zoom out");
            menuHandler.register(ZoomListener(model), "center");
            SwingUtilities.invokeLater(() {
                ViewerFrame frame = ViewerFrame(model, menuHandler);
                menuHandler.register(WindowCloser(frame), "close");
                menuHandler.register((event) =>
                    SelectTileDialog(frame, model).setVisible(true), "go to tile");
                menuHandler.register(FindHandler(frame, model), "find a fixture",
                    "find next");
                menuHandler.register((event) =>
                    AboutDialog(frame, frame.windowName).setVisible(true), "about");
                frame.setVisible(true);
            });
        } else if (is IMultiMapModel model) {
            for (map in model.allMaps) {
                startDriverOnModel(cli, options.copy(), ViewerModel(map));
            }
        } else {
            startDriverOnModel(cli, options, ViewerModel(model.map, model.mapFile));
        }
    }
}