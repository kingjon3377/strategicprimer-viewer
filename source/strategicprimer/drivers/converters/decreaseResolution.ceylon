import ceylon.test {
    assertEquals,
    assertTrue,
    test,
    assertThatException,
    assertFalse
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
    Unit,
	AnimalImpl
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
	"Can only convert maps with even numbers of rows and columns"
	assert (old.dimensions.rows % 2 == 0, old.dimensions.columns % 2 == 0);
    PlayerCollection players = PlayerCollection();
    for (player in old.players) {
        players.add(player);
    }
    Integer newColumns = old.dimensions.columns / 2;
    Integer newRows = old.dimensions.rows / 2;
    IMutableMapNG retval = SPMapNG(MapDimensionsImpl(newRows, newColumns, 2), players,
        old.currentTurn);
    TileType? consensus([TileType?+] types) {
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
            assert (exists [count, retval] = counted.first);
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
            retval.baseTerrain[point] =
                consensus(subPoints.collect(old.baseTerrain.get));
            for (oldPoint in subPoints) {
                if (exists mtn = old.mountainous[oldPoint], mtn) {
                    retval.mountainous[point] = true;
                }
//                for (fixture in old.fixtures[oldPoint]) {
                for (fixture in old.fixtures.get(oldPoint)) {
                    retval.addFixture(point, fixture);
                }
            }
            Set<River> upperLeftRivers = set(
//                old.rivers[subPoints[0]]).complement(set([River.east, River.south])); // TODO: syntax sugar once Ceylon bug #4517 fixed
                old.rivers.get(subPoints[0])).complement(set([River.east, River.south]));
            Set<River> upperRightRivers = set(
//                old.rivers[subPoints[1]]).complement(set([River.west, River.south]));
                old.rivers.get(subPoints[1])).complement(set([River.west, River.south]));
            Set<River> lowerLeftRivers = set(
//                old.rivers[subPoints[2]]).complement(set([River.east, River.north]));
                old.rivers.get(subPoints[2])).complement(set([River.east, River.north]));
            Set<River> lowerRightRivers = set(
//                old.rivers[subPoints[3]]).complement(set([River.west, River.north]));
                old.rivers.get(subPoints[3])).complement(set([River.west, River.north]));
            retval.addRivers(point, *upperLeftRivers.union(upperRightRivers)
                .union(lowerLeftRivers).union(lowerRightRivers));
        }
    }
    return retval;
}
object resolutionDecreaseTests {
	void initialize(IMutableMapNG map, Point point, TileType? terrain,
	        TileFixture* fixtures) {
	    if (exists terrain) {
	        map.baseTerrain[point] = terrain;
	    }
	    for (fixture in fixtures) {
	        map.addFixture(point, fixture);
	    }
	}
	test
	shared void testResolutionReduction() {
	    IMutableMapNG start = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), 0);
	    Animal fixture = AnimalImpl("animal", false, true, "domesticated", 1);
	    initialize(start, pointFactory(0, 0), TileType.desert, fixture);
	    CacheFixture fixtureTwo = CacheFixture("gemstones", "small", 2);
	    initialize(start, pointFactory(0, 1), TileType.desert, fixtureTwo);
	    IUnit fixtureThree = Unit(PlayerImpl(0, "A. Player"), "legion", "eagles", 3);
	    initialize(start, pointFactory(1, 0), TileType.desert, fixtureThree);
	    Fortress fixtureFour = Fortress(PlayerImpl(1, "B. Player"), "HQ", 4, TownSize.small);
	    initialize(start, pointFactory(1, 1), TileType.plains, fixtureFour);

	    IMapNG converted = decreaseResolution(start);
	    Point zeroPoint = pointFactory(0, 0);
	//    assertTrue(converted.fixtures[zeroPoint] // TODO: syntax sugar once compiler bug fixed
	    assertTrue(converted.fixtures.get(zeroPoint)
	        .containsEvery([fixture, fixtureTwo, fixtureThree, fixtureFour]),
	        "Combined tile should contain fixtures from all four original tiles");
	    assertEquals(converted.baseTerrain[zeroPoint], TileType.desert,
	        "Combined tile has type of most of input tiles");
	}
	test
	shared void testMoreReduction() {
	    IMutableMapNG start = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), 0);
	    Point pointOne = pointFactory(0, 0);
	    start.mountainous[pointOne] = true;
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
	//    assertTrue(converted.mountainous[zeroPoint], // TODO: syntax sugar once compiler bug fixed
	    assertTrue(converted.mountainous.get(zeroPoint),
	        "One mountainous point makes the reduced point mountainous");
	    assertEquals(converted.fixtures[zeroPoint]?.narrow<Ground>()?.first, groundOne,
	        "Ground carries over");
	    assertEquals(converted.fixtures[zeroPoint]?.narrow<Forest>()?.first, forestOne,
	        "Forest carries over");
	//    assertTrue(converted.fixtures[zeroPoint]
	    assertTrue(converted.fixtures.get(zeroPoint)
	        .containsEvery([groundTwo, forestTwo]),
	        "Ground and forest carry over even when already set");
	//    assertTrue(converted.rivers[zeroPoint]
	    assertTrue(converted.rivers.get(zeroPoint)
	        .containsEvery([River.lake, River.north]),
	        "Non-interior rivers carry over");
	//    assertFalse(converted.rivers[zeroPoint].containsAny([River.east, River.south]),
	    assertFalse(converted.rivers.get(zeroPoint).containsAny([River.east, River.south]),
	        "Interior rivers do not carry over");
	    assertEquals(converted.baseTerrain[zeroPoint], TileType.steppe,
	        "Combined tile has most common terrain type among inputs");
	}
	test
	shared void testResolutionDecreaseRequirement() {
	    // TODO: Uncomment hasType() once Ceylon compiler bug #5448 fixed
	    assertThatException(
	                () => decreaseResolution(SPMapNG(MapDimensionsImpl(3, 3, 2),
	            PlayerCollection(), -1)))
	        /*.hasType(`IllegalArgumentException`)*/;
	}
}