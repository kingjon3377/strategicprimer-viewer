import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    WindowCloser
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
    SelectTileDialog,
    FindDialog
}
import javax.swing {
    SwingUtilities
}
import strategicprimer.viewer.about {
    aboutDialog
}
"A driver to start the map viewer."
object viewerGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-m";
        longOption = "--map";
        paramsWanted = ParamCount.one;
        shortDescription = "Map viewer";
        longDescription = "Look at the map visually. This is probably the app you want.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
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
                variable FindDialog? finder = null;
                FindDialog getFindDialog() {
                    if (exists temp = finder) {
                        return temp;
                    } else {
                        FindDialog local = FindDialog(frame, model);
                        finder = local;
                        return local;
                    }
                }
                menuHandler.register((event) => getFindDialog().setVisible(true),
                    "find a fixture");
                menuHandler.register((event) => getFindDialog().search(), "find next");
                menuHandler.register((event) =>
                    aboutDialog(frame, frame.windowName).setVisible(true), "about");
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