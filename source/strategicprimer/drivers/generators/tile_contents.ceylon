import lovelace.util.common {
    todo
}
import strategicprimer.model.xmlio {
    warningLevels,
    mapIOHelper
}
import ceylon.collection {
    MutableMap,
    HashMap
}
import java.nio.file {
    JPaths=Paths
}
import strategicprimer.model.map {
    pointFactory,
    TileType,
    IMapNG,
    Point
}
import ceylon.file {
    parsePath,
    Directory
}
import strategicprimer.drivers.exploration.old {
    ExplorationRunner,
    loadAllTables
}
import lovelace.util.jvm {
	singletonRandom
}
"A class to non-interactively generate a tile's contents." // TODO: Replace this with a *map* contents generator
todo("Figure out how to run the Ceylon version repeatedly on a single JVM")
class TileContentsGenerator(IMapNG map) {
    ExplorationRunner runner = ExplorationRunner();
    "Tile-contents generator requires a tables directory"
    assert (is Directory directory = parsePath("tables").resource);
    loadAllTables(directory, runner);
    shared void generateTileContents(Point point,
            TileType? terrain = map.baseTerrain[point]) {
        Integer reps = singletonRandom.nextInteger(4) + 1;
        for (i in 0:reps) {
            process.writeLine(runner.recursiveConsultTable("fisher", point, terrain,
//                map.mountainous[point], {}, map.dimensions)); // TODO: syntax sugar once compiler bug fixed
                map.mountainous.get(point), {}, map.dimensions));
        }
    }
}
todo("What's the Ceylon equivalent of Collections.synchronizedMap()?")
MutableMap<String, TileContentsGenerator> tileContentsInstances =
        HashMap<String, TileContentsGenerator>();
TileContentsGenerator tileContentsInstance(String filename) {
    if (exists retval = tileContentsInstances[filename]) {
        return retval;
    } else {
        TileContentsGenerator retval =
                TileContentsGenerator(mapIOHelper.readMap(JPaths.get(filename),
                    warningLevels.default));
        tileContentsInstances[filename] = retval;
        return retval;
    }
}
shared void tileContentsGenerator() {
    // FIXME: Does process.arguments get reset properly when called from NailGun or equiv?
    String[] args = process.arguments;
    if (exists filename = args[0], exists second = args[1],
        is Integer row = Integer.parse(second), exists third = args[2],
        is Integer column = Integer.parse(third)) {
        tileContentsInstance(filename).generateTileContents(pointFactory(row, column));
    } else {
        process.writeErrorLine("Usage: tileContentsGenerator map_name.xml row col");
    }
}
