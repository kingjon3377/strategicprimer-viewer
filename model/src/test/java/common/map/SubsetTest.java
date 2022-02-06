package common.map;

import org.javatuples.Pair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import common.map.fixtures.TextFixture;
import common.map.fixtures.mobile.Worker;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.mobile.AnimalImpl;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.worker.Job;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.terrain.Forest;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.FortressImpl;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.Fortification;
import common.map.fixtures.towns.Town;
import common.map.fixtures.towns.City;
import common.map.fixtures.towns.CommunityStats;

import java.util.logging.Logger;
import java.util.function.Consumer;

/**
 * A collection of tests of the subset-checking features.
 */
public final class SubsetTest {
	private final Logger LOGGER = Logger.getLogger(SubsetTest.class.getName());
	// FIXME: Drop the ostream parameters from these assertion methods now we don't need them (and the interface below)
	/**
	 * Assert that {@link two} is a "strict subset," by our loose definition, of {@link one}.
	 */
	private static <SpecificType, GeneralType extends Subsettable<SpecificType>>
			void assertIsSubset(final GeneralType one, final SpecificType two, final String message,
			                    final Consumer<String> ostream) {
		assertTrue(one.isSubset(two, ostream), message);
	}

	/**
	 * Assert that {@link two} is <em>not</em> a "strict subset," even by our loose definition,
	 * of {@link one}.
	 */
	private static <SpecificType, GeneralType extends Subsettable<SpecificType>>
			void assertNotSubset(final GeneralType one, final SpecificType two, final String message,
			                     final Consumer<String> ostream) {
		assertFalse(one.isSubset(two, ostream), message);
	}

	private static void noop(final String str) {}

	/**
	 * A test of {@link PlayerCollection}'s subset feature
	 */
	@Test
	public void testPlayerCollectionSubset() {
		// TODO: Make a way to initialize one declaratively
		final IMutablePlayerCollection firstCollection = new PlayerCollection();
		firstCollection.add(new PlayerImpl(1, "one"));
		final IMutablePlayerCollection secondCollection = new PlayerCollection();
		secondCollection.add(new PlayerImpl(1, "one"));
		secondCollection.add(new PlayerImpl(2, "two"));
		final IPlayerCollection zero = new PlayerCollection();
		assertIsSubset(zero, zero, "Empty is subset of self", SubsetTest::noop);
		assertIsSubset(firstCollection, zero, "Empty is subset of one", SubsetTest::noop);
		assertIsSubset(secondCollection, zero, "Empty is subset of two", SubsetTest::noop);
		assertNotSubset(zero, firstCollection, "One is not subset of empty", SubsetTest::noop);
		assertIsSubset(firstCollection, firstCollection, "One is subset of self",
			SubsetTest::noop);
		assertIsSubset(secondCollection, firstCollection, "One is subset of two",
			SubsetTest::noop);
		assertNotSubset(zero, secondCollection, "Two is not subset of empty", SubsetTest::noop);
		assertNotSubset(firstCollection, secondCollection, "Two is not subset of one",
			SubsetTest::noop);
		assertIsSubset(secondCollection, secondCollection, "Two is subset of self",
			SubsetTest::noop);
	}

	/**
	 * Assert that neither of two {@link IFortress fortresses} is a subset of the other.
	 */
	private static void requireMatching(final IFortress one, final IFortress two, final String what) {
		assertNotSubset(one, two,
			String.format("Subset requires %s, first test", what), SubsetTest::noop);
		assertNotSubset(two, one,
			String.format("Subset requires %s, second test", what), SubsetTest::noop);
	}

	/**
	 * A test of {@link IFortress a fortress's} subset feature.
	 */
	@Test
	public void testFortressSubset() {
		final int fortId = 1; // TODO: Take constants as parameters?
		final IFortress firstFort = new FortressImpl(new PlayerImpl(1, "one"), "fOne", fortId,
			TownSize.Small);
		requireMatching(firstFort,
			new FortressImpl(new PlayerImpl(2, "two"), "fOne", fortId, TownSize.Small),
			"same owner");
		requireMatching(firstFort,
			new FortressImpl(new PlayerImpl(1, "one"), "fTwo", fortId, TownSize.Small),
			"same name");
		requireMatching(firstFort,
			new FortressImpl(new PlayerImpl(1, "one"), "fOne", 3, TownSize.Small),
			"identity or ID equality");
		final IMutableFortress fifthFort = new FortressImpl(new PlayerImpl(1, "one"), "fOne", fortId,
			TownSize.Small);
		fifthFort.addMember(new Unit(new PlayerImpl(2, "two"), "unit_type", "unit_name", 4));
		assertIsSubset(fifthFort, firstFort,
			"Fortress without is a subset of fortress with unit", SubsetTest::noop);
		assertNotSubset(firstFort, fifthFort,
			"Fortress with is not a subset of fortress without unit", SubsetTest::noop);
		final IFortress sixthFort = new FortressImpl(new PlayerImpl(1, "one"), "unknown", fortId,
			TownSize.Small);
		assertIsSubset(firstFort, sixthFort,
			"Fortress named \"unknown\" can be subset", SubsetTest::noop);
		assertNotSubset(sixthFort, firstFort,
			"\"unknown\" is not commutative", SubsetTest::noop);
		final IFortress seventhFort = new FortressImpl(new PlayerImpl(1, "one"), "unknown", fortId,
			TownSize.Medium);
		assertNotSubset(sixthFort, seventhFort,
			"Different size breaks Fortress subset", SubsetTest::noop);
		assertNotSubset(seventhFort, sixthFort,
			"Different size breaks Fortress subset", SubsetTest::noop);
	}

	/**
	 * Create a map with the given terrain.
	 */
	@SafeVarargs
	private static IMutableMapNG createMap(final Pair<Point, TileType>... terrain) {
		final IMutableMapNG retval = new SPMapNG(new MapDimensionsImpl(2, 2, 2),
			new PlayerCollection(), -1);
		for (Pair<Point, TileType> pair : terrain) {
			retval.setBaseTerrain(pair.getValue0(), pair.getValue1());
		}
		return retval;
	}

	/**
	 * Test the {@link IMapNG} subset feature
	 */
	@Test
	public void testMapSubset() {
		final IMutableMapNG firstMap = createMap(Pair.with(new Point(0, 0), TileType.Jungle));
		final IMutableMapNG secondMap = createMap(Pair.with(new Point(0, 0), TileType.Jungle),
			Pair.with(new Point(1, 1), TileType.Ocean));
		IMapNG zero = createMap();
		assertIsSubset(zero, zero, "None is a subset of itself", SubsetTest::noop);
		assertIsSubset(firstMap, zero, "None is a subset of one", SubsetTest::noop);
		assertIsSubset(secondMap, zero, "None is a subset of one", SubsetTest::noop);
		assertNotSubset(zero, firstMap, "One is not a subset of none", SubsetTest::noop);
		assertIsSubset(firstMap, firstMap, "One is a subset of itself", SubsetTest::noop);
		assertIsSubset(secondMap, firstMap, "One is a subset of two", SubsetTest::noop);
		assertNotSubset(zero, secondMap, "Two is not a subset of none", SubsetTest::noop);
		assertNotSubset(firstMap, secondMap, "Two is not a subset of one", SubsetTest::noop);
		assertIsSubset(secondMap, secondMap, "Two is a subset of itself", SubsetTest::noop);
		firstMap.setBaseTerrain(new Point(1, 1), TileType.Plains);
		assertNotSubset(secondMap, firstMap,
			"Corresponding but non-matching tile breaks subset", SubsetTest::noop);
		assertNotSubset(firstMap, secondMap,
			"Corresponding but non-matching tile breaks subset", SubsetTest::noop);
		secondMap.setBaseTerrain(new Point(1, 1), TileType.Plains);
		assertIsSubset(secondMap, firstMap, "Subset again after resetting terrain", SubsetTest::noop);
		firstMap.addFixture(new Point(1, 1), new CacheFixture("category", "contents", 3));
		assertIsSubset(secondMap, firstMap, "Subset calculation ignores caches", SubsetTest::noop);
		firstMap.addFixture(new Point(1, 1), new TextFixture("text", -1));
		assertIsSubset(secondMap, firstMap, "Subset calculation ignores text fixtures", SubsetTest::noop);
		firstMap.addFixture(new Point(1, 1), new AnimalTracks("animal"));
		assertIsSubset(secondMap, firstMap, "Subset calculation ignores animal tracks", SubsetTest::noop);
		firstMap.addFixture(new Point(1, 1), new AnimalImpl("animal", true, "status", 7));
		assertNotSubset(secondMap, firstMap,
			"Subset calculation does not ignore other fixtures", SubsetTest::noop);
	}

	@FunctionalInterface
	private static interface ITestMethod {
		void apply(IMapNG one, IMapNG two, String message, Consumer<String> ostream);
	}

	/**
	 * Test map subset calculations to ensure that off-by-one errors are caught.
	 */
	@Test
	public void testMapOffByOne() {
		final IMutableMapNG baseMap = new SPMapNG(new MapDimensionsImpl(2, 2, 2),
			new PlayerCollection(), -1);
		for (Point point : baseMap.getLocations()) {
			baseMap.setBaseTerrain(point, TileType.Plains);
		}
		baseMap.addFixture(new Point(1, 1), new Forest("elm", false, 1));
		baseMap.addFixture(new Point(1, 1), new AnimalImpl("skunk", false, "wild", 2));
		baseMap.addRivers(new Point(1, 1), River.East);

		final IMutableMapNG testMap = new SPMapNG(new MapDimensionsImpl(2, 2, 2),
			new PlayerCollection(), -1);
		for (Point point : testMap.getLocations()) {
			testMap.setBaseTerrain(point, TileType.Plains);
		}
		final Forest forest = new Forest("elm", false, 1);
		final TileFixture animal = new AnimalImpl("skunk", false, "wild", 2);
		for (Point point : testMap.getLocations()) {
			assertIsSubset(baseMap, testMap,
				"Subset invariant before attempt using " + point, SubsetTest::noop);
			final ITestMethod testMethod;
			final String result;
			if (point.equals(new Point(1, 1))) {
				testMethod = SubsetTest::assertIsSubset;
				result = "Subset holds when fixture(s) placed correctly";
			} else {
				testMethod = SubsetTest::assertNotSubset;
				result = "Subset fails when fixture(s) off by one";
			}
			testMap.addFixture(point, forest);
			testMethod.apply(baseMap, testMap, result, SubsetTest::noop);
			testMap.removeFixture(point, forest);
			testMap.addFixture(point, animal);
			testMethod.apply(baseMap, testMap, result, SubsetTest::noop);
			testMap.removeFixture(point, animal);
			testMap.addRivers(point, River.East);
			testMethod.apply(baseMap, testMap, result, SubsetTest::noop);
			testMap.removeRivers(point, River.East);
			assertIsSubset(baseMap, testMap,
				"Subset invariant after attempt using " + point, SubsetTest::noop);
		}
	}

	/**
	 * Test interaction between isSubset() and copy().
	 */
	@Test
	public void testSubsetsAndCopy() {
		final IMutableMapNG firstMap = new SPMapNG(new MapDimensionsImpl(2, 2, 2),
			new PlayerCollection(), -1);
		firstMap.setBaseTerrain(new Point(0, 0), TileType.Jungle);
		final IMapNG zero = new SPMapNG(new MapDimensionsImpl(2, 2, 2),
			new PlayerCollection(), -1);
		assertIsSubset(firstMap, zero, "zero is a subset of one before copy", SubsetTest::noop);
		IMutableMapNG secondMap =
			new SPMapNG(new MapDimensionsImpl(2, 2, 2), new PlayerCollection(), -1);
		secondMap.setBaseTerrain(new Point(0, 0), TileType.Jungle);
		firstMap.setBaseTerrain(new Point(1, 1), TileType.Plains);
		secondMap.setBaseTerrain(new Point(1, 1), TileType.Plains);
		firstMap.addFixture(new Point(1, 1), new CacheFixture("category", "contents", 3));
		firstMap.addFixture(new Point(1, 1), new TextFixture("text", -1));
		firstMap.addFixture(new Point(1, 1), new AnimalTracks("animal"));
		firstMap.addFixture(new Point(0, 0),
			new Fortification(TownStatus.Burned, TownSize.Large, 15, "fortification", 6,
				new PlayerImpl(0, "")));
		assertEquals(firstMap, firstMap.copy(false, null), "Cloned map equals original");
		final IMapNG clone = firstMap.copy(true, null);
		assertIsSubset(clone, zero, "unfilled map is still a subset of zeroed clone", SubsetTest::noop);
		// DCs, the only thing zeroed out in *map* copy() at the moment, are ignored by
		// equals().
		for (TileFixture fixture : clone.getFixtures(new Point(0, 0))) {
			if (fixture instanceof AbstractTown) {
				assertEquals(0, ((AbstractTown) fixture).getDC(),
					"Copied map didn't copy DCs");
			}
		}
		final Unit uOne = new Unit(new PlayerImpl(0, ""), "type", "name", 7);
		uOne.addMember(new Worker("worker", "dwarf", 8, new Job("job", 1)));
		assertEquals(uOne, uOne.copy(false), "clone equals original");
		assertNotEquals(uOne, uOne.copy(true), "zeroed clone doesn't equal original");
		assertIsSubset(uOne, uOne.copy(true), "zeroed clone is subset of original", SubsetTest::noop);
	}

	static Stream<Arguments> townParameters() {
		return Stream.of(TownSize.values()).flatMap(a ->
			Stream.of(TownStatus.values()).flatMap(b ->
				Stream.of(Arguments.of(a, b))));
	}

	/**
	 * Test {@link AbstractTown} subset calculations, specifically in the {@link Town} instantiation.
	 */
	@ParameterizedTest
	@MethodSource("townParameters")
	public void testTownSubsets(final TownSize size, final TownStatus status) {
		TownSize differentSize;
		TownStatus differentStatus;
		final Player playerOne = new PlayerImpl(0, "playerOne");
		final Player playerTwo = new PlayerImpl(1, "playerTwo");
		final Player independent = new PlayerImpl(2, "independent");
		if (size.equals(TownSize.Small)) {
			differentSize = TownSize.Large;
		} else {
			differentSize = TownSize.Small;
		}
		if (status.equals(TownStatus.Active)) {
			differentStatus = TownStatus.Ruined;
		} else {
			differentStatus = TownStatus.Active;
		}
		assertNotSubset(new Town(status, size, -1, "townName", 0, playerOne),
			new Town(status, size, -1, "townName", 1, playerOne),
			"Different IDs break Town subsets", SubsetTest::noop);
		assertNotSubset(new Town(status, size, -1, "nameOne", 2, playerOne),
			new Town(status, size, -1, "nameTwo", 2, playerOne),
			"Different names breaks Town subsets", SubsetTest::noop);
		assertIsSubset(new Town(status, size, -1, "townName", 3, playerOne),
			new Town(status, size, -1, "unknown", 3, playerOne),
			"Town with \"unknown\" name is still subset", SubsetTest::noop);
		assertNotSubset(new Town(status, size, -1, "unknown", 3, playerOne),
			new Town(status, size, -1, "townName", 3, playerOne),
			"Name of \"unknown\" doesn't work in reverse", SubsetTest::noop);
		assertNotSubset(new Town(status, size, -1, "townName", 4, playerOne),
			new City(status, size, -1, "townName", 4, playerOne),
			"City not a subset of town", SubsetTest::noop);
		assertNotSubset(new Town(status, size, -1, "townName", 5, playerOne),
			new Fortification(status, size, -1, "townName", 5, playerOne),
			"Fortification not a subset of town", SubsetTest::noop);
		assertNotSubset(new Town(status, size, -1, "townName", 6, playerOne),
			new Town(differentStatus, size, -1, "townName", 6, playerOne),
			"Different status breaks subset", SubsetTest::noop);
		assertIsSubset(new Town(status, size, 5, "townName", 7, playerOne),
			new Town(status, size, 10, "townName", 7, playerOne),
			"Different DC doesn't break subset", SubsetTest::noop);
		assertNotSubset(new Town(status, size, -1, "townName", 8, playerOne),
			new Town(status, differentSize, -1, "townName", 8, playerOne),
			"Different size breaks subset", SubsetTest::noop);
		assertNotSubset(new Town(status, size, -1, "townName", 9, playerOne),
			new Town(status, size, -1, "townName", 9, playerTwo),
			"Different owner breaks subset", SubsetTest::noop);
		assertIsSubset(new Town(status, size, -1, "townName", 10, playerOne),
			new Town(status, size, -1, "townName", 10, independent),
			"Still a subset if they think independently owned", SubsetTest::noop);
		assertNotSubset(new Town(status, size, -1, "townName", 11, independent),
			new Town(status, size, -1, "townName", 11, playerOne),
			"Owned is not a subset of independently owned", SubsetTest::noop);
		final Town first = new Town(status, size, -1, "townName", 12, playerOne);
		final Town second = new Town(status, size, -1, "townName", 12, playerOne);
		first.setPopulation(new CommunityStats(8));
		assertIsSubset(first, second, "Missing population detils doesn't break subset", SubsetTest::noop);
		assertNotSubset(second, first,
			"Having population details when we don't does break subset", SubsetTest::noop);
		second.setPopulation(new CommunityStats(10));
		assertNotSubset(first, second,
			"Having non-subset population details breaks subset", SubsetTest::noop);
	}

	/**
	 * Test {@link AbstractTown} subset calculations, specifically in the {@link City} instantiation.
	 */
	@ParameterizedTest
	@MethodSource("townParameters")
	public void testCitySubsets(final TownSize size, final TownStatus status) {
		TownSize differentSize;
		TownStatus differentStatus;
		final Player playerOne = new PlayerImpl(0, "playerOne");
		final Player playerTwo = new PlayerImpl(1, "playerTwo");
		final Player independent = new PlayerImpl(2, "independent");
		if (size.equals(TownSize.Small)) {
			differentSize = TownSize.Large;
		} else {
			differentSize = TownSize.Small;
		}
		if (status.equals(TownStatus.Active)) {
			differentStatus = TownStatus.Ruined;
		} else {
			differentStatus = TownStatus.Active;
		}
		assertNotSubset(new City(status, size, -1, "townName", 0, playerOne),
			new City(status, size, -1, "townName", 1, playerOne),
			"Different IDs break City subsets", SubsetTest::noop);
		assertNotSubset(new City(status, size, -1, "nameOne", 2, playerOne),
			new City(status, size, -1, "nameTwo", 2, playerOne),
			"Different names breaks City subsets", SubsetTest::noop);
		assertIsSubset(new City(status, size, -1, "townName", 3, playerOne),
			new City(status, size, -1, "unknown", 3, playerOne),
			"City with \"unknown\" name is still subset", SubsetTest::noop);
		assertNotSubset(new City(status, size, -1, "unknown", 3, playerOne),
			new City(status, size, -1, "townName", 3, playerOne),
			"Name of \"unknown\" doesn't work in reverse", SubsetTest::noop);
		assertNotSubset(new City(status, size, -1, "townName", 4, playerOne),
			new Town(status, size, -1, "townName", 4, playerOne),
			"Town not a subset of City", SubsetTest::noop);
		assertNotSubset(new City(status, size, -1, "townName", 5, playerOne),
			new Fortification(status, size, -1, "townName", 5, playerOne),
			"Fortification not a subset of City", SubsetTest::noop);
		assertNotSubset(new City(status, size, -1, "townName", 6, playerOne),
			new City(differentStatus, size, -1, "townName", 6, playerOne),
			"Different status breaks subset", SubsetTest::noop);
		assertIsSubset(new City(status, size, 5, "townName", 7, playerOne),
			new City(status, size, 10, "townName", 7, playerOne),
			"Different DC doesn't break subset", SubsetTest::noop);
		assertNotSubset(new City(status, size, -1, "townName", 8, playerOne),
			new City(status, differentSize, -1, "townName", 8, playerOne),
			"Different size breaks subset", SubsetTest::noop);
		assertNotSubset(new City(status, size, -1, "townName", 9, playerOne),
			new City(status, size, -1, "townName", 9, playerTwo),
			"Different owner breaks subset", SubsetTest::noop);
		assertIsSubset(new City(status, size, -1, "townName", 10, playerOne),
			new City(status, size, -1, "townName", 10, independent),
			"Still a subset if they think independently owned", SubsetTest::noop);
		assertNotSubset(new City(status, size, -1, "townName", 11, independent),
			new City(status, size, -1, "townName", 11, playerOne),
			"Owned is not a subset of independently owned", SubsetTest::noop);
		City first = new City(status, size, -1, "townName", 12, playerOne);
		City second = new City(status, size, -1, "townName", 12, playerOne);
		first.setPopulation(new CommunityStats(8));
		assertIsSubset(first, second, "Missing population detils doesn't break subset", SubsetTest::noop);
		assertNotSubset(second, first,
			"Having population details when we don't does break subset", SubsetTest::noop);
		second.setPopulation(new CommunityStats(10));
		assertNotSubset(first, second,
			"Having non-subset population details breaks subset", SubsetTest::noop);
	}

	/**
	 * Test {@link AbstractTown} subset calculations, specifically in the
	 * {@link Fortification} instantiation.
	 */
	@ParameterizedTest
	@MethodSource("townParameters")
	public void testFortificationSubsets(final TownSize size, final TownStatus status) {
		TownSize differentSize;
		TownStatus differentStatus;
		final Player playerOne = new PlayerImpl(0, "playerOne");
		final Player playerTwo = new PlayerImpl(1, "playerTwo");
		final Player independent = new PlayerImpl(2, "independent");
		if (size.equals(TownSize.Small)) {
			differentSize = TownSize.Large;
		} else {
			differentSize = TownSize.Small;
		}
		if (status.equals(TownStatus.Active)) {
			differentStatus = TownStatus.Ruined;
		} else {
			differentStatus = TownStatus.Active;
		}
		assertNotSubset(new Fortification(status, size, -1, "townName", 0, playerOne),
			new Fortification(status, size, -1, "townName", 1, playerOne),
			"Different IDs break Fortification subsets", SubsetTest::noop);
		assertNotSubset(new Fortification(status, size, -1, "nameOne", 2, playerOne),
			new Fortification(status, size, -1, "nameTwo", 2, playerOne),
			"Different names breaks Fortification subsets", SubsetTest::noop);
		assertIsSubset(new Fortification(status, size, -1, "townName", 3, playerOne),
			new Fortification(status, size, -1, "unknown", 3, playerOne),
			"Fortification with \"unknown\" name is still subset", SubsetTest::noop);
		assertNotSubset(new Fortification(status, size, -1, "unknown", 3, playerOne),
			new Fortification(status, size, -1, "townName", 3, playerOne),
			"Name of \"unknown\" doesn't work in reverse", SubsetTest::noop);
		assertNotSubset(new Fortification(status, size, -1, "townName", 4, playerOne),
			new Town(status, size, -1, "townName", 4, playerOne),
			"Town not a subset of Fortification", SubsetTest::noop);
		assertNotSubset(new Fortification(status, size, -1, "townName", 5, playerOne),
			new City(status, size, -1, "townName", 5, playerOne),
			"City not a subset of Fortification", SubsetTest::noop);
		assertNotSubset(new Fortification(status, size, -1, "townName", 6, playerOne),
			new Fortification(differentStatus, size, -1, "townName", 6, playerOne),
			"Different status breaks subset", SubsetTest::noop);
		assertIsSubset(new Fortification(status, size, 5, "townName", 7, playerOne),
			new Fortification(status, size, 10, "townName", 7, playerOne),
			"Different DC doesn't break subset", SubsetTest::noop);
		assertNotSubset(new Fortification(status, size, -1, "townName", 8, playerOne),
			new Fortification(status, differentSize, -1, "townName", 8, playerOne),
			"Different size breaks subset", SubsetTest::noop);
		assertNotSubset(new Fortification(status, size, -1, "townName", 9, playerOne),
			new Fortification(status, size, -1, "townName", 9, playerTwo),
			"Different owner breaks subset", SubsetTest::noop);
		assertIsSubset(new Fortification(status, size, -1, "townName", 10, playerOne),
			new Fortification(status, size, -1, "townName", 10, independent),
			"Still a subset if they think independently owned", SubsetTest::noop);
		assertNotSubset(new Fortification(status, size, -1, "townName", 11, independent),
			new Fortification(status, size, -1, "townName", 11, playerOne),
			"Owned is not a subset of independently owned", SubsetTest::noop);
		Fortification first = new Fortification(status, size, -1, "townName", 12, playerOne);
		Fortification second = new Fortification(status, size, -1, "townName", 12, playerOne);
		first.setPopulation(new CommunityStats(8));
		assertIsSubset(first, second, "Missing population detils doesn't break subset", SubsetTest::noop);
		assertNotSubset(second, first,
			"Having population details when we don't does break subset", SubsetTest::noop);
		second.setPopulation(new CommunityStats(10));
		assertNotSubset(first, second,
			"Having non-subset population details breaks subset", SubsetTest::noop);
	}
}
