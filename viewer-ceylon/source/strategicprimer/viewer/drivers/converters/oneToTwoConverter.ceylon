import ceylon.collection {
    LinkedList,
    MutableList,
    Queue
}
import ceylon.file {
    parsePath,
    Directory
}
import ceylon.interop.java {
    CeylonIterable
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
import java.util {
    Random
}

import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    shuffle
}

import model.map {
    River,
    PointFactory,
    TileType,
    PlayerCollection,
    IMutableMapNG,
    TileFixture,
    Player,
    MapDimensions,
    MapDimensionsImpl,
    PlayerImpl,
    SPMapNG,
    IMapNG,
    Point
}
import model.map.fixtures {
    Ground,
    TextFixture
}
import model.map.fixtures.resources {
    Shrub,
    FieldStatus,
    Meadow,
    Grove
}
import model.map.fixtures.terrain {
    Forest,
    Hill,
    Sandbar
}
import model.map.fixtures.towns {
    TownStatus
}

import strategicprimer.viewer.drivers {
    DriverUsage,
    ParamCount,
    SPOptions,
    createIDFactory,
    DriverFailedException,
    ICLIHelper,
    IDriverUsage,
    SimpleDriver
}
import strategicprimer.viewer.drivers.advancement {
    randomRace
}
import strategicprimer.viewer.drivers.exploration {
    MissingTableException,
    ExplorationRunner,
    loadAllTables
}
import strategicprimer.viewer.model {
    IMultiMapModel,
    IDriverModel,
    IDRegistrar
}
import strategicprimer.viewer.model.map.fixtures.towns {
    ITownFixture,
    Village
}
import strategicprimer.viewer.xmlio {
    writeMap
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
    "The next turn, to use when creating [[TextFixture]]s."
    Integer nextTurn = 15;
    "The factor by which to expand the map on each axis."
    Integer expansionFactor = 4;
    "The maximum number of iterations per tile."
    Integer maxIterations = 100;
    shared String maxIterationsWarning = "FIXME: A fixture here was force-added after ``
    maxIterations`` iterations";
    "Probability of turning a watered desert to plains."
    Float desertToPlains = 0.4;
    "Probability of adding a forest to a tile."
    Float addForestProbability = 0.1;
    "Source for forest and ground types"
    ExplorationRunner runner = ExplorationRunner();
    if (is Directory directory = parsePath("tables").resource) {
        loadAllTables(directory, runner);
    } else {
        throw IllegalStateException("1-to-2 converter requires a tables directory");
    }
    "Convert a version-1 map to a higher-resolution version-2 map."
    shared IMapNG convert(
            "The version-1 map to convert"
            todo("Skip this if it's already version 2?")
            IMapNG old,
            "Whether the map is the main map (new encounter-type fixtures don't go on
             players' maps)"
            Boolean main) {
        MapDimensions oldDimensions = old.dimensions();
        IMutableMapNG retval = SPMapNG(MapDimensionsImpl(
            oldDimensions.rows * expansionFactor,
            oldDimensions.columns * expansionFactor, 2), PlayerCollection(), nextTurn);
        Player independent = CeylonIterable(old.players()).find(Player.independent)
        else PlayerImpl(-1, "independent");
        retval.addPlayer(independent);
        for (player in old.players()) {
            retval.addPlayer(player);
        }
        MutableList<Point> converted = LinkedList<Point>();
        IDRegistrar idFactory = createIDFactory(old);
        IMapNG oldCopy = old.copy(false, null);
        TileType equivalentTerrain(TileType original) {
            switch (original)
            case (TileType.mountain|TileType.temperateForest) { return TileType.plains; }
            case (TileType.borealForest) { return TileType.steppe; }
            else { return original; }
        }
        "Add a fixture to a tile if this is the main map."
        void addFixture(Point point, TileFixture fixture) {
            if (main) {
                if (is Ground fixture, !retval.getGround(point) exists) {
                    retval.setGround(point, fixture);
                } else if (is Forest fixture, !retval.getForest(point) exists) {
                    retval.setForest(point, fixture);
                } else {
                    retval.addFixture(point, fixture);
                }
            }
        }
        "Convert a tile. That is, change it from a forest or mountain type to the proper
         replacement type plus the proper fixture, and also add the proper Ground."
        void convertSubTile(Point point) {
            TileType originalTerrain = retval.getBaseTerrain(point);
            if (TileType.mountain == originalTerrain) {
                retval.setMountainous(point, true);
            } else if (!retval.getForest(point) exists,
                !CeylonIterable(retval.getOtherFixtures(point))
                    .find((element) => element is Forest) exists,
                (TileType.temperateForest == originalTerrain ||
                TileType.borealForest == originalTerrain)) {
                Ground? tempGround = retval.getGround(point);
                Forest? tempForest = retval.getForest(point);
                retval.setForest(point, Forest(runner.getPrimaryTree(point,
                    originalTerrain, {tempGround, tempForest,
                        *retval.getOtherFixtures(point)}.coalesced,
                    retval.dimensions()), false, idFactory.createID()));
            }
            retval.setBaseTerrain(point, equivalentTerrain(originalTerrain));
            Ground? tempGround = retval.getGround(point);
            Forest? tempForest = retval.getForest(point);
            addFixture(point, Ground(idFactory.createID(),
                runner.getPrimaryRock(point, retval.getBaseTerrain(point),
                    {tempGround, tempForest, *retval.getOtherFixtures(point)}.coalesced,
                    retval.dimensions()), false));
        }
        "Convert a single version-1 tile to the equivalent version-2 tiles."
        {Point*} convertTile(Point point) {
            Point[] initial = [ for (i in 0..expansionFactor)
            for (j in 0..expansionFactor)
            PointFactory.point(point.row * expansionFactor + i,
                point.col * expansionFactor + j) ];
            for (subtile in initial) {
                retval.setBaseTerrain(subtile, oldCopy.getBaseTerrain(point));
                convertSubTile(subtile);
            }
            if (!oldCopy.isLocationEmpty(point)) {
                Integer idNum = idFactory.createID();
                if (is IMutableMapNG oldCopy) {
                    Random rng = Random(idNum);
                    oldCopy.addFixture(point, Village(TownStatus.active, "", idNum,
                        independent, randomRace((bound) => rng.nextInt(bound))));
                }
                {TileFixture*} fixtures = {oldCopy.getGround(point),
                    oldCopy.getForest(point), *oldCopy.getOtherFixtures(point)}.coalesced;
                for (river in oldCopy.getRivers(point)) {
                    assert (expansionFactor == 4); // the river-dispersion algorithm is tuned
                    switch (river)
                    case (River.east) {
                        retval.addRivers(initial[10], River.east);
                        retval.addRivers(initial[11], River.east, River.west);
                    }
                    case (River.lake) { retval.addRivers(initial[10], River.lake); }
                    case (River.north) {
                        retval.addRivers(initial[2], River.north, River.south);
                        retval.addRivers(initial[6], River.north, River.south);
                        retval.addRivers(initial[10], River.north);
                    }
                    case (River.south) {
                        retval.addRivers(initial[10], River.south);
                        retval.addRivers(initial[14], River.south, River.north);
                    }
                    case (River.west) {
                        retval.addRivers(initial[8], River.west, River.east);
                        retval.addRivers(initial[9], River.west, River.east);
                        retval.addRivers(initial[10], River.west);
                    }
                }
                Random rng = Random((point.col.leftLogicalShift(32)) + point.row);
                Queue<Point> shuffledInitial = LinkedList(shuffle(initial,
                    rng.nextDouble));
                Queue<TileFixture> shuffledFixtures = LinkedList(shuffle(fixtures,
                    rng.nextDouble));
                for (iteration in 0..maxIterations) {
                    if (!shuffledFixtures.front exists) {
                        break;
                    } else if (exists currentSubtile = shuffledInitial.accept()) {
                        if (CeylonIterable(retval.getOtherFixtures(point)).every(
                                    (fixture) =>
                            fixture is Forest|Ground|Sandbar|Shrub|Meadow|Hill),
                            exists fixture = shuffledFixtures.accept()) {
                            if (is ITownFixture fixture) {
                                {TileFixture*} toRemove = {
                                    for (suspect in retval.getOtherFixtures(point))
                                    if (is Forest suspect) suspect };
                                for (suspect in toRemove) {
                                    retval.removeFixture(point, suspect);
                                }
                                retval.setForest(point, null);
                            }
                            addFixture(currentSubtile, fixture);
                        }
                        shuffledInitial.offer(currentSubtile);
                    }
                } else {
                    log.error("Maximum number of iterations reached on tile ``
                    point``; forcing ...");
                    while (exists fixture = shuffledFixtures.accept()) {
                        assert (exists subtile = shuffledInitial.accept());
                        addFixture(subtile, fixture);
                        retval.addFixture(subtile,
                            TextFixture(maxIterationsWarning, nextTurn));
                    }
                }
            }
            return initial;
        }
        for (row in 0..oldDimensions.rows) {
            for (column in 0..oldDimensions.columns) {
                converted.addAll(convertTile(PointFactory.point(row, column)));
            }
        }
        Random rng = Random(maxIterations);
        for (point in shuffle(converted, rng.nextDouble)) {
            // TODO: wrap around edges of map
            {Point*} neighbors = { for (row in (point.row - 1)..(point.row + 1))
            for (column in (point.col - 1)..(point.col + 1))
            PointFactory.point(row, column) }.filter((element) => point != element);
            Boolean adjacentToTown() {
                for (neighbor in neighbors) {
                    for (fixture in retval.getOtherFixtures(neighbor)) {
                        if (is ITownFixture fixture) {
                            return true;
                        }
                    }
                } else {
                    return false;
                }
            }
            Boolean adjacentWater() {
                for (neighbor in neighbors) {
                    if (retval.getBaseTerrain(neighbor) == TileType.ocean ||
                    CeylonIterable(retval.getRivers(neighbor)).first exists) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
            try {
                if (TileType.ocean != retval.getBaseTerrain(point)) {
                    if (adjacentToTown(), rng.nextDouble() < 0.6) {
                        Integer id = idFactory.createID();
                        if (rng.nextBoolean()) {
                            Ground? tempGround = retval.getGround(point);
                            Forest? tempForest = retval.getForest(point);
                            addFixture(point, Meadow(runner.recursiveConsultTable("grain",
                                point, retval.getBaseTerrain(point),
                                {tempGround, tempForest,
                                    *retval.getOtherFixtures(point)}.coalesced,
                                retval.dimensions()), true,
                                true, id, FieldStatus.random(id)));
                        } else {
                            Ground? tempGround = retval.getGround(point);
                            Forest? tempForest = retval.getForest(point);
                            addFixture(point, Grove(true, true,
                                runner.recursiveConsultTable("fruit_trees", point,
                                    retval.getBaseTerrain(point),
                                    {tempGround, tempForest,
                                        *retval.getOtherFixtures(point)}.coalesced,
                                    retval.dimensions()), id));
                        }
                    } else if (TileType.desert == retval.getBaseTerrain(point)) {
                        Boolean watered = adjacentWater();
                        if ((watered && rng.nextDouble() < desertToPlains) ||
                        CeylonIterable(retval.getRivers(point)).first exists &&
                        rng.nextDouble() < 0.6) {
                            retval.setBaseTerrain(point, TileType.plains);
                        }
                    } else if (rng.nextDouble() < addForestProbability) {
                        Ground? tempGround = retval.getGround(point);
                        Forest? tempForest = retval.getForest(point);
                        String forestType = runner.recursiveConsultTable(
                            "temperate_major_tree", point, retval.getBaseTerrain(point),
                            {tempGround, tempForest,
                                *retval.getOtherFixtures(point)}.coalesced,
                            retval.dimensions());
                        Forest? existingForest = retval.getForest(point);
                        if (exists existingForest, forestType == existingForest.kind) {
                            // do nothing
                        } else {
                            addFixture(point, Forest(forestType, false,
                                idFactory.createID()));
                        }
                    }
                }
            } catch (MissingTableException except) {
                log.warn("Missing encounter table", except);
            }
        }
        return retval;
    }
    void writeConvertedMap(JPath old, IMapNG map) {
        try {
            writeMap(old.resolveSibling("``old.fileName``.converted.xml"), map);
        } catch (IOException except) {
            throw DriverFailedException(except,
                "I/O error writing to ``old.fileName``.converted.xml");
        }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IMapNG oldMain = model.map;
        JPath oldMainPath;
        if (exists temp = model.mapFile) {
            oldMainPath = temp;
        } else {
            throw DriverFailedException(IllegalStateException("No path for main map"),
                "No path for main map");
        }
        IMapNG newMain = convert(oldMain, true);
        writeConvertedMap(oldMainPath, newMain);
        if (is IMultiMapModel model) {
            for ([map, path] in model.subordinateMaps) {
                if (!path exists) {
                    log.warn("No file path associated with map, skipping ...");
                    continue;
                }
                assert (exists path);
                IMapNG newMap = convert(map, false);
                writeConvertedMap(path, newMap);
            }
        }
    }
}
