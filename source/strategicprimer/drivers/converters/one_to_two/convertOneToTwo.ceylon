import ceylon.collection {
    MutableList,
    LinkedList,
    Queue
}
import ceylon.logging {
    Logger,
    logger
}

import strategicprimer.model.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.map {
    IMapNG,
    MapDimensions,
    IMutableMapNG,
    Player,
    TileType,
    TileFixture,
    pointFactory,
    River,
    Point,
    SPMapNG,
    MapDimensionsImpl,
    PlayerCollection,
    PlayerImpl
}
import strategicprimer.model.map.fixtures {
    Ground,
    TextFixture
}
import strategicprimer.model.map.fixtures.mobile.worker {
    raceFactory
}
import strategicprimer.model.map.fixtures.resources {
    Shrub,
    Meadow,
    FieldStatus,
    Grove
}
import strategicprimer.model.map.fixtures.terrain {
    Forest,
    Sandbar,
    Hill
}
import strategicprimer.model.map.fixtures.towns {
    Village,
    TownStatus,
    ITownFixture
}
import strategicprimer.drivers.exploration.old {
    MissingTableException,
    ExplorationRunner
}
import ceylon.random {
    randomize,
    Random,
    DefaultRandom
}
Logger log = logger(`module strategicprimer.model`);
"Convert a version-1 map to a higher-resolution version-2 map."
suppressWarnings("deprecation")
shared IMapNG convertOneToTwo(
        "The version-1 map to convert"
        IMapNG old,
        "The source for kinds of ground, fields, etc."
        ExplorationRunner runner,
        "Whether the map is the main map (new encounter-type fixtures don't go on
         players' maps)"
        Boolean main) {
    MapDimensions oldDimensions = old.dimensions;
    if (oldDimensions.version >= 2) {
        return old;
    }
    IMutableMapNG retval = SPMapNG(MapDimensionsImpl(
	        oldDimensions.rows * oneToTwoConfig.expansionFactor,
	        oldDimensions.columns * oneToTwoConfig.expansionFactor, 2), PlayerCollection(),
	    oneToTwoConfig.nextTurn);
    Player independent = old.players.find(Player.independent)
    else PlayerImpl(-1, "independent");
    retval.addPlayer(independent);
    for (player in old.players) {
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
            retval.addFixture(point, fixture);
        }
    }
    "Convert a tile. That is, change it from a forest or mountain type to the proper
     replacement type plus the proper fixture, and also add the proper Ground."
    void convertSubTile(Point point) {
        if (exists originalTerrain = retval.baseTerrain[point]) {
            if (TileType.mountain == originalTerrain) {
                retval.mountainous[point] = true;
//        } else if (retval.fixtures[point].narrow<Forest>().empty, // TODO: syntax sugar once compiler bug fixed
            } else if (retval.fixtures.get(point).narrow<Forest>().empty,
                (TileType.temperateForest == originalTerrain ||
                TileType.borealForest == originalTerrain)) {
                retval.addFixture(point, Forest(runner.getPrimaryTree(point,
//                originalTerrain, retval.mountainous[point], retval.fixtures[point],
                    originalTerrain, retval.mountainous.get(point), retval.fixtures.get(point),
                    retval.dimensions), false, idFactory.createID()));
            }
            value newTerrain = equivalentTerrain(originalTerrain);
            retval.baseTerrain[point] = newTerrain;
            addFixture(point, Ground(idFactory.createID(),
                runner.getPrimaryRock(point, newTerrain,
//                retval.mountainous[point], retval.fixtures[point], retval.dimensions), // TODO: syntax sugar once compiler bug fixed
                    retval.mountainous.get(point), retval.fixtures.get(point), retval.dimensions),
                false));
        }
    }
    "Convert a single version-1 tile to the equivalent version-2 tiles."
    {Point*} convertTile(Point point) {
        Point[] initial = [ for (i in 0:oneToTwoConfig.expansionFactor)
        for (j in 0:oneToTwoConfig.expansionFactor)
        pointFactory(point.row * oneToTwoConfig.expansionFactor + i,
            point.column * oneToTwoConfig.expansionFactor + j) ];
        for (subtile in initial) {
//            retval.baseTerrain[subtile] = oldCopy.baseTerrain[point]; // TODO: syntax sugar once compiler bug fixed
            retval.baseTerrain[subtile] = oldCopy.baseTerrain.get(point);
            convertSubTile(subtile);
        }
        if (!oldCopy.locationEmpty(point)) {
            Integer idNum = idFactory.createID();
            if (is IMutableMapNG oldCopy) {
                oldCopy.addFixture(point, Village(TownStatus.active, "", idNum,
                    independent, raceFactory.randomRace(DefaultRandom(idNum))));
            }
//            {TileFixture*} fixtures = oldCopy.fixtures[point]; // TODO: syntax sugar once compiler bug fixed
            {TileFixture*} fixtures = oldCopy.fixtures.get(point);
            void riversAt(Point? point, River* rivers) {
                assert (exists point);
                retval.addRivers(point, *rivers);
            }
//            for (river in oldCopy.rivers[point]) {
            for (river in oldCopy.rivers.get(point)) {
                assert (oneToTwoConfig.expansionFactor == 4); // the river-dispersion algorithm is tuned
                switch (river)
                case (River.east) {
                    riversAt(initial[10], River.east);
                    riversAt(initial[11], River.east, River.west);
                }
                case (River.lake) { riversAt(initial[10], River.lake); }
                case (River.north) {
                    riversAt(initial[2], River.north, River.south);
                    riversAt(initial[6], River.north, River.south);
                    riversAt(initial[10], River.north);
                }
                case (River.south) {
                    riversAt(initial[10], River.south);
                    riversAt(initial[14], River.south, River.north);
                }
                case (River.west) {
                    riversAt(initial[8], River.west, River.east);
                    riversAt(initial[9], River.west, River.east);
                    riversAt(initial[10], River.west);
                }
            }
            Random rng = DefaultRandom((point.column.leftLogicalShift(32)) + point.row);
            Queue<Point> shuffledInitial = LinkedList(randomize(initial, rng));
            Queue<TileFixture> shuffledFixtures = LinkedList(randomize(fixtures, rng));
            for (iteration in 0:oneToTwoConfig.maxIterations) {
                if (!shuffledFixtures.front exists) {
                    break;
                } else if (exists currentSubtile = shuffledInitial.accept()) {
//                    if (retval.fixtures[point].every( // TODO: syntax sugar once compiler bug fixed
                    if (retval.fixtures.get(point).every(
                                (fixture) =>
                        fixture is Forest|Ground|Sandbar|Shrub|Meadow|Hill),
                        exists fixture = shuffledFixtures.accept()) {
                        if (is ITownFixture fixture) {
//                            {TileFixture*} toRemove = retval.fixtures[point]
                            {TileFixture*} toRemove = retval.fixtures.get(point)
                                .narrow<Forest>();
                            for (suspect in toRemove) {
                                retval.removeFixture(point, suspect);
                            }
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
                        TextFixture(oneToTwoConfig.maxIterationsWarning, oneToTwoConfig.nextTurn));
                }
            }
        }
        return initial;
    }
    for (row in 0:oldDimensions.rows) {
        for (column in 0:oldDimensions.columns) {
            converted.addAll(convertTile(pointFactory(row, column)));
        }
    }
    Random rng = DefaultRandom(oneToTwoConfig.maxIterations);
    for (point in randomize(converted, rng)) {
        {Point*} neighbors = {
            for (row in ((point.row - 1)..(point.row + 1))
                .map((num) => num.modulo(retval.dimensions.rows)))
            for (column in ((point.column - 1)..(point.column + 1))
                .map((num) => num.modulo(retval.dimensions.columns)))
            pointFactory(row, column)
        }.filter((element) => point != element);
        Boolean adjacentToTown() {
            for (neighbor in neighbors) {
                // if (!retval.fixtures[neighbor].narrow<ITownFixture>().empty) { // TODO: syntax sugar once compiler bug fixed
                if (!retval.fixtures.get(neighbor).narrow<ITownFixture>().empty) {
                    return true;
                }
            } else {
                return false;
            }
        }
        Boolean adjacentWater() {
            for (neighbor in neighbors) {
                if (exists terrain = retval.baseTerrain[neighbor],
                        terrain == TileType.ocean ||
//                        !retval.rivers[neighbor].empty) { // TODO: syntax sugar once compiler bug fixed
                        !retval.rivers.get(neighbor).empty) {
                    return true;
                }
            } else {
                return false;
            }
        }
        try {
            if (exists terrain = retval.baseTerrain[point], terrain != TileType.ocean) {
                if (adjacentToTown(), rng.nextFloat() < 0.6) {
                    Integer id = idFactory.createID();
                    if (rng.nextBoolean()) {
                        addFixture(point, Meadow(runner.recursiveConsultTable("grain",
//                            point, retval.baseTerrain[point], retval.mountainous[point],
                            point, retval.baseTerrain[point], retval.mountainous.get(point),
//                            retval.fixtures[point], retval.dimensions), true,
                            retval.fixtures.get(point), retval.dimensions), true,
                            true, id, FieldStatus.random(id)));
                    } else {
                        addFixture(point, Grove(true, true,
                            runner.recursiveConsultTable("fruit_trees", point,
//                                retval.baseTerrain[point], retval.mountainous[point],
                                retval.baseTerrain.get(point), retval.mountainous.get(point),
//                                retval.fixtures[point], retval.dimensions), id));
                                retval.fixtures.get(point), retval.dimensions), id));
                    }
                } else if (terrain == TileType.desert) {
                    Boolean watered = adjacentWater();
                    if ((watered && rng.nextFloat() < oneToTwoConfig.desertToPlains) ||
//                    !retval.rivers[point].empty &&
                    !retval.rivers.get(point).empty &&
                    rng.nextFloat() < 0.6) {
                        retval.baseTerrain[point] = TileType.plains;
                    }
                } else if (rng.nextFloat() < oneToTwoConfig.addForestProbability) {
                    String forestType = runner.recursiveConsultTable(
//                        "temperate_major_tree", point, retval.baseTerrain[point], // TODO: syntax sugar once compiler bug fixed
                        "temperate_major_tree", point, retval.baseTerrain.get(point),
//                        retval.mountainous[point], retval.fixtures[point],
                        retval.mountainous.get(point), retval.fixtures.get(point),
                        retval.dimensions);
//                    if (retval.fixtures[point].narrow<Forest>().map(Forest.kind).any(forestType.equals)) {
                    if (retval.fixtures.get(point).narrow<Forest>().map(Forest.kind).any(forestType.equals)) {
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
