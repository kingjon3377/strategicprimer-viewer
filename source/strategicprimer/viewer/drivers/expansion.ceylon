import ceylon.collection {
    ArrayList,
    MutableList
}

import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.common.map {
    Player,
    HasOwner,
    TileType,
    TileFixture,
    Point,
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    AnimalImpl
}
import strategicprimer.model.common.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.model.common.map.fixtures.towns {
    ITownFixture
}
import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IMultiMapModel,
    IDriverModel,
    SPOptions,
    ParamCount,
    IDriverUsage,
    DriverUsage,
    CLIDriver,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver,
    SimpleDriverModel
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    Speed,
    surroundingPointIterable,
    simpleMovementModel
}
import ceylon.random {
    randomize
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import lovelace.util.common {
    singletonRandom,
    PathWrapper
}
"""A factory for a driver to update a player's map to include a certain minimum distance
   around allied villages."""
service(`interface DriverFactory`)
shared class ExpansionDriverFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-n", "--expand"];
        paramsWanted = ParamCount.atLeastTwo;
        shortDescription = "Expand a player's map.";
        longDescription = "Ensure a player's map covers all terrain allied villages can
                           see.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => ExpansionDriver(cli, options, model);
    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);
}
"""A driver to update a player's map to include a certain minimum distance around allied
   villages."""
// FIXME: Write GUI for map-expanding driver
shared class ExpansionDriver(ICLIHelper cli, SPOptions options, IDriverModel model)
        satisfies CLIDriver {
    shared actual void startDriver() {
        if (is IMultiMapModel model) {
            IMapNG master = model.map;
            for (map->[path, _] in model.subordinateMaps) {
                Player currentPlayer = map.currentPlayer; // TODO: move these inner methods to the top level of the object
                Boolean containsSwornVillage(Point point) { // TODO: fat arrow once syntax sugar in place
//                    return map.fixtures[point].narrow<ITownFixture>() // TODO: syntax sugar once compiler bug fixed
                    return map.fixtures.get(point).narrow<ITownFixture>()
                        .map(HasOwner.owner).any(currentPlayer.equals);
                }
                void safeAdd(Point point, TileFixture fixture) {
                    if (map.fixtures.get(point).any(fixture.equals)) {
                        return;
                    } else if (is HasOwner fixture, !fixture is ITownFixture) {
                        value zeroed = fixture.copy(fixture.owner != currentPlayer);
                        if (!map.fixtures.get(point).any(zeroed.equals)) {
                            map.addFixture(point, fixture.copy(
                                    fixture.owner != currentPlayer));
                        }
                    } else {
                        value zeroed = fixture.copy(true);
                        if (!map.fixtures.get(point).any(zeroed.equals)) {
                            map.addFixture(point, fixture.copy(true));
                        }
                    }
                }
                object mock satisfies HasOwner {
                    shared actual Player owner = currentPlayer;
                }
                for (point in map.locations.filter(containsSwornVillage)) {
                    for (neighbor in surroundingPointIterable(point,
                            map.dimensions)) {
                        if (!map.baseTerrain[neighbor] exists) {
                            map.baseTerrain[neighbor] =
                                master.baseTerrain[neighbor];
                            //if (master.mountainous[neighbor]) { // TODO: syntax sugar once compiler bug fixed
                            if (master.mountainous.get(neighbor)) {
                                map.mountainous[neighbor] = true;
                            }
                        }
                        MutableList<TileFixture> possibilities =
                                ArrayList<TileFixture>();
                        //for (fixture in master.fixtures[neighbor]) {
                        for (fixture in master.fixtures.get(neighbor)) {
                            if (fixture is CacheFixture ||
                                    //map.fixtures[neighbor]
                                    map.fixtures.get(neighbor)
                                        .contains(fixture)) {
                                continue;
                            } else if (simpleMovementModel
                                    .shouldAlwaysNotice(mock, fixture)) {
                                safeAdd(neighbor, fixture);
                            } else if (simpleMovementModel.shouldSometimesNotice(mock,
                                    Speed.careful, fixture)) {
                                possibilities.add(fixture);
                            }
                        }
                        if (exists first = randomize(possibilities).first) {
                            safeAdd(neighbor, first);
                        }
                    }
                }
                model.setModifiedFlag(map, true);
            }
        } else {
            log.warn("Expansion on a master map with no subordinate maps does nothing");
        }
    }
}
"An interface for map-populating passes to implement. Create an object satisfying this
 interface, and assign a reference to it to the designated field in
 [[MapPopulatorDriver]], and run the driver."
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
        if (exists terrain = map.baseTerrain[location]) {
            return !map.mountainous.get(location)&& TileType.ocean != terrain &&
                !map.fixtures[location]?.narrow<Forest>()?.first exists;
        } else {
            return false;
        }
    }
    shared actual Float chance = 0.05;
    shared actual void create(Point location, IMutableMapNG map, IDRegistrar idf) =>
            map.addFixture(location,
                AnimalImpl("hare", false, "wild", idf.createID()));
}
"""A factory for a driver to add some kind of fixture to suitable tiles throughout the
   map."""
service(`interface DriverFactory`)
shared class MapPopulatorFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-l", "--populate"];
        paramsWanted = ParamCount.one;
        shortDescription = "Add missing fixtures to a map";
        longDescription = "Add specified kinds of fixtures to suitable points throughout
                           a map";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => MapPopulatorDriver(cli, options, model);
    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleDriverModel(map, path);
}
"""A driver to add some kind of fixture to suitable tiles throughout the map. Customize
   the [[populator]] field before each use."""
// TODO: Write GUI equivalent of Map Populator Driver
shared class MapPopulatorDriver(ICLIHelper cli, SPOptions options,
        IDriverModel model) satisfies CLIDriver {
    "The object that does the heavy lifting of populating the map. This is the one field
     that should be changed before each populating pass."
    MapPopulator populator = sampleMapPopulator;
    variable Integer suitableCount = 0;
    variable Integer changedCount = 0;
    "Populate the map. You shouldn't need to customize this."
    void populate(IMutableMapNG map) {
        IDRegistrar idf = createIDFactory(map);
        for (location in randomize(map.locations)) {
            if (populator.isSuitable(map, location)) {
                suitableCount++;
                if (singletonRandom.nextFloat() < populator.chance) {
                    changedCount++;
                    populator.create(location, map, idf);
                }
            }
        }
    }
    shared actual void startDriver() {
        populate(model.map);
        cli.println(
            "``changedCount`` out of ``suitableCount`` suitable locations were changed");
        if (changedCount > 0) {
            model.mapModified = true;
        }
    }
}
