import java.lang {
    IllegalArgumentException
}
import strategicprimer.viewer.model.map {
    TileType,
    SPMapNG,
    IMutableMapNG,
    PlayerCollection,
    pointFactory,
    IMapNG
}
import lovelace.util.jvm {
    shuffle,
    EnumCounter
}
import model.map {
    River,
    MapDimensionsImpl,
    Point,
    PlayerImpl
}
import ceylon.collection {
    MutableSet,
    HashSet
}
import strategicprimer.viewer.model.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.viewer.model.map.fixtures.towns {
    TownSize,
    Fortress
}
import ceylon.test {
    assertEquals,
    assertTrue,
    test,
    assertThatException
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit,
    Animal,
    Unit
}
import strategicprimer.viewer.model.map.fixtures {
    Ground
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Forest
}
"A utility to convert a map to an equivalent half-resolution one."
IMapNG decreaseResolution(IMapNG old) {
	if (old.dimensions.rows % 2 != 0 || old.dimensions.columns %2 != 0) {
		throw IllegalArgumentException(
			"Can only convert maps with even numbers of rows and columns");
	}
	PlayerCollection players = PlayerCollection();
	for (player in old.players) {
		players.add(player);
	}
	Integer newColumns = old.dimensions.columns / 2;
	Integer newRows = old.dimensions.rows / 2;
	IMutableMapNG retval = SPMapNG(MapDimensionsImpl(newRows, newColumns, 2), players,
		old.currentTurn);
	TileType consensus([TileType+] types) {
		assert (is TileType[4] types); // the algorithm assumes only possible splits are
		// 4-0, 3-1, 2-2, 2-1-1, and 1-1-1-1
		// TODO: more Ceylonic algorithm/implementation
		EnumCounter<TileType> counter = EnumCounter<TileType>();
		counter.countMany(*types);
		MutableSet<TileType> twos = HashSet<TileType>();
		for (type in `TileType`.caseValues) {
			switch (counter.getCount(type))
			case (0|1) { }
			case (2) { twos.add(type); }
			else { return type; }
		}
		if (twos.size == 1) {
			assert (exists type = twos.first);
			return type;
		} else {
			assert (exists type = shuffle(types).first);
			return type;
		}
	}
	for (row in 0..newRows) {
		for (column in 0..newColumns) {
			Point point = pointFactory(row, column);
			Point[4] subPoints = [pointFactory(row * 2, column * 2),
				pointFactory(row * 2, (column * 2) + 1),
				pointFactory((row * 2) + 1, column * 2),
				pointFactory((row * 2) + 1, (column * 2) + 1) ];
			retval.setBaseTerrain(point,
				consensus([ *subPoints.map(old.getBaseTerrain) ]));
			for (oldPoint in subPoints) {
				if (old.isMountainous(oldPoint)) {
					retval.setMountainous(point, true);
				}
				if (exists ground = old.getGround(oldPoint)) {
					if (retval.getGround(point) exists) {
						retval.addFixture(point, ground);
					} else {
						retval.setGround(point, ground);
					}
				}
				if (exists forest = old.getForest(oldPoint)) {
					if (retval.getForest(point) exists) {
						retval.addFixture(point, forest);
					} else {
						retval.setForest(point, forest);
					}
				}
				for (fixture in old.getOtherFixtures(oldPoint)) {
					retval.addFixture(point, fixture);
				}
			}
			MutableSet<River> upperLeftRivers =HashSet<River> {
				*old.getRivers(subPoints[0]) };
			MutableSet<River> upperRightRivers = HashSet<River> {
				*old.getRivers(subPoints[1]) };
			MutableSet<River> lowerLeftRivers =HashSet<River> {
				*old.getRivers(subPoints[2]) };
			MutableSet<River> lowerRightRivers = HashSet<River> {
				*old.getRivers(subPoints[3]) };
			upperLeftRivers.removeAll({River.east, River.south});
			upperRightRivers.removeAll({River.west, River.south});
			lowerLeftRivers.removeAll({River.east, River.north});
			lowerRightRivers.removeAll({River.west, River.north});
			retval.addRivers(point, *({ upperLeftRivers, upperRightRivers, lowerLeftRivers,
				lowerRightRivers}.reduce((Set<River> partial, Set<River> element) =>
			partial.union(element))));
		}
	}
	return retval;
}
test
void testResolutionReduction() {
	IMutableMapNG start = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), 0);
	Animal fixture = Animal("animal", false, true, "domesticated", 1);
	initialize(start, pointFactory(0, 0), TileType.desert, fixture);
	CacheFixture fixtureTwo = CacheFixture("gemstones", "small", 2);
	initialize(start, pointFactory(0, 1), TileType.desert, fixtureTwo);
	IUnit fixtureThree = Unit(PlayerImpl(0, "A. Player"), "legion", "eagles", 3);
	initialize(start, pointFactory(1, 0), TileType.desert, fixtureThree);
	Fortress fixtureFour = Fortress(PlayerImpl(1, "B. Player"), "HQ", 4, TownSize.small);
	initialize(start, pointFactory(1, 1), TileType.plains, fixtureFour);

	IMapNG converted = decreaseResolution(start);
	Point zeroPoint = pointFactory(0, 0);
	assertTrue(converted.getOtherFixtures(zeroPoint)
		.containsEvery({fixture, fixtureTwo, fixtureThree, fixtureFour}),
		"Combined tile should contain fixtures from all four original tiles");
	assertEquals(converted.getBaseTerrain(zeroPoint), TileType.desert,
		"Combined tile has type of most of input tiles");
}
test
void testMoreReduction() {
	IMutableMapNG start = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), 0);
	Point pointOne = pointFactory(0, 0);
	start.setMountainous(pointOne, true);
	start.addRivers(pointOne, River.east, River.south);
	Ground groundOne = Ground(-1, "groundOne", false);
	initialize(start, pointOne, TileType.steppe, groundOne);
	Point pointTwo = pointFactory(0, 1);
	start.addRivers(pointTwo, River.lake);
	Ground groundTwo = Ground(-1, "groundTwo", false);
	initialize(start, pointTwo, TileType.steppe, groundTwo);
	Point pointThree = pointFactory(1, 0);
	Forest forestOne = Forest("forestOne", false, 1);
	initialize(start, pointThree, TileType.plains, forestOne);
	Point pointFour = pointFactory(1, 1);
	Forest forestTwo = Forest("forestTwo", false, 2);
	initialize(start, pointFour, TileType.desert, forestTwo);

	IMapNG converted = decreaseResolution(start);
	Point zeroPoint = pointFactory(0, 0);
	assertTrue(converted.isMountainous(zeroPoint),
		"One mountainous point makes the reduced point mountainous");
	assertEquals(converted.getGround(zeroPoint), groundOne, "Ground carries over");
	assertEquals(converted.getForest(zeroPoint), forestOne, "Forest carries over");
	assertTrue(converted.getOtherFixtures(zeroPoint)
		.containsEvery({groundTwo, forestTwo}),
		"Ground and forest carry over even when already set");
	assertTrue(converted.getRivers(zeroPoint)
		.containsEvery({River.lake, River.east, River.south}),
		"Non-interior rivers carry over");
	// TODO: ensure that the original included some interior rivers
	assertTrue(!converted.getRivers(zeroPoint).containsAny({River.north, River.west}),
		"Interior rivers do not carry over");
	assertEquals(converted.getBaseTerrain(zeroPoint), TileType.steppe,
		"Combined tile has most common terrain type among inputs");
}
test
void testResolutionDecreaseRequirement() {
	assertThatException(
				() => decreaseResolution(SPMapNG(MapDimensionsImpl(3, 3, 2),
			PlayerCollection(), -1)))
		.hasType(`IllegalArgumentException`);
}