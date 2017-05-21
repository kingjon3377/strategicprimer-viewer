import ceylon.file {
    Directory,
    parsePath
}

import java.io {
    IOException
}
import java.lang {
    IllegalStateException
}
import java.nio.file {
    JPath=Path
}

import strategicprimer.drivers.converters.one_to_two {
    convertOneToTwo
}
import strategicprimer.model.map {
    IMap
}
import strategicprimer.model.xmlio {
    writeMap
}
import strategicprimer.drivers.exploration.old {
    ExplorationRunner,
    loadAllTables
}
import strategicprimer.drivers.common {
    DriverUsage,
    ParamCount,
    SPOptions,
    DriverFailedException,
    IMultiMapModel,
    IDriverModel,
    IDriverUsage,
    SimpleDriver,
    ICLIHelper
}
"A class to convert a version-1 map to a version-2 map with greater resolution."
object oneToTwoConverter satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-12";
        longOption = "--one-to-two";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Convert a map's format from version 1 to 2";
        longDescription = "Convert a map from format version 1 to format version 2";
        firstParamDescription = "mainMap.xml";
        subsequentParamDescription = "playerMap.xml";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    "Source for forest and ground types"
    ExplorationRunner runner = ExplorationRunner();
    if (is Directory directory = parsePath("tables").resource) {
        loadAllTables(directory, runner);
    } else {
        throw IllegalStateException("1-to-2 converter requires a tables directory");
    }
    void writeConvertedMap(JPath old, IMap map) {
        try {
            writeMap(parsePath(old.string).siblingPath("``old.fileName``.converted.xml"),
                map);
        } catch (IOException except) {
            throw DriverFailedException(except,
                "I/O error writing to ``old.fileName``.converted.xml");
        }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IMap oldMain = model.map;
        JPath oldMainPath;
        if (exists temp = model.mapFile) {
            oldMainPath = temp;
        } else {
            throw DriverFailedException(IllegalStateException("No path for main map"),
                "No path for main map");
        }
        IMap newMain = convertOneToTwo(oldMain, runner, true);
        writeConvertedMap(oldMainPath, newMain);
        if (is IMultiMapModel model) {
            for ([map, path] in model.subordinateMaps) {
                if (!path exists) {
                    log.warn("No file path associated with map, skipping ...");
                    continue;
                }
                assert (exists path);
                IMap newMap = convertOneToTwo(map, runner, false);
                writeConvertedMap(path, newMap);
            }
        }
    }
    "This is a CLI driver, so we can't show a file-chooser dialog."
    shared actual JPath? askUserForFile() => null;
}
