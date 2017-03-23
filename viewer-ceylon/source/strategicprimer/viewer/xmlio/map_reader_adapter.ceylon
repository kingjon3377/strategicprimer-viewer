import ceylon.logging {
    Logger,
    logger
}
import ceylon.test {
    test,
    assertEquals
}

import controller.map.formatexceptions {
    SPFormatException
}
import controller.map.iointerfaces {
    IMapReader,
    SPWriter
}

import java.io {
    JReader=Reader,
    IOException
}
import java.nio.file {
    JPath=Path,
    JPaths=Paths
}

import javax.xml.stream {
    XMLStreamException
}

import lovelace.util.common {
    todo
}

import model.map {
    IMutableMapNG,
    IMapNG
}

import strategicprimer.viewer.drivers {
    DriverFailedException
}
import strategicprimer.viewer.drivers.map_viewer {
    ViewerModel
}
import strategicprimer.viewer.model {
    SimpleMultiMapModel,
    IMultiMapModel,
    IDriverModel
}
import strategicprimer.viewer.xmlio.yaxml {
    yaXMLReader,
    yaXMLWriter
}

import util {
    Warning
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
IMapReader reader = yaXMLReader;
SPWriter writer = yaXMLWriter;
"Turn a series of Strings into a series of equvalent Paths, optionally omitting the
 first."
todo("Do we really need dropFirst in Ceylon?")
shared {JPath*} namesToFiles(Boolean dropFirst, String* names) {
    if (dropFirst) {
        return {for (name in names.rest) JPaths.get(name) };
    } else {
        return { for (name in names) JPaths.get(name) };
    }
}
"Read a map from a file or a stream.."
todo("Add a default value for Warning argument", "Port to use ceylon.file or ceylon.io")
shared IMutableMapNG readMap(JPath|JReader file, Warning warner) {
    if (is JPath file) {
        return reader.readMap(file, warner);
    } else {
        return reader.readMap(JPaths.get(""), file, warner);
    }
}
"Read a map model from a file or a stream, wrapping any errors the process generates in a
 [[DriverFailedException]] to simplify callers."
todo("Return exceptions instead of throwing them")
shared IDriverModel readMapModel(JPath|JReader file, Warning warner) {
    try {
        if (is JReader file) {
            return ViewerModel(readMap(file, warner), null);
        } else {
            return ViewerModel(readMap(file, warner), file);
        }
    } catch (IOException except) {
        throw DriverFailedException(except, "I/O error while reading");
    } catch (XMLStreamException except) {
        throw DriverFailedException(except, "Malformed XML");
    } catch (SPFormatException except) {
        throw DriverFailedException(except, "SP map format error");
    }
}
"Read several maps into a driver model, wrapping any errors in a DriverFailedException to
 simplify callers."
todo("Return exceptions instead of throwing them")
shared IMultiMapModel readMultiMapModel(Warning warner, JPath master, JPath* files) {
    variable String current = master.string;
    try {
        IMultiMapModel retval = SimpleMultiMapModel(readMap(master, warner),
            master);
        for (file in files) {
            current = file.string;
            retval.addSubordinateMap(readMap(file, warner), file);
        }
        return retval;
    } catch (IOException except) {
        throw DriverFailedException(except, "I/O error reading from file ``current``");
    } catch (XMLStreamException except) {
        throw DriverFailedException(except, "Malformed XML in ``current``");
    } catch (SPFormatException except) {
        throw DriverFailedException(except, "SP map format error in ``current``");
    }
}
"Write a map to file."
shared void writeMap(JPath file, IMapNG map) => writer.write(file, map);
"Write maps from a map model back to file, wrapping any errors in a
 [[DriverFailedException]] to simplify callers."
todo("Return exceptions instead of throwing them")
shared void writeModel(IDriverModel model) {
    if (exists mainFile = model.mapFile) {
        try {
            writer.write(mainFile, model.map);
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error writing to ``mainFile``");
        }
    } else {
        log.error("Model didn't contain filename for main map, so didn't write it");
    }
    if (is IMultiMapModel model) {
        for ([map, filename] in model.subordinateMaps) {
            if (exists filename) {
                try {
                    writer.write(filename, map);
                } catch (IOException except) {
                    throw DriverFailedException(except,
                        "I/O error writing to ``filename``");
                }
            } else {
                log.error("A map didn't have a filename, and so wasn't written.");
            }
        }
    }
}
test
void testNamesToFiles() {
    JPath[] expected = [ JPaths.get("two"), JPaths.get("three"), JPaths.get("four") ];
    assertEquals(namesToFiles(false, "two", "three", "four"), expected,
        "Returns all names when dropFirst is false");
    assertEquals(namesToFiles(true, "one", "two", "three", "four"), expected,
        "Drops first name when dropFirst is true");
}
