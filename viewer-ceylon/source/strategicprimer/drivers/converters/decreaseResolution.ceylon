import ceylon.collection {
    MutableSet,
    HashSet
}
import ceylon.test {
    assertEquals,
    assertTrue,
    test,
    assertThatException,
    assertFalse
}

import java.lang {
    IllegalArgumentException
}


import strategicprimer.model.map {
    Point,
    River,
    TileType,
    IMutableMapNG,
    pointFactory,
    IMapNG,
    PlayerCollection,
    SPMapNG,
    MapDimensionsImpl,
    PlayerImpl,
    TileFixture
}
import strategicprimer.model.map.fixtures {
    Ground
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    Animal,
    Unit
}
import strategicprimer.model.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    TownSize,
    Fortress
}
import ceylon.random {
    randomize
}
"A utility to convert a map to an equivalent half-resolution one."
shared IMapNG decreaseResolution(IMapNG old) {
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
		value counted = types.frequencies().map((type->count) => [count, type])
			.sort(comparing(
					([Integer, TileType] first, [Integer, TileType] second) =>
						first.first <=> second.first,
					([Integer, TileType] first, [Integer, TileType] second) =>
						first.rest.first.xml <=> second.rest.first.xml)).reversed;
		assert (exists largestCount = counted.first?.first);
		value matchingCount = counted.filter((item) => item.first == largestCount);
		if (matchingCount.size > 1) {
			assert (exists retval = randomize(matchingCount.map(
						([Integer count, TileType type]) => type)).first);
			return retval;
		} else {
			assert (exists retval = counted.first?.rest?.first);
			return retval;
		}
	}
	for (row in 0:newRows) {
		for (column in 0:newColumns) {
			Point point = pointFactory(row, column);
			Point[4] subPoints = [pointFactory(row * 2, column * 2),
				pointFactory(row * 2, (column * 2) + 1),
				pointFactory((row * 2) + 1, column * 2),
				pointFactory((row * 2) + 1, (column * 2) + 1) ];
			retval.setBaseTerrain(point,
				consensus([ *subPoints.map(old.baseTerrain) ]));
			for (oldPoint in subPoints) {
				if (old.mountainous(oldPoint)) {
					retval.setMountainous(point, true);
				}
				if (exists ground = old.ground(oldPoint)) {
					if (retval.ground(point) exists) {
						retval.addFixture(point, ground);
					} else {
						retval.setGround(point, ground);
					}
				}
				if (exists forest = old.forest(oldPoint)) {
					if (retval.forest(point) exists) {
						retval.addFixture(point, forest);
					} else {
						retval.setForest(point, forest);
					}
				}
				for (fixture in old.otherFixtures(oldPoint)) {
					retval.addFixture(point, fixture);
				}
			}
			MutableSet<River> upperLeftRivers = HashSet<River> {
				*old.rivers(subPoints[0]) };
			MutableSet<River> upperRightRivers = HashSet<River> {
				*old.rivers(subPoints[1]) };
			MutableSet<River> lowerLeftRivers =HashSet<River> {
				*old.rivers(subPoints[2]) };
			MutableSet<River> lowerRightRivers = HashSet<River> {
				*old.rivers(subPoints[3]) };
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
void initialize(IMutableMapNG map, Point point, TileType? terrain, TileFixture* fixtures) {
	if (exists terrain, terrain != TileType.notVisible) {
		map.setBaseTerrain(point, terrain);
	}
	for (fixture in fixtures) {
		if (is Ground fixture, !map.ground(point) exists) {
			map.setGround(point, fixture);
		} else if (is Forest fixture, !map.forest(point) exists) {
			map.setForest(point, fixture);
		} else {
			map.addFixture(point, fixture);
		}
	}
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
	assertTrue(converted.otherFixtures(zeroPoint)
		.containsEvery({fixture, fixtureTwo, fixtureThree, fixtureFour}),
		"Combined tile should contain fixtures from all four original tiles");
	assertEquals(converted.baseTerrain(zeroPoint), TileType.desert,
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
	start.addRivers(pointTwo, River.north, River.lake);
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
	assertTrue(converted.mountainous(zeroPoint),
		"One mountainous point makes the reduced point mountainous");
	assertEquals(converted.ground(zeroPoint), groundOne, "Ground carries over");
	assertEquals(converted.forest(zeroPoint), forestOne, "Forest carries over");
	assertTrue(converted.otherFixtures(zeroPoint)
		.containsEvery({groundTwo, forestTwo}),
		"Ground and forest carry over even when already set");
	assertTrue(converted.rivers(zeroPoint)
		.containsEvery({River.lake, River.north}),
		"Non-interior rivers carry over");
	assertFalse(converted.rivers(zeroPoint).containsAny({River.east, River.south}),
		"Interior rivers do not carry over");
	assertEquals(converted.baseTerrain(zeroPoint), TileType.steppe,
		"Combined tile has most common terrain type among inputs");
}
test
void testResolutionDecreaseRequirement() {
	// TODO: Uncomment hasType() once Ceylon tooling bug fixed
	assertThatException(
				() => decreaseResolution(SPMapNG(MapDimensionsImpl(3, 3, 2),
			PlayerCollection(), -1)))
		/*.hasType(`IllegalArgumentException`)*/;
}