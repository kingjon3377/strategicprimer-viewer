import java.io {
    IOException,
    JReader=Reader
}

import lovelace.util.common {
    todo,
    PathWrapper,
    MalformedXMLException
}

import strategicprimer.model.common.xmlio {
    SPFormatException,
    Warning
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import strategicprimer.drivers.common {
    IDriverModel,
    DriverFailedException,
    IMultiMapModel,
    SimpleDriverModel,
    SimpleMultiMapModel
}

import strategicprimer.model.common.map {
    IMutableMapNG
}

"A collection of a few methods for reading and writing map models, adding an
 additional layer of caller convenience on top of [[mapIOHelper]]."
todo("Evaluate whether this is really desirable now that driver factories have
      their own type-specific model factory methods. Maybe take the factory
      methods as parameters in the reading methods here?")
shared object mapReaderAdapter {
    "Read a map from a file, wrapping any errors the process generates in a [[DriverFailedException]]
     it returns instead."
    shared IMutableMapNG|DriverFailedException readMap(PathWrapper file, Warning warner) {
        try {
            return mapIOHelper.readMap(file, warner);
        } catch (IOException except) {
            return DriverFailedException(except, "I/O error while reading");
        } catch (MalformedXMLException except) {
            return DriverFailedException(except, "Malformed XML");
        } catch (SPFormatException except) {
            return DriverFailedException(except, "SP map format error");
        }
    }
    "Read a map model from a file or a stream, wrapping any errors the process generates
     in a [[DriverFailedException]] to simplify callers."
    todo("Return exceptions instead of throwing them")
    shared IDriverModel readMapModel(PathWrapper|JReader file, Warning warner) {
        try {
            return SimpleDriverModel(mapIOHelper.readMap(file, warner));
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error while reading");
        } catch (MalformedXMLException except) {
            throw DriverFailedException(except, "Malformed XML");
        } catch (SPFormatException except) {
            throw DriverFailedException(except, "SP map format error");
        }
    }

    "Read several maps into a driver model, wrapping any errors in a
     DriverFailedException to simplify callers."
    todo("Return exceptions instead of throwing them")
    shared IMultiMapModel readMultiMapModel(Warning warner, PathWrapper master,
            PathWrapper* files) {
        log.trace("In mapReaderAdapter.readMultiMapModel");
        variable String current = master.string;
        try {
            IMultiMapModel retval = SimpleMultiMapModel(
                mapIOHelper.readMap(master, warner));
            for (file in files) {
                current = file.string;
                retval.addSubordinateMap(mapIOHelper.readMap(file, warner));
            }
            log.trace("Finished with mapReaderAdapter.readMultiMapModel");
            return retval;
        } catch (IOException except) {
            throw DriverFailedException(except,
                "I/O error reading from file ``current``");
        } catch (MalformedXMLException except) {
            throw DriverFailedException(except, "Malformed XML in ``current``");
        } catch (SPFormatException except) {
            throw DriverFailedException(except, "SP map format error in ``current``");
        }
    }

    "Write maps from a map model back to file, wrapping any errors in a
     [[DriverFailedException]] to simplify callers."
    todo("Return exceptions instead of throwing them")
    shared void writeModel(IDriverModel model) {
        if (exists mainFile = model.map.filename) {
            try {
                mapIOHelper.writeMap(mainFile, model.map);
                model.mapModified = false;
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error writing to ``mainFile``");
            }
        } else {
            log.error("Model didn't contain filename for main map, so didn't write it");
        }
        if (is IMultiMapModel model) {
            for (map in model.subordinateMaps) {
                if (exists filename = map.filename) {
                    try {
                        mapIOHelper.writeMap(filename, map);
                        model.clearModifiedFlag(map);
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
