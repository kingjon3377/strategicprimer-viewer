import ceylon.test {
    assertTrue,
    test,
    assertFalse,
    assertEquals,
    assertNotEquals
}

import strategicprimer.model.map {
    Point,
    IFixture,
    River,
    Subsettable,
    IPlayerCollection,
    IMutablePlayerCollection,
    TileType,
    IMutableMapNG,
    IMapNG,
    TileFixture,
    pointFactory,
    PlayerCollection,
    PlayerImpl,
    SPMapNG,
    MapDimensionsImpl
}
import strategicprimer.model.map.fixtures {
    TextFixture
}
import strategicprimer.model.map.fixtures.mobile {
    Unit,
    Worker,
    Animal
}
import strategicprimer.model.map.fixtures.mobile.worker {
    Job
}
import strategicprimer.model.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    TownSize,
    Fortress,
    AbstractTown,
    TownStatus,
    Fortification
}
object subsetTests {
	void assertIsSubset<T,U=T>(T&U one, T&U two, String message)
	        given T satisfies Subsettable<U> given U satisfies Object =>
	            assertTrue(one.isSubset(two, noop), message);
	void assertNotSubset<T,U=T>(T&U one, T&U two, String message)
	        given T satisfies Subsettable<U> given U satisfies Object =>
	        assertFalse(one.isSubset(two, noop), message);
	"A test of [[PlayerCollection]]'s subset feature"
	test
	shared void testPlayerCollectionSubset() {
	    IMutablePlayerCollection firstCollection = PlayerCollection();
	    firstCollection.add(PlayerImpl(1, "one"));
	    IMutablePlayerCollection secondCollection = PlayerCollection();
	    secondCollection.add(PlayerImpl(1, "one"));
	    secondCollection.add(PlayerImpl(2, "two"));
	    IPlayerCollection zero = PlayerCollection();
	    assertIsSubset(zero, zero, "Empty is subset of self");
	    assertIsSubset(firstCollection, zero, "Empty is subset of one");
	    assertIsSubset(secondCollection, zero, "Empty is subset of two");
	    assertNotSubset(zero, firstCollection, "One is not subset of empty");
	    assertIsSubset<IPlayerCollection, {Player*}>(firstCollection, firstCollection,
	        "One is subset of self");
	    assertIsSubset<IPlayerCollection, {Player*}>(secondCollection, firstCollection,
	        "One is subset of two");
	    assertNotSubset(zero, secondCollection, "Two is not subset of empty");
	    assertNotSubset<IPlayerCollection, {Player*}>(firstCollection, secondCollection,
	        "Two is not subset of one");
	    assertIsSubset<IPlayerCollection, {Player*}>(secondCollection, secondCollection,
	        "Two is subset of self");
	}

	"A test of [[Fortress]]'s subset feature."
	test
	shared void testFortressSubset() {
	    void requireMatching(Fortress one, Fortress two, String what) {
	        assertNotSubset<Fortress, IFixture>(one, two,
	            "Subset requires ``what``, first test");
	        assertNotSubset<Fortress, IFixture>(two, one,
	            "Subset requires ``what``, second test");
	    }
	    Integer fortId = 1;
	    Fortress firstFort = Fortress(PlayerImpl(1, "one"), "fOne", fortId, TownSize.small);
	    requireMatching(firstFort,
	        Fortress(PlayerImpl(2, "two"), "fOne", fortId, TownSize.small), "same owner");
	    requireMatching(firstFort,
	        Fortress(PlayerImpl(1, "one"), "fTwo", fortId, TownSize.small), "same name");
	    requireMatching(firstFort,
	        Fortress(PlayerImpl(1, "one"), "fOne", 3, TownSize.small),
	        "identity or ID equality");
	    Fortress fifthFort = Fortress(PlayerImpl(1, "one"), "fOne", fortId, TownSize.small);
	    fifthFort.addMember(Unit(PlayerImpl(2, "two"), "unit_type", "unit_name", 4));
	    assertIsSubset<Fortress, IFixture>(fifthFort, firstFort,
	        "Fortress without is a subset of fortress with unit");
	    assertNotSubset<Fortress, IFixture>(firstFort, fifthFort,
	        "Fortress with is not a subset of fortress without unit");
	    Fortress sixthFort = Fortress(PlayerImpl(1, "one"), "unknown", fortId,
	        TownSize.small);
	    assertIsSubset<Fortress, IFixture>(firstFort, sixthFort,
	        """Fortress named "unknown" can be subset""");
	    assertNotSubset<Fortress, IFixture>(sixthFort, firstFort,
	        """"unknown" is not commutative""");
	    Fortress seventhFort = Fortress(PlayerImpl(1, "one"), "unknown", fortId,
	        TownSize.medium);
	    assertNotSubset<Fortress, IFixture>(sixthFort, seventhFort,
	        "Different size breaks Fortress subset");
	    assertNotSubset<Fortress, IFixture>(seventhFort, sixthFort,
	        "Different size breaks Fortress subset");
	}

	"Test the [[IMapNG]] subset feature"
	test
	shared void testMapSubset() {
	    IMutableMapNG createMap(<Point->TileType>* terrain) {
	        IMutableMapNG retval = SPMapNG(MapDimensionsImpl(2, 2, 2),
	            PlayerCollection(), -1);
	        for (point->type in terrain) {
	            retval.baseTerrain[point] = type;
	        }
	        return retval;
	    }
	    IMutableMapNG firstMap = createMap(pointFactory(0, 0)->TileType.jungle);
	    IMutableMapNG secondMap = createMap(pointFactory(0, 0)->TileType.jungle,
	        pointFactory(1, 1)->TileType.ocean);
	    IMapNG zero = createMap();
	    assertIsSubset(zero, zero, "None is a subset of itself");
	    assertIsSubset(firstMap, zero, "None is a subset of one");
	    assertIsSubset(secondMap, zero, "None is a subset of one");
	    assertNotSubset(zero, firstMap, "One is not a subset of none");
	    assertIsSubset(firstMap, firstMap, "One is a subset of itself");
	    assertIsSubset(secondMap, firstMap, "One is a subset of two");
	    assertNotSubset(zero, secondMap, "Two is not a subset of none");
	    assertNotSubset(firstMap, secondMap, "Two is not a subset of one");
	    assertIsSubset(secondMap, secondMap, "Two is a subset of itself");
	    firstMap.baseTerrain[pointFactory(1, 1)] = TileType.plains;
	    assertNotSubset(secondMap, firstMap,
	        "Corresponding but non-matching tile breaks subset");
	    assertNotSubset(firstMap, secondMap,
	        "Corresponding but non-matching tile breaks subset");
	    secondMap.baseTerrain[pointFactory(1, 1)] = TileType.plains;
	    assertIsSubset(secondMap, firstMap, "Subset again after resetting terrain");
	    firstMap.addFixture(pointFactory(1, 1), CacheFixture("category", "contents", 3));
	    assertIsSubset(secondMap, firstMap, "Subset calculation ignores caches");
	    firstMap.addFixture(pointFactory(1, 1), TextFixture("text", -1));
	    assertIsSubset(secondMap, firstMap, "Subset calculation ignores text fixtures");
	    firstMap.addFixture(pointFactory(1, 1), Animal("animal", true, false, "status", 5));
	    assertIsSubset(secondMap, firstMap, "Subset calculation ignores animal tracks");
	    firstMap.addFixture(pointFactory(1, 1), Animal("animal", false, true, "status", 7));
	    assertNotSubset(secondMap, firstMap,
	        "Subset calculation does not ignore other fixtures");
	}

	"Test map subset calculations to ensure that off-by-one errors are caught."
	test
	shared void testMapOffByOne() {
	    IMutableMapNG baseMap = SPMapNG(MapDimensionsImpl(2, 2, 2),
	        PlayerCollection(), -1);
	    for (point in baseMap.locations) {
	        baseMap.baseTerrain[point] = TileType.plains;
	    }
	    baseMap.addFixture(pointFactory(1, 1), Forest("elm", false, 1));
	    baseMap.addFixture(pointFactory(1, 1), Animal("skunk", false, false, "wild", 2));
	    baseMap.addRivers(pointFactory(1, 1), River.east);

	    IMutableMapNG testMap = SPMapNG(MapDimensionsImpl(2, 2, 2),
	        PlayerCollection(), -1);
	    for (point in testMap.locations) {
	        testMap.baseTerrain[point] = TileType.plains;
	    }
	    Forest forest = Forest("elm", false, 1);
	    TileFixture animal = Animal("skunk", false, false, "wild", 2);
	    for (point in testMap.locations) {
	        assertIsSubset(baseMap, testMap,
	            "Subset invariant before attempt using ``point``");
	        Anything(IMapNG, IMapNG, String) testMethod;
	        String result;
	        if (pointFactory(1, 1) == point) {
	            testMethod = assertIsSubset<IMapNG>;
	            result = "Subset holds when fixture(s) placed correctly";
	        } else {
	            testMethod = assertNotSubset<IMapNG>;
	            result = "Subset fails when fixture(s) off by one";
	        }
	        testMap.addFixture(point, forest);
	        testMethod(baseMap, testMap, result);
	        testMap.removeFixture(point, forest);
	        testMap.addFixture(point, animal);
	        testMethod(baseMap, testMap, result);
	        testMap.removeFixture(point, animal);
	        testMap.addRivers(point, River.east);
	        testMethod(baseMap, testMap, result);
	        testMap.removeRivers(point, River.east);
	        assertIsSubset(baseMap, testMap,
	            "Subset invariant after attempt using ``point``");
	    }
	}

	"Test interaction between isSubset() and copy()."
	test
	shared void testSubsetsAndCopy() {
	    IMutableMapNG firstMap = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), -1);
	    firstMap.baseTerrain[pointFactory(0, 0)] = TileType.jungle;
	    IMapNG zero = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), -1);
	    assertIsSubset(firstMap, zero, "zero is a subset of one before copy");
	    IMutableMapNG secondMap = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), -1);
	    secondMap.baseTerrain[pointFactory(0, 0)] = TileType.jungle;
	    firstMap.baseTerrain[pointFactory(1, 1)] = TileType.plains;
	    secondMap.baseTerrain[pointFactory(1, 1)] = TileType.plains;
	    firstMap.addFixture(pointFactory(1, 1), CacheFixture("category", "contents", 3));
	    firstMap.addFixture(pointFactory(1, 1), TextFixture("text", -1));
	    firstMap.addFixture(pointFactory(1, 1), Animal("animal", true, false, "status", 5));
	    firstMap.addFixture(pointFactory(0, 0),
	        Fortification(TownStatus.burned, TownSize.large, 15, "fortification", 6,
	            PlayerImpl(0, "")));
	    assertEquals(firstMap.copy(false, null), firstMap, "Cloned map equals original");
	    IMapNG clone = firstMap.copy(true, null);
	    assertIsSubset(clone, zero, "unfilled map is still a subset of zeroed clone");
	    // DCs, the only thing zeroed out in *map* copy() at the moment, are ignored by
	    // equals().
	//    for (fixture in clone.fixtures[pointFactory(0, 0)]) { // TODO: syntax sugar once compiler bug fixed
	    for (fixture in clone.fixtures.get(pointFactory(0, 0))) {
	        if (is AbstractTown fixture) {
	            assertEquals(fixture.dc, 0, "Copied map didn't copy DCs");
	        }
	    }
	    Unit uOne = Unit(PlayerImpl(0, ""), "type", "name", 7);
	    uOne.addMember(Worker("worker", "dwarf", 8, Job("job", 1)));
	    assertEquals(uOne.copy(false), uOne, "clone equals original");
	    assertNotEquals(uOne.copy(true), uOne, "zeroed clone doesn't equal original");
	    assertIsSubset(uOne, uOne.copy(true), "zeroed clone is subset of original");
	}
}