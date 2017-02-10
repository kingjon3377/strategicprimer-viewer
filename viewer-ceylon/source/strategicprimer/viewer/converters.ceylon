import controller.map.drivers {
    UtilityDriver,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions
}
import controller.map.misc {
    ICLIHelper,
    MapReaderAdapter
}
import model.map {
    IMutableMapNG,
    IMapNG
}
import java.nio.file {
    Paths,
    NoSuchFileException
}
import util {
    Warning
}
import java.io {
    FileNotFoundException,
    IOException
}
import strategicprimer.viewer { log }
import javax.xml.stream {
    XMLStreamException
}
import controller.map.formatexceptions {
    SPFormatException
}
import controller.map.converter {
    ResolutionDecreaseConverter
}
"A driver to convert maps: at present, halving their resolution."
class ConverterDriver(
    """Set to true when the provided [[ICLIHelper]] is connected to a graphical window
       instead of standard output."""
    Boolean gui = false) satisfies UtilityDriver {
    DriverUsage tempUsage = DriverUsage(gui, "-v", "--convert", ParamCount.one,
        "Convert a map's format",
        "Convert a map. At present, this means reducing its resolution.");
    tempUsage.addSupportedOption("--current-turn=NN");
    "The usage object."
    shared actual IDriverUsage usage() => tempUsage;
    "Run the driver."
    shared actual void startDriver(ICLIHelper cli, SPOptions options, String?* args) {
        if (nonempty arguments = args.coalesced.sequence()) {
            MapReaderAdapter reader = MapReaderAdapter();
            for (filename in arguments) {
                cli.print("Reading ``filename ``... ");
                try {
                    IMutableMapNG old = reader.readMap(Paths.get(filename),
                        Warning.default);
                    if (options.hasOption("--current-turn")) {
                        value currentTurn =
                                Integer.parse(options.getArgument("--current-turn"));
                        if (is Integer currentTurn) {
                            old.setCurrentTurn(currentTurn);
                        } else {
                            log.error(
                                "Current turn passed on the command line must be an integer",
                                currentTurn);
                        }
                    }
                    cli.println(" ... Converting ... ");
                    IMapNG map = ResolutionDecreaseConverter.convert(old);
                    cli.println("About to write ``filename``.new.xml");
                    reader.write(Paths.get(filename + ".new.xml"), map);
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