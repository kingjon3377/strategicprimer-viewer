import controller.map.drivers {
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    SimpleDriver
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
    BufferedWriter
}
import ceylon.file {
    parsePath,
    Nil
}
import controller.map.report {
    ReportGenerator
}
"A driver to produce a report of the contents of a map."
object reportCLI satisfies SimpleDriver {
    DriverUsage usageObject = DriverUsage(false, "-m", "--map", ParamCount.one,
        "Report Generator", "Produce HTML report of the contents of a map");
    if ("\\" == operatingSystem.fileSeparator) {
        usageObject.addSupportedOption("--out=C:\\path\\to\\output.html");
    } else {
        usageObject.addSupportedOption("--out=/path/to/output.html");
    }
    usageObject.addSupportedOption("--player=NN");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options,
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