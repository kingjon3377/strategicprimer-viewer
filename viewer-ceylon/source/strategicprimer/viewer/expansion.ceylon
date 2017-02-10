import controller.map.drivers {
    SimpleCLIDriver,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions
}
import controller.map.misc {
    ICLIHelper
}
import model.misc {
    IDriverModel,
    IMultiMapModel,
    SimpleMultiMapModel
}
import model.map {
    IMapNG,
    IMutableMapNG,
    Player,
    Point,
    TileFixture,
    TileType,
    HasOwner
}
import model.map.fixtures.towns {
    ITownFixture
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import model.map.fixtures.mobile {
    IUnit,
    SimpleMovement
}
import ceylon.interop.java {
    CeylonIterable
}
import model.exploration {
    SurroundingPointIterable,
    IExplorationModel
}
import model.map.fixtures.resources {
    CacheFixture
}
import lovelace.util.jvm { shuffle }
IUnit mockUnit(Player player) {
    return UnitProxyMaker.makeProxyFor(player);
}
"""A driver to update a player's map to include a certain minimum distance around allied
   villages."""
object expansionDriver satisfies SimpleCLIDriver {
    DriverUsage usageObject = DriverUsage(false, "-n", "--expand", ParamCount.atLeastTwo,
        "Expand a player's map.",
        "Ensure a player's map covers all terrain allied villages can see.");
    usageObject.addSupportedOption("--current-turn=NN");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            IMapNG master = model.map;
            for (pair in model.subordinateMaps) {
                IMutableMapNG map = pair.first();
                Player currentPlayer = map.currentPlayer;
                Boolean containsSwornVillage(Point point) {
                    for (fixture in map.getOtherFixtures(point)) {
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
                IUnit mock = mockUnit(currentPlayer);
                for (point in map.locations()) {
                    if (containsSwornVillage(point)) {
                        for (neighbor in SurroundingPointIterable(point,
                                map.dimensions())) {
                            if (map.getBaseTerrain(neighbor) == TileType.notVisible) {
                                map.setBaseTerrain(neighbor,
                                    master.getBaseTerrain(neighbor));
                                if (master.isMountainous(neighbor)) {
                                    map.setMountainous(neighbor, true);
                                }
                            }
                            MutableList<TileFixture> possibilities =
                                    ArrayList<TileFixture>();
                            if (exists ground = master.getGround(neighbor)) {
                                possibilities.add(ground);
                            }
                            if (exists forest = master.getForest(neighbor)) {
                                possibilities.add(forest);
                            }
                            for (fixture in master.getOtherFixtures(neighbor)) {
                                if (fixture is CacheFixture ||
                                        CeylonIterable(map.getOtherFixtures(neighbor))
                                            .contains(fixture)) {
                                    continue;
                                } else if (SimpleMovement.shouldAlwaysNotice(mock,
                                        fixture)) {
                                    safeAdd(neighbor, fixture);
                                } else if (SimpleMovement.shouldSometimesNotice(mock,
                                        IExplorationModel.Speed.careful, fixture)) {
                                    possibilities.add(fixture);
                                }
                            }
                            if (exists first = shuffle(possibilities).first) {
                                safeAdd(neighbor, first);
                            }
                        }
                    }
                }
            }
        } else {
            log.warn("Expansion on a master map with no subordinate maps does nothing");
            startDriver(cli, options, SimpleMultiMapModel(model));
        }
    }
}