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
    IMap,
    MapDimensions,
    IMutableMap,
    Player,
    TileType,
    TileFixture,
    pointFactory,
    River,
    Point,
    SPMap,
    MapDimensionsImpl,
    PlayerCollection,
    PlayerImpl
}
import strategicprimer.model.map.fixtures {
    Ground,
    TextFixture
}
import strategicprimer.model.map.fixtures.mobile.worker {
    randomRace
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
shared IMap convertOneToTwo(
		"The version-1 map to convert"
        IMap old,
		"The source for kinds of ground, fields, etc."
		ExplorationRunner runner,
		"Whether the map is the main map (new encounter-type fixtures don't go on
		 players' maps)"
		Boolean main) {
	MapDimensions oldDimensions = old.dimensions;
	if (oldDimensions.version >= 2) {
		return old;
	}
	IMutableMap retval = SPMap(MapDimensionsImpl(
		oldDimensions.rows * expansionFactor,
		oldDimensions.columns * expansionFactor, 2), PlayerCollection(), nextTurn);
	Player independent = old.players.find(Player.independent)
	else PlayerImpl(-1, "independent");
	retval.addPlayer(independent);
	for (player in old.players) {
		retval.addPlayer(player);
	}
	MutableList<Point> converted = LinkedList<Point>();
	IDRegistrar idFactory = createIDFactory(old);
	IMap oldCopy = old.copy(false, null);
	TileType equivalentTerrain(TileType original) {
		switch (original)
		case (TileType.mountain|TileType.temperateForest) { return TileType.plains; }
		case (TileType.borealForest) { return TileType.steppe; }
		else { return original; }
	}
	"Add a fixture to a tile if this is the main map."
	void addFixture(Point point, TileFixture fixture) {
		if (main) {
			if (is Ground fixture, !retval.ground(point) exists) {
				retval.setGround(point, fixture);
			} else if (is Forest fixture, !retval.forest(point) exists) {
				retval.setForest(point, fixture);
			} else {
				retval.addFixture(point, fixture);
			}
		}
	}
	"Convert a tile. That is, change it from a forest or mountain type to the proper
	 replacement type plus the proper fixture, and also add the proper Ground."
	void convertSubTile(Point point) {
		TileType originalTerrain = retval.baseTerrain(point);
		if (TileType.mountain == originalTerrain) {
			retval.setMountainous(point, true);
		} else if (!retval.forest(point) exists,
			retval.otherFixtures(point).narrow<Forest>().empty,
			(TileType.temperateForest == originalTerrain ||
			TileType.borealForest == originalTerrain)) {
			retval.setForest(point, Forest(runner.getPrimaryTree(point,
				originalTerrain, retval.mountainous(point), retval.allFixtures(point),
				retval.dimensions), false, idFactory.createID()));
		}
		retval.setBaseTerrain(point, equivalentTerrain(originalTerrain));
		addFixture(point, Ground(idFactory.createID(),
			runner.getPrimaryRock(point, retval.baseTerrain(point),
				retval.mountainous(point), retval.allFixtures(point), retval.dimensions),
			false));
	}
	"Convert a single version-1 tile to the equivalent version-2 tiles."
	{Point*} convertTile(Point point) {
		Point[] initial = [ for (i in 0:expansionFactor)
		for (j in 0:expansionFactor)
		pointFactory(point.row * expansionFactor + i,
			point.column * expansionFactor + j) ];
		for (subtile in initial) {
			retval.setBaseTerrain(subtile, oldCopy.baseTerrain(point));
			convertSubTile(subtile);
		}
		if (!oldCopy.locationEmpty(point)) {
			Integer idNum = idFactory.createID();
			if (is IMutableMap oldCopy) {
				oldCopy.addFixture(point, Village(TownStatus.active, "", idNum,
					independent, randomRace(DefaultRandom(idNum))));
			}
			{TileFixture*} fixtures = oldCopy.allFixtures(point);
			void riversAt(Point? point, River* rivers) {
				assert (exists point);
				retval.addRivers(point, *rivers);
			}
			for (river in oldCopy.rivers(point)) {
				assert (expansionFactor == 4); // the river-dispersion algorithm is tuned
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
			for (iteration in 0:maxIterations) {
				if (!shuffledFixtures.front exists) {
					break;
				} else if (exists currentSubtile = shuffledInitial.accept()) {
					if (retval.otherFixtures(point).every(
								(fixture) =>
						fixture is Forest|Ground|Sandbar|Shrub|Meadow|Hill),
						exists fixture = shuffledFixtures.accept()) {
						if (is ITownFixture fixture) {
							{TileFixture*} toRemove = {
								for (suspect in retval.otherFixtures(point))
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
	for (row in 0:oldDimensions.rows) {
		for (column in 0:oldDimensions.columns) {
			converted.addAll(convertTile(pointFactory(row, column)));
		}
	}
	Random rng = DefaultRandom(maxIterations);
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
				for (fixture in retval.otherFixtures(neighbor)) {
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
				if (retval.baseTerrain(neighbor) == TileType.ocean ||
				!retval.rivers(neighbor).empty) {
					return true;
				}
			} else {
				return false;
			}
		}
		try {
			if (TileType.ocean != retval.baseTerrain(point)) {
				if (adjacentToTown(), rng.nextFloat() < 0.6) {
					Integer id = idFactory.createID();
					if (rng.nextBoolean()) {
						addFixture(point, Meadow(runner.recursiveConsultTable("grain",
							point, retval.baseTerrain(point), retval.mountainous(point),
							retval.allFixtures(point), retval.dimensions), true,
							true, id, FieldStatus.random(id)));
					} else {
						addFixture(point, Grove(true, true,
							runner.recursiveConsultTable("fruit_trees", point,
								retval.baseTerrain(point), retval.mountainous(point),
								retval.allFixtures(point), retval.dimensions), id));
					}
				} else if (TileType.desert == retval.baseTerrain(point)) {
					Boolean watered = adjacentWater();
					if ((watered && rng.nextFloat() < desertToPlains) ||
					!retval.rivers(point).empty &&
					rng.nextFloat() < 0.6) {
						retval.setBaseTerrain(point, TileType.plains);
					}
				} else if (rng.nextFloat() < addForestProbability) {
					String forestType = runner.recursiveConsultTable(
						"temperate_major_tree", point, retval.baseTerrain(point),
						retval.mountainous(point), retval.allFixtures(point),
						retval.dimensions);
					if (exists existingForest = retval.forest(point),
						forestType == existingForest.kind) {
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
