import ceylon.logging {
    logger,
    Logger
}

import java.io {
    FileNotFoundException,
    IOException
}
import java.nio.file {
    NoSuchFileException
}

import javax.xml.stream {
    XMLStreamException
}

import strategicprimer.drivers.common {
    DriverUsage,
    ParamCount,
    UtilityDriver,
    IDriverUsage,
    SPOptions,
    DriverFactory,
    UtilityDriverFactory
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import strategicprimer.model.common.xmlio {
    warningLevels,
    SPFormatException
}
import lovelace.util.common {
    PathWrapper
}

"A logger."
Logger log = logger(`module strategicprimer.drivers.converters`);
"A factory for the driver that convers maps, at present by halving their resolution."
service(`interface DriverFactory`)
shared class ConverterDriverFactory(
        """Set to true when the provided [[ICLIHelper]] is connected to a graphical window
           instead of standard output."""
        Boolean gui = false)
        satisfies UtilityDriverFactory {
    "The usage object."
    shared actual IDriverUsage usage = DriverUsage {
        graphical = gui;
        invocations = ["-v", "--convert"];
        paramsWanted = ParamCount.one;
        shortDescription = "Convert a map's format";
        longDescription = "Convert a map. At present, this means reducing its
                           resolution.";
        includeInCLIList = false;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };
    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            ConverterDriver(cli, options);
}
"A driver to convert maps: at present, halving their resolution."
class ConverterDriver(ICLIHelper cli, SPOptions options) satisfies UtilityDriver {
    "Run the driver."
    shared actual void startDriver(String* args) {
        for (filename in args.coalesced) {
            cli.print("Reading ``filename ``... ");
            try {
                IMutableMapNG old = mapIOHelper.readMap(PathWrapper(filename),
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
                mapIOHelper.writeMap(PathWrapper(filename + ".new.xml"), map);
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
