import ceylon.logging {
    logger,
    Logger
}

import java.io {
    FileNotFoundException,
    IOException
}
import java.nio.file {
    NoSuchFileException,
    Paths
}

import javax.xml.stream {
    XMLStreamException
}

import strategicprimer.drivers.common {
    DriverUsage,
    ParamCount,
    UtilityDriver,
    IDriverUsage,
    SPOptions
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.map {
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.xmlio {
    readMap,
    warningLevels,
    writeMap,
    SPFormatException
}
import ceylon.file {
    parsePath
}
"A logger."
Logger log = logger(`module strategicprimer.drivers.converters`);
"A driver to convert maps: at present, halving their resolution."
class ConverterDriver(
        """Set to true when the provided [[ICLIHelper]] is connected to a graphical window
           instead of standard output."""
        Boolean gui = false) satisfies UtilityDriver {
    "The usage object."
    shared actual IDriverUsage usage = DriverUsage {
        graphical = gui;
        shortOption = "-v";
        longOption = "--convert";
        paramsWanted = ParamCount.one;
        shortDescription = "Convert a map's format";
        longDescription = "Convert a map. At present, this means reducing its
                           resolution.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    "Run the driver."
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        if (nonempty arguments = args.coalesced.sequence()) {
            for (filename in arguments) {
                cli.print("Reading ``filename ``... ");
                try {
                    IMutableMapNG old = readMap(Paths.get(filename),
                        warningLevels.default);
                    if (options.hasOption("--current-turn")) {
                        value currentTurn =
                                Integer.parse(options.getArgument("--current-turn"));
                        if (is Integer currentTurn) {
                            old.currentTurn = currentTurn;
                        } else {
                            log.error(
                                "Current turn passed as an option must be an integer",
                                currentTurn);
                        }
                    }
                    cli.println(" ... Converting ... ");
                    IMapNG map = decreaseResolution(old);
                    cli.println("About to write ``filename``.new.xml");
                    writeMap(parsePath(filename + ".new.xml"), map);
                } catch (FileNotFoundException|NoSuchFileException except) {
                    log.error("``filename`` not found", except);
                } catch (IOException except) {
                    log.error("I/O error processing ``filename``", except);
                } catch (XMLStreamException except) {
                    log.error("XML stream error reading ``filename``", except);
                } catch (SPFormatException except) {
                    log.error("SP map format error in ``filename``", except);
                }
            }
        }
    }
}