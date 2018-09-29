import java.io {
    IOException,
    JReader=Reader
}
import java.nio.file {
    JPath=Path
}

import javax.xml.stream {
    XMLStreamException
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.common.xmlio {
    SPFormatException,
    Warning
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import ceylon.file {
    parsePath
}
import strategicprimer.drivers.common {
    IDriverModel,
    DriverFailedException,
    IMultiMapModel,
    SimpleDriverModel,
    SimpleMultiMapModel
}

shared object mapReaderAdapter {
    "Read a map model from a file or a stream, wrapping any errors the process generates
     in a [[DriverFailedException]] to simplify callers."
    todo("Return exceptions instead of throwing them")
    shared IDriverModel readMapModel(JPath|JReader file, Warning warner) {
        try {
            if (is JReader file) {
                return SimpleDriverModel(mapIOHelper.readMap(file, warner), null);
            } else {
                return SimpleDriverModel(mapIOHelper.readMap(file, warner), file);
            }
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error while reading");
        } catch (XMLStreamException except) {
            throw DriverFailedException(except, "Malformed XML");
        } catch (SPFormatException except) {
            throw DriverFailedException(except, "SP map format error");
        }
    }
    "Read several maps into a driver model, wrapping any errors in a
     DriverFailedException to simplify callers."
    todo("Return exceptions instead of throwing them")
    shared IMultiMapModel readMultiMapModel(Warning warner, JPath master, JPath* files) {
        log.trace("In mapReaderAdapter.readMultiMapModel");
        variable String current = master.string;
        try {
            IMultiMapModel retval = SimpleMultiMapModel(
                mapIOHelper.readMap(master, warner), master);
            for (file in files) {
                current = file.string;
                retval.addSubordinateMap(mapIOHelper.readMap(file, warner), file);
            }
            log.trace("Finished with mapReaderAdapter.readMultiMapModel");
            return retval;
        } catch (IOException except) {
            throw DriverFailedException(except,
                "I/O error reading from file ``current``");
        } catch (XMLStreamException except) {
            throw DriverFailedException(except, "Malformed XML in ``current``");
        } catch (SPFormatException except) {
            throw DriverFailedException(except, "SP map format error in ``current``");
        }
    }
    "Write maps from a map model back to file, wrapping any errors in a
     [[DriverFailedException]] to simplify callers."
    todo("Return exceptions instead of throwing them")
    shared void writeModel(IDriverModel model) {
        if (exists mainFile = model.mapFile) {
            try {
                mapIOHelper.writeMap(parsePath(mainFile.string), model.map);
                model.mapModified = false;
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error writing to ``mainFile``");
            }
        } else {
            log.error("Model didn't contain filename for main map, so didn't write it");
        }
        if (is IMultiMapModel model) {
            for (map->[filename, _] in model.subordinateMaps) {
                if (exists filename) {
                    try {
                        mapIOHelper.writeMap(parsePath(filename.string), map);
                        model.setModifiedFlag(map, false);
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
}