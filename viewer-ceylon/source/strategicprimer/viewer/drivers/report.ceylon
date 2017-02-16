import controller.map.drivers {
    DriverFailedException
}
import controller.map.misc {
    ICLIHelper
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import java.nio.file {
    JPath=Path, JPaths=Paths, JFiles=Files
}
import java.util {
    JOptional=Optional
}
import model.map {
    IMapNG,
    Player
}
import ceylon.interop.java {
    CeylonIterable
}
import java.io {
    OutputStream,
    IOException,
    IOError
}
import ceylon.file {
    parsePath,
    Nil
}
import controller.map.report {
    ReportGenerator
}
import controller.map.report.tabular {
    TableReportGenerator
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
        void writeReport(JOptional<JPath> maybeFilename, IMapNG map) {
            if (maybeFilename.present) {
                JPath filename = maybeFilename.get();
                Player player;
                // FIXME: If parsing fails or there isn't a matching player, log a warning
                if (options.hasOption("--player"),
                        is Integer playerNum =
                                Integer.parse(options.getArgument("--player")),
                        exists temp = CeylonIterable(map.players()).find((item) =>
                            item.playerId == playerNum)) {
                    player = temp;
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
                        writer.write(ReportGenerator.createReport(map, player));
                    }
                }
            } else {
                log.error("Asked to make report from map with no filename");
            }
        }
        if (is IMultiMapModel model) {
            for (pair in model.allMaps) {
                writeReport(pair.second(), pair.first());
            }
        } else {
            writeReport(model.mapFile, model.map);
        }
    }
}
"A driver to produce tabular (CSV) reports of the contents of a player's map."
object tabularReportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage(false, "-b", "--tabular",
        ParamCount.atLeastOne, "Tabular Report Generator",
        "Produce CSV reports of the contents of a map.");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        OutputStream(String) filenameFunction(JPath base) {
            return (String string) =>
                JFiles.newOutputStream(
                    base.resolveSibling("``base.fileName``.``string``.csv"));
        }
        void createReports(IMapNG map, JOptional<JPath> file) {
            if (file.present) {
                JPath mapFile = file.get();
                try {
                    TableReportGenerator.createReports(map, filenameFunction(mapFile));
                } catch (IOException|IOError except) {
                    throw DriverFailedException(except);
                }
            } else {
                log.error("Asked to create reports from map with no filename");
            }
        }
        if (is IMultiMapModel model) {
            for (pair in model.allMaps) {
                createReports(pair.first(), pair.second());
            }
        } else {
            createReports(model.map, model.mapFile);
        }
    }
}