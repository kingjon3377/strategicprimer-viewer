import ceylon.file {
    parsePath,
    Nil,
    File,
    Path
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
    DriverFailedException,
    ICLIHelper
}
import strategicprimer.model.map {
    IMapNG,
    Player
}
import strategicprimer.report {
    createReport,
    createTabularReports
}
"A driver to produce a report of the contents of a map."
object reportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-m";
        longOption = "--map";
        paramsWanted = ParamCount.one;
        shortDescription = "Report Generator";
        longDescription = "Produce HTML report of the contents of a map";
        supportedOptionsTemp = [
            ("\\" == operatingSystem.fileSeparator) then
                "--out=C:\\path\\to\\output.html"
                else "--out=/path/to/output.html",
            "--player=NN"
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
                                // TODO: create new instead?
                                player = map.currentPlayer;
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
"A driver to produce tabular (CSV) reports of the contents of a player's map."
object tabularReportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage(false, "-b", "--tabular",
        ParamCount.atLeastOne, "Tabular Report Generator",
        "Produce CSV reports of the contents of a map.");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        Anything(String)(String) filenameFunction(Path base) {
            assert (exists baseName = base.elements.terminal(1).first);
            Anything(String) retval(String tableName) {
                File file;
                switch (temp = base.siblingPath("``baseName``.``tableName``.csv"))
                case (is File) {
                    file = temp;
                }
                case (is Nil) {
                    file = temp.createFile();
                }
                else {
                    throw IOException("``base`` exists but is not a file");
                }
                value writer = file.Overwriter();
                return writer.write;
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
    }
    "Since this is a CLI driver, we can't show a file-chooser dialog."
    shared actual {JPath*} askUserForFiles() => {};
}