import ceylon.file {
    parsePath,
    Nil,
    File,
    Path,
    Writer
}

import java.io {
    IOException,
    IOError
}
import java.nio.file {
    JPath=Path,
    JPaths=Paths
}

import strategicprimer.drivers.common {
    IMultiMapModel,
    IDriverModel,
    ParamCount,
    SimpleDriver,
    IDriverUsage,
    SPOptions,
    DriverUsage,
    DriverFailedException
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.map {
    IMapNG,
    Player,
    PlayerImpl
}
import strategicprimer.report {
    createReport,
    createTabularReports,
    createGUITabularReports
}
import javax.swing {
    JTabbedPane
}
import java.awt {
    Dimension,
    Component
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    UtilityMenu
}
import ceylon.collection {
    HashMap,
    MutableMap
}
"A driver to produce a report of the contents of a map."
object reportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-m", "--map"]; // TODO: Not accurate
        paramsWanted = ParamCount.one;
        shortDescription = "Report Generator";
        longDescription = "Produce HTML report of the contents of a map";
        supportedOptionsTemp = [
            ("\\" == operatingSystem.fileSeparator) then
                "--out=C:\\path\\to\\output.html"
                else "--out=/path/to/output.html",
            "--player=NN", "--current-turn=NN"
        ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        void writeReport(JPath? filename, IMapNG map) {
            if (exists filename) {
                Player player;
                if (options.hasOption("--player")) {
                    value playerNum = Integer.parse(options.getArgument("--player"));
                    if (is Integer playerNum) {
                            if (exists temp = map.players
                                    .find((item) => item.playerId == playerNum)) {
                                player = temp;
                            } else {
                                log.warn("No player with that number");
                                player = PlayerImpl(playerNum, "");
                            }
                    } else {
                        log.warn("Non-numeric player", playerNum);
                        player = map.currentPlayer;
                    }
                } else {
                    player = map.currentPlayer;
                }
                String outString;
                JPath outPath;
                if (options.hasOption("--out")) {
                    outString = options.getArgument("--out");
                    outPath = JPaths.get(outString);
                } else {
                    outString = "``filename.fileName``.report.html";
                    outPath = filename.resolveSibling(outString);
                }
                value outPathCeylon = parsePath(outPath.toAbsolutePath().string);
                if (is Nil loc = outPathCeylon.resource) {
                    value file = loc.createFile();
                    try (writer = file.Overwriter()) {
                        writer.write(createReport(map, player));
                    }
                }
            } else {
                log.error("Asked to make report from map with no filename");
            }
        }
        if (is IMultiMapModel model) {
            for ([map, file] in model.allMaps) {
                writeReport(file, map);
            }
        } else {
            writeReport(model.mapFile, model.map);
        }
    }
    "As we're a CLI driver, we can't show a file-chooser dialog."
    shared actual {JPath*} askUserForFiles() => {};
}
"A driver to show tabular reports of the contents of a player's map in a GUI."
object tabularReportGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage(true, ["-b", "--tabular"],
        ParamCount.one, "Tabular Report Viewer",
        "Show the contents of a map in tabular form");
    suppressWarnings("expressionTypeNothing")
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        object window extends SPFrame("Tabular Report", model.mapFile, Dimension(640, 480)) {
            shared actual Boolean supportsDroppedFiles => false;
            shared actual String windowName => "Tabular Report";
        }
        JTabbedPane frame = JTabbedPane(JTabbedPane.top, JTabbedPane.scrollTabLayout);
        createGUITabularReports((String str, Component comp) => frame.addTab(str, comp),
            model.map);
        window.add(frame);
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register((event) => window.dispose(), "close");
        menuHandler.register((event) => aboutDialog(frame, window.windowName).setVisible(true), "about");
        menuHandler.register((event) => process.exit(0), "quit");
        window.jMenuBar = UtilityMenu(window);
        window.setVisible(true);
    }
    "Ask the user to choose a file."
    shared actual {JPath*} askUserForFiles() {
        try {
            return FileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
}
"A driver to produce tabular (CSV) reports of the contents of a player's map."
object tabularReportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage(false, ["-b", "--tabular"],
        ParamCount.atLeastOne, "Tabular Report Generator",
        "Produce CSV reports of the contents of a map.");
    MutableMap<String,Writer> writers = HashMap<String,Writer>();
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        Anything(String)(String) filenameFunction(Path base) {
            assert (exists baseName = base.elements.terminal(1).first);
            Anything(String) retval(String tableName) {
                if (exists writer = writers.get("``baseName``.``tableName``.csv")) {
                    return writer.write;
                } else {
                    File file;
                    switch (temp = base.siblingPath("``baseName``.``tableName``.csv").resource)
                    case (is File) {
                        file = temp;
                    }
                    case (is Nil) {
                        file = temp.createFile();
                    }
                    else {
                        throw IOException("``base``.``tableName``.csv exists but is not a file");
                    }
                    value writer = file.Overwriter();
                    writers["``baseName``.``tableName``.csv"] = writer;
                    return writer.write;
                }
            }
            return retval;
        }
        void createReports(IMapNG map, JPath? mapFile) {
            if (exists mapFile) {
                try {
                    createTabularReports(map,
                        filenameFunction(parsePath(mapFile.string)));
                } catch (IOException|IOError except) {
                    throw DriverFailedException(except);
                }
            } else {
                log.error("Asked to create reports from map with no filename");
            }
        }
        if (is IMultiMapModel model) {
            for ([map, file] in model.allMaps) {
                createReports(map, file);
            }
        } else {
            createReports(model.map, model.mapFile);
        }
        for (writer in writers.items) {
            writer.close();
        }
    }
    "Since this is a CLI driver, we can't show a file-chooser dialog."
    shared actual {JPath*} askUserForFiles() => {};
}
