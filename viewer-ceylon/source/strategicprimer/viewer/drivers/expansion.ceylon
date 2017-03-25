import strategicprimer.viewer.model.map {
    IMutableMapNG
}
import model.map {
    IMapNG,
    Player,
    Point,
    TileFixture,
    TileType,
    HasOwner
}
import strategicprimer.viewer.model.map.fixtures.towns {
    ITownFixture
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import model.map.fixtures.mobile {
    IUnit,
    Animal
}
import ceylon.interop.java {
    CeylonIterable
}
import model.map.fixtures.resources {
    CacheFixture
}
import lovelace.util.jvm { shuffle }
import lovelace.util.common {
    todo
}
import ceylon.math.float {
    random
}
import strategicprimer.viewer.drivers.exploration {
    surroundingPointIterable,
    shouldSometimesNotice,
    shouldAlwaysNotice,
    Speed
}
import strategicprimer.viewer.model {
    SimpleMultiMapModel,
    IMultiMapModel,
    IDriverModel,
    IDRegistrar
}
IUnit mockUnit(Player player) {
    return UnitProxyMaker.makeProxyFor(player);
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
        longDescription = "Ensure a player's map covers all terrain allied villages can see.";
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
                        for (neighbor in surroundingPointIterable(point,
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
                                } else if (shouldAlwaysNotice(mock, fixture)) {
                                    safeAdd(neighbor, fixture);
                                } else if (shouldSometimesNotice(mock, Speed.careful,
                                        fixture)) {
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
            startDriverOnModel(cli, options, SimpleMultiMapModel.copyConstructor(model));
        }
    }
}
"""A driver to add some kind of fixture to suitable tiles throughout the map. Customize
   isSuitable(), chance(), and create() before each use."""
todo("Make this an interface for objects to satisfy instead?")
object mapPopulatorDriver satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-l";
        longOption = "--populate";
        paramsWanted = ParamCount.one;
        shortDescription = "Add missing fixtures to a map";
        longDescription = "Add specified kinds of fixtures to suitable points throughout a map";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    variable Integer suitableCount = 0;
    variable Integer changedCount = 0;
    "The first method to customize for each use: whether a point is suitable for the
     kind of fixture we're creating."
    Boolean isSuitable(IMapNG map, Point location) {
        TileType terrain = map.getBaseTerrain(location);
        // Hares won't appear in mountains, forests, or ocean.
        if (map.isMountainous(location) || TileType.ocean == terrain ||
                TileType.notVisible == terrain || map.getForest(location) exists) {
            return false;
        } else {
            suitableCount++;
            return true;
        }
    }
    """The probability of adding something to any given tile. The second method to
       customize."""
    Float chance = 0.05;
    """Add a fixture of the kind we're creating at the given location. The third method to
       customize."""
    void create(Point location, IMutableMapNG map, IDRegistrar idf) {
        changedCount++;
        map.addFixture(location, Animal("hare", false, false, "wild", idf.createID()));
    }
    "Populate the map. You shouldn't need to customize this."
    void populate(IMutableMapNG map) {
        IDRegistrar idf = createIDFactory(map);
        for (location in map.locations()) {
            if (isSuitable(map, location) && random() < chance) {
                create(location, map, idf);
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