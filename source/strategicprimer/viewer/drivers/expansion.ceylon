import ceylon.collection {
    ArrayList,
    MutableList
}
import ceylon.math.float {
    random
}

import strategicprimer.model.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.map {
    Player,
    IMutableMapNG,
    TileType,
    TileFixture,
    IMapNG,
    HasOwner,
    Point
}
import strategicprimer.model.map.fixtures.mobile {
    Animal
}
import strategicprimer.model.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.model.map.fixtures.towns {
    ITownFixture
}
import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IMultiMapModel,
    IDriverModel,
    SimpleCLIDriver,
    SPOptions,
    ParamCount,
    IDriverUsage,
    DriverUsage,
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    Speed,
    surroundingPointIterable,
    shouldAlwaysNotice,
    shouldSometimesNotice
}
import ceylon.random {
    randomize
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
"""A driver to update a player's map to include a certain minimum distance around allied
   villages."""
object expansionDriver satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-n";
        longOption = "--expand";
        paramsWanted = ParamCount.atLeastTwo;
        shortDescription = "Expand a player's map.";
        longDescription = "Ensure a player's map covers all terrain allied villages can
                           see.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            IMapNG master = model.map;
            for (pair in model.subordinateMaps) {
                IMutableMapNG map = pair.first;
                Player currentPlayer = map.currentPlayer;
                Boolean containsSwornVillage(Point point) {
//                    for (fixture in map.fixtures[point]) { // TODO: syntax sugar once compiler bug fixed
                    for (fixture in map.fixtures.get(point)) {
                        if (is ITownFixture fixture, currentPlayer == fixture.owner) {
                            return true;
                        }
                    }
                    return false;
                }
                void safeAdd(Point point, TileFixture fixture) {
                    if (is HasOwner fixture) {
                        map.addFixture(point, fixture.copy(
                            fixture.owner == currentPlayer));
                    } else {
                        map.addFixture(point, fixture.copy(true));
                    }
                }
                object mock satisfies HasOwner {
                    shared actual Player owner = currentPlayer;
                }
                for (point in map.locations) {
                    if (containsSwornVillage(point)) {
                        for (neighbor in surroundingPointIterable(point,
                                map.dimensions)) {
//                            if (map.baseTerrain[neighbor] == TileType.notVisible) { // TODO: syntax sugar once compiler bug fixed
                            if (map.baseTerrain.get(neighbor) == TileType.notVisible) {
                                map.baseTerrain[neighbor] =
//                                    master.baseTerrain[neighbor];
                                    master.baseTerrain.get(neighbor);
//                                if (master.mountainous[neighbor]) {
                                if (master.mountainous.get(neighbor)) {
                                    map.mountainous[neighbor] = true;
                                }
                            }
                            MutableList<TileFixture> possibilities =
                                    ArrayList<TileFixture>();
//                            for (fixture in master.fixtures[neighbor]) {
                            for (fixture in master.fixtures.get(neighbor)) {
                                if (fixture is CacheFixture ||
//                                        map.fixtures[neighbor]
                                        map.fixtures.get(neighbor)
                                            .contains(fixture)) {
                                    continue;
                                } else if (shouldAlwaysNotice(mock, fixture)) {
                                    safeAdd(neighbor, fixture);
                                } else if (shouldSometimesNotice(mock, Speed.careful,
                                        fixture)) {
                                    possibilities.add(fixture);
                                }
                            }
                            if (exists first = randomize(possibilities).first) {
                                safeAdd(neighbor, first);
                            }
                        }
                    }
                }
            }
        } else {
            log.warn("Expansion on a master map with no subordinate maps does nothing");
            startDriverOnModel(cli, options, SimpleMultiMapModel.copyConstructor(model));
        }
    }
}
"An interface for map-populating passes to implement. Create an object satisfying this
 interface, and assign a reference to it to the designated field in
 [[mapPopulatorDriver]], and run the driver."
interface MapPopulator {
    "Whether a point is suitable for the kind of fixture we're creating."
    shared formal Boolean isSuitable(IMapNG map, Point location);
    "The probability of adding something to any given tile."
    shared formal Float chance;
    "Add a fixture of the kind we're creating at the given location."
    shared formal void create(Point location, IMutableMapNG map, IDRegistrar idf);
}
"A sample map-populator."
object sampleMapPopulator satisfies MapPopulator {
    "Hares won't appear in mountains, forests, or ocean."
    shared actual Boolean isSuitable(IMapNG map, Point location) {
//        TileType terrain = map.baseTerrain[location]; // TODO: syntax sugar once compiler bug fixed
        TileType terrain = map.baseTerrain.get(location);
//        if (map.mountainous[location] || TileType.ocean == terrain ||
        return !map.mountainous.get(location) && TileType.ocean != terrain &&
            TileType.notVisible!=terrain &&
            !map.fixtures[location]?.narrow<Forest>()?.first exists;
    }
    shared actual Float chance = 0.05;
    shared actual void create(Point location, IMutableMapNG map, IDRegistrar idf) =>
            map.addFixture(location,
                Animal("hare", false, false, "wild", idf.createID()));
}
"""A driver to add some kind of fixture to suitable tiles throughout the map. Customize
   isSuitable(), chance(), and create() before each use."""
object mapPopulatorDriver satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-l";
        longOption = "--populate";
        paramsWanted = ParamCount.one;
        shortDescription = "Add missing fixtures to a map";
        longDescription = "Add specified kinds of fixtures to suitable points throughout
                           a map";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    "The object that does the heavy lifting of populating the map. This is the one field
     that should be changed before each populating pass."
    MapPopulator populator = sampleMapPopulator;
    variable Integer suitableCount = 0;
    variable Integer changedCount = 0;
    "Populate the map. You shouldn't need to customize this."
    void populate(IMutableMapNG map) {
        IDRegistrar idf = createIDFactory(map);
        for (location in map.locations) {
            if (populator.isSuitable(map, location)) {
                suitableCount++;
                if (random() < populator.chance) {
                    changedCount++;
                    populator.create(location, map, idf);
                }
            }
        }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        populate(model.map);
        cli.println(
            "``changedCount`` out of ``suitableCount`` suitable locations were changed");
    }
}