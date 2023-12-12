package legacy.map;

import common.map.*;
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

import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.mobile.Worker;
import legacy.map.fixtures.mobile.Unit;
import legacy.map.fixtures.mobile.AnimalImpl;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.worker.Job;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.terrain.Forest;
import common.map.fixtures.towns.TownSize;
import legacy.map.fixtures.towns.FortressImpl;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.IMutableFortress;
import legacy.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.TownStatus;
import legacy.map.fixtures.towns.Fortification;
import legacy.map.fixtures.towns.Town;
import legacy.map.fixtures.towns.City;
import legacy.map.fixtures.towns.CommunityStats;

/**
 * A collection of tests of the subset-checking features.
 *
 * TODO: Add test of Worker subset logic
 */
public final class SubsetTest {
    /**
     * Assert that "two" is a "strict subset," by our loose definition, of "one".
     */
    private static <SpecificType, GeneralType extends Subsettable<SpecificType>>
    void assertIsSubset(final GeneralType one, final SpecificType two, final String message) {
        assertTrue(one.isSubset(two, SubsetTest::noop), message);
    }

    /**
     * Assert that "two" is <em>not</em> a "strict subset," even by our loose definition,
     * of "one".
     */
    private static <SpecificType, GeneralType extends Subsettable<SpecificType>>
    void assertNotSubset(final GeneralType one, final SpecificType two, final String message) {
        assertFalse(one.isSubset(two, SubsetTest::noop), message);
    }

    private static void noop(final String str) {
    }

    /**
     * A test of {@link LegacyPlayerCollection}'s subset feature
     */
    @Test
    public void testPlayerCollectionSubset() {
        // TODO: Make a way to initialize one declaratively
        final IMutableLegacyPlayerCollection firstCollection = new LegacyPlayerCollection();
        firstCollection.add(new PlayerImpl(1, "one"));
        final IMutableLegacyPlayerCollection secondCollection = new LegacyPlayerCollection();
        secondCollection.add(new PlayerImpl(1, "one"));
        secondCollection.add(new PlayerImpl(2, "two"));
        final ILegacyPlayerCollection zero = new LegacyPlayerCollection();
        assertIsSubset(zero, zero, "Empty is subset of self");
        assertIsSubset(firstCollection, zero, "Empty is subset of one");
        assertIsSubset(secondCollection, zero, "Empty is subset of two");
        assertNotSubset(zero, firstCollection, "One is not subset of empty");
        assertIsSubset(firstCollection, firstCollection, "One is subset of self");
        assertIsSubset(secondCollection, firstCollection, "One is subset of two");
        assertNotSubset(zero, secondCollection, "Two is not subset of empty");
        assertNotSubset(firstCollection, secondCollection, "Two is not subset of one");
        assertIsSubset(secondCollection, secondCollection, "Two is subset of self");
    }

    /**
     * Assert that neither of two {@link IFortress fortresses} is a subset of the other.
     */
    private static void requireMatching(final IFortress one, final IFortress two, final String what) {
        assertNotSubset(one, two,
                String.format("Subset requires %s, first test", what));
        assertNotSubset(two, one,
                String.format("Subset requires %s, second test", what));
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
                "Fortress without is a subset of fortress with unit");
        assertNotSubset(firstFort, fifthFort,
                "Fortress with is not a subset of fortress without unit");
        final IFortress sixthFort = new FortressImpl(new PlayerImpl(1, "one"), "unknown", fortId,
                TownSize.Small);
        assertIsSubset(firstFort, sixthFort,
                "Fortress named \"unknown\" can be subset");
        assertNotSubset(sixthFort, firstFort,
                "\"unknown\" is not commutative");
        final IFortress seventhFort = new FortressImpl(new PlayerImpl(1, "one"), "unknown", fortId,
                TownSize.Medium);
        assertNotSubset(sixthFort, seventhFort,
                "Different size breaks Fortress subset");
        assertNotSubset(seventhFort, sixthFort,
                "Different size breaks Fortress subset");
    }

    /**
     * Create a map with the given terrain.
     */
    @SafeVarargs
    private static IMutableLegacyMap createMap(final Pair<Point, TileType>... terrain) {
        final IMutableLegacyMap retval = new LegacyMap(new MapDimensionsImpl(2, 2, 2),
                new LegacyPlayerCollection(), -1);
        for (final Pair<Point, TileType> pair : terrain) {
            retval.setBaseTerrain(pair.getValue0(), pair.getValue1());
        }
        return retval;
    }

    /**
     * Test the {@link ILegacyMap} subset feature
     */
    @Test
    public void testMapSubset() {
        final IMutableLegacyMap firstMap = createMap(Pair.with(new Point(0, 0), TileType.Jungle));
        final IMutableLegacyMap secondMap = createMap(Pair.with(new Point(0, 0), TileType.Jungle),
                Pair.with(new Point(1, 1), TileType.Ocean));
        final ILegacyMap zero = createMap();
        assertIsSubset(zero, zero, "None is a subset of itself");
        assertIsSubset(firstMap, zero, "None is a subset of one");
        assertIsSubset(secondMap, zero, "None is a subset of one");
        assertNotSubset(zero, firstMap, "One is not a subset of none");
        assertIsSubset(firstMap, firstMap, "One is a subset of itself");
        assertIsSubset(secondMap, firstMap, "One is a subset of two");
        assertNotSubset(zero, secondMap, "Two is not a subset of none");
        assertNotSubset(firstMap, secondMap, "Two is not a subset of one");
        assertIsSubset(secondMap, secondMap, "Two is a subset of itself");
        firstMap.setBaseTerrain(new Point(1, 1), TileType.Plains);
        assertNotSubset(secondMap, firstMap,
                "Corresponding but non-matching tile breaks subset");
        assertNotSubset(firstMap, secondMap,
                "Corresponding but non-matching tile breaks subset");
        secondMap.setBaseTerrain(new Point(1, 1), TileType.Plains);
        assertIsSubset(secondMap, firstMap, "Subset again after resetting terrain");
        firstMap.addFixture(new Point(1, 1), new CacheFixture("category", "contents", 3));
        assertIsSubset(secondMap, firstMap, "Subset calculation ignores caches");
        firstMap.addFixture(new Point(1, 1), new TextFixture("text", -1));
        assertIsSubset(secondMap, firstMap, "Subset calculation ignores text fixtures");
        firstMap.addFixture(new Point(1, 1), new AnimalTracks("animal"));
        assertIsSubset(secondMap, firstMap, "Subset calculation ignores animal tracks");
        firstMap.addFixture(new Point(1, 1), new AnimalImpl("animal", true, "status", 7));
        assertNotSubset(secondMap, firstMap,
                "Subset calculation does not ignore other fixtures");
    }

    @FunctionalInterface
    private interface ITestMethod {
        void apply(ILegacyMap one, ILegacyMap two, String message);
    }

    /**
     * Test map subset calculations to ensure that off-by-one errors are caught.
     */
    @Test
    public void testMapOffByOne() {
        final IMutableLegacyMap baseMap = new LegacyMap(new MapDimensionsImpl(2, 2, 2),
                new LegacyPlayerCollection(), -1);
        for (final Point point : baseMap.getLocations()) {
            baseMap.setBaseTerrain(point, TileType.Plains);
        }
        baseMap.addFixture(new Point(1, 1), new Forest("elm", false, 1));
        baseMap.addFixture(new Point(1, 1), new AnimalImpl("skunk", false, "wild", 2));
        baseMap.addRivers(new Point(1, 1), River.East);

        final IMutableLegacyMap testMap = new LegacyMap(new MapDimensionsImpl(2, 2, 2),
                new LegacyPlayerCollection(), -1);
        for (final Point point : testMap.getLocations()) {
            testMap.setBaseTerrain(point, TileType.Plains);
        }
        final Forest forest = new Forest("elm", false, 1);
        final TileFixture animal = new AnimalImpl("skunk", false, "wild", 2);
        for (final Point point : testMap.getLocations()) {
            assertIsSubset(baseMap, testMap,
                    "Subset invariant before attempt using " + point);
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
            testMethod.apply(baseMap, testMap, result);
            testMap.removeFixture(point, forest);
            testMap.addFixture(point, animal);
            testMethod.apply(baseMap, testMap, result);
            testMap.removeFixture(point, animal);
            testMap.addRivers(point, River.East);
            testMethod.apply(baseMap, testMap, result);
            testMap.removeRivers(point, River.East);
            assertIsSubset(baseMap, testMap,
                    "Subset invariant after attempt using " + point);
        }
    }

    /**
     * Test interaction between isSubset() and copy().
     */
    @Test
    public void testSubsetsAndCopy() {
        final IMutableLegacyMap firstMap = new LegacyMap(new MapDimensionsImpl(2, 2, 2),
                new LegacyPlayerCollection(), -1);
        firstMap.setBaseTerrain(new Point(0, 0), TileType.Jungle);
        final ILegacyMap zero = new LegacyMap(new MapDimensionsImpl(2, 2, 2),
                new LegacyPlayerCollection(), -1);
        assertIsSubset(firstMap, zero, "zero is a subset of one before copy");
        final IMutableLegacyMap secondMap =
                new LegacyMap(new MapDimensionsImpl(2, 2, 2), new LegacyPlayerCollection(), -1);
        secondMap.setBaseTerrain(new Point(0, 0), TileType.Jungle);
        firstMap.setBaseTerrain(new Point(1, 1), TileType.Plains);
        secondMap.setBaseTerrain(new Point(1, 1), TileType.Plains);
        firstMap.addFixture(new Point(1, 1), new CacheFixture("category", "contents", 3));
        firstMap.addFixture(new Point(1, 1), new TextFixture("text", -1));
        firstMap.addFixture(new Point(1, 1), new AnimalTracks("animal"));
        firstMap.addFixture(new Point(0, 0),
                new Fortification(TownStatus.Burned, TownSize.Large, 15, "fortification", 6,
                        new PlayerImpl(0, "")));
        assertEquals(firstMap, firstMap.copy(IFixture.CopyBehavior.KEEP, null), "Cloned map equals original");
        final ILegacyMap clone = firstMap.copy(IFixture.CopyBehavior.ZERO, null);
        assertIsSubset(clone, zero, "unfilled map is still a subset of zeroed clone");
        // DCs, the only thing zeroed out in *map* copy() at the moment, are ignored by
        // equals().
        for (final TileFixture fixture : clone.getFixtures(new Point(0, 0))) {
            if (fixture instanceof AbstractTown) {
                assertEquals(0, fixture.getDC(),
                        "Copied map didn't copy DCs");
            }
        }
        final Unit uOne = new Unit(new PlayerImpl(0, ""), "type", "name", 7);
        uOne.addMember(new Worker("worker", "dwarf", 8, new Job("job", 1)));
        assertEquals(uOne, uOne.copy(IFixture.CopyBehavior.KEEP), "clone equals original");
        assertNotEquals(uOne, uOne.copy(IFixture.CopyBehavior.ZERO), "zeroed clone doesn't equal original");
        assertIsSubset(uOne, uOne.copy(IFixture.CopyBehavior.ZERO), "zeroed clone is subset of original");
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
        final TownSize differentSize;
        final TownStatus differentStatus;
        final Player playerOne = new PlayerImpl(0, "playerOne");
        final Player playerTwo = new PlayerImpl(1, "playerTwo");
        final Player independent = new PlayerImpl(2, "independent");
        if (size == TownSize.Small) {
            differentSize = TownSize.Large;
        } else {
            differentSize = TownSize.Small;
        }
        if (status == TownStatus.Active) {
            differentStatus = TownStatus.Ruined;
        } else {
            differentStatus = TownStatus.Active;
        }
        assertNotSubset(new Town(status, size, -1, "townName", 0, playerOne),
                new Town(status, size, -1, "townName", 1, playerOne),
                "Different IDs break Town subsets");
        assertNotSubset(new Town(status, size, -1, "nameOne", 2, playerOne),
                new Town(status, size, -1, "nameTwo", 2, playerOne),
                "Different names breaks Town subsets");
        assertIsSubset(new Town(status, size, -1, "townName", 3, playerOne),
                new Town(status, size, -1, "unknown", 3, playerOne),
                "Town with \"unknown\" name is still subset");
        assertNotSubset(new Town(status, size, -1, "unknown", 3, playerOne),
                new Town(status, size, -1, "townName", 3, playerOne),
                "Name of \"unknown\" doesn't work in reverse");
        assertNotSubset(new Town(status, size, -1, "townName", 4, playerOne),
                new City(status, size, -1, "townName", 4, playerOne),
                "City not a subset of town");
        assertNotSubset(new Town(status, size, -1, "townName", 5, playerOne),
                new Fortification(status, size, -1, "townName", 5, playerOne),
                "Fortification not a subset of town");
        assertNotSubset(new Town(status, size, -1, "townName", 6, playerOne),
                new Town(differentStatus, size, -1, "townName", 6, playerOne),
                "Different status breaks subset");
        assertIsSubset(new Town(status, size, 5, "townName", 7, playerOne),
                new Town(status, size, 10, "townName", 7, playerOne),
                "Different DC doesn't break subset");
        assertNotSubset(new Town(status, size, -1, "townName", 8, playerOne),
                new Town(status, differentSize, -1, "townName", 8, playerOne),
                "Different size breaks subset");
        assertNotSubset(new Town(status, size, -1, "townName", 9, playerOne),
                new Town(status, size, -1, "townName", 9, playerTwo),
                "Different owner breaks subset");
        assertIsSubset(new Town(status, size, -1, "townName", 10, playerOne),
                new Town(status, size, -1, "townName", 10, independent),
                "Still a subset if they think independently owned");
        assertNotSubset(new Town(status, size, -1, "townName", 11, independent),
                new Town(status, size, -1, "townName", 11, playerOne),
                "Owned is not a subset of independently owned");
        final Town first = new Town(status, size, -1, "townName", 12, playerOne);
        final Town second = new Town(status, size, -1, "townName", 12, playerOne);
        first.setPopulation(new CommunityStats(8));
        assertIsSubset(first, second, "Missing population detils doesn't break subset");
        assertNotSubset(second, first,
                "Having population details when we don't does break subset");
        second.setPopulation(new CommunityStats(10));
        assertNotSubset(first, second,
                "Having non-subset population details breaks subset");
    }

    /**
     * Test {@link AbstractTown} subset calculations, specifically in the {@link City} instantiation.
     */
    @ParameterizedTest
    @MethodSource("townParameters")
    public void testCitySubsets(final TownSize size, final TownStatus status) {
        final TownSize differentSize;
        final TownStatus differentStatus;
        final Player playerOne = new PlayerImpl(0, "playerOne");
        final Player playerTwo = new PlayerImpl(1, "playerTwo");
        final Player independent = new PlayerImpl(2, "independent");
        if (size == TownSize.Small) {
            differentSize = TownSize.Large;
        } else {
            differentSize = TownSize.Small;
        }
        if (status == TownStatus.Active) {
            differentStatus = TownStatus.Ruined;
        } else {
            differentStatus = TownStatus.Active;
        }
        assertNotSubset(new City(status, size, -1, "townName", 0, playerOne),
                new City(status, size, -1, "townName", 1, playerOne),
                "Different IDs break City subsets");
        assertNotSubset(new City(status, size, -1, "nameOne", 2, playerOne),
                new City(status, size, -1, "nameTwo", 2, playerOne),
                "Different names breaks City subsets");
        assertIsSubset(new City(status, size, -1, "townName", 3, playerOne),
                new City(status, size, -1, "unknown", 3, playerOne),
                "City with \"unknown\" name is still subset");
        assertNotSubset(new City(status, size, -1, "unknown", 3, playerOne),
                new City(status, size, -1, "townName", 3, playerOne),
                "Name of \"unknown\" doesn't work in reverse");
        assertNotSubset(new City(status, size, -1, "townName", 4, playerOne),
                new Town(status, size, -1, "townName", 4, playerOne),
                "Town not a subset of City");
        assertNotSubset(new City(status, size, -1, "townName", 5, playerOne),
                new Fortification(status, size, -1, "townName", 5, playerOne),
                "Fortification not a subset of City");
        assertNotSubset(new City(status, size, -1, "townName", 6, playerOne),
                new City(differentStatus, size, -1, "townName", 6, playerOne),
                "Different status breaks subset");
        assertIsSubset(new City(status, size, 5, "townName", 7, playerOne),
                new City(status, size, 10, "townName", 7, playerOne),
                "Different DC doesn't break subset");
        assertNotSubset(new City(status, size, -1, "townName", 8, playerOne),
                new City(status, differentSize, -1, "townName", 8, playerOne),
                "Different size breaks subset");
        assertNotSubset(new City(status, size, -1, "townName", 9, playerOne),
                new City(status, size, -1, "townName", 9, playerTwo),
                "Different owner breaks subset");
        assertIsSubset(new City(status, size, -1, "townName", 10, playerOne),
                new City(status, size, -1, "townName", 10, independent),
                "Still a subset if they think independently owned");
        assertNotSubset(new City(status, size, -1, "townName", 11, independent),
                new City(status, size, -1, "townName", 11, playerOne),
                "Owned is not a subset of independently owned");
        final City first = new City(status, size, -1, "townName", 12, playerOne);
        final City second = new City(status, size, -1, "townName", 12, playerOne);
        first.setPopulation(new CommunityStats(8));
        assertIsSubset(first, second, "Missing population detils doesn't break subset");
        assertNotSubset(second, first,
                "Having population details when we don't does break subset");
        second.setPopulation(new CommunityStats(10));
        assertNotSubset(first, second,
                "Having non-subset population details breaks subset");
    }

    /**
     * Test {@link AbstractTown} subset calculations, specifically in the
     * {@link Fortification} instantiation.
     */
    @ParameterizedTest
    @MethodSource("townParameters")
    public void testFortificationSubsets(final TownSize size, final TownStatus status) {
        final TownSize differentSize;
        final TownStatus differentStatus;
        final Player playerOne = new PlayerImpl(0, "playerOne");
        final Player playerTwo = new PlayerImpl(1, "playerTwo");
        final Player independent = new PlayerImpl(2, "independent");
        if (size == TownSize.Small) {
            differentSize = TownSize.Large;
        } else {
            differentSize = TownSize.Small;
        }
        if (status == TownStatus.Active) {
            differentStatus = TownStatus.Ruined;
        } else {
            differentStatus = TownStatus.Active;
        }
        assertNotSubset(new Fortification(status, size, -1, "townName", 0, playerOne),
                new Fortification(status, size, -1, "townName", 1, playerOne),
                "Different IDs break Fortification subsets");
        assertNotSubset(new Fortification(status, size, -1, "nameOne", 2, playerOne),
                new Fortification(status, size, -1, "nameTwo", 2, playerOne),
                "Different names breaks Fortification subsets");
        assertIsSubset(new Fortification(status, size, -1, "townName", 3, playerOne),
                new Fortification(status, size, -1, "unknown", 3, playerOne),
                "Fortification with \"unknown\" name is still subset");
        assertNotSubset(new Fortification(status, size, -1, "unknown", 3, playerOne),
                new Fortification(status, size, -1, "townName", 3, playerOne),
                "Name of \"unknown\" doesn't work in reverse");
        assertNotSubset(new Fortification(status, size, -1, "townName", 4, playerOne),
                new Town(status, size, -1, "townName", 4, playerOne),
                "Town not a subset of Fortification");
        assertNotSubset(new Fortification(status, size, -1, "townName", 5, playerOne),
                new City(status, size, -1, "townName", 5, playerOne),
                "City not a subset of Fortification");
        assertNotSubset(new Fortification(status, size, -1, "townName", 6, playerOne),
                new Fortification(differentStatus, size, -1, "townName", 6, playerOne),
                "Different status breaks subset");
        assertIsSubset(new Fortification(status, size, 5, "townName", 7, playerOne),
                new Fortification(status, size, 10, "townName", 7, playerOne),
                "Different DC doesn't break subset");
        assertNotSubset(new Fortification(status, size, -1, "townName", 8, playerOne),
                new Fortification(status, differentSize, -1, "townName", 8, playerOne),
                "Different size breaks subset");
        assertNotSubset(new Fortification(status, size, -1, "townName", 9, playerOne),
                new Fortification(status, size, -1, "townName", 9, playerTwo),
                "Different owner breaks subset");
        assertIsSubset(new Fortification(status, size, -1, "townName", 10, playerOne),
                new Fortification(status, size, -1, "townName", 10, independent),
                "Still a subset if they think independently owned");
        assertNotSubset(new Fortification(status, size, -1, "townName", 11, independent),
                new Fortification(status, size, -1, "townName", 11, playerOne),
                "Owned is not a subset of independently owned");
        final Fortification first = new Fortification(status, size, -1, "townName", 12, playerOne);
        final Fortification second = new Fortification(status, size, -1, "townName", 12, playerOne);
        first.setPopulation(new CommunityStats(8));
        assertIsSubset(first, second, "Missing population detils doesn't break subset");
        assertNotSubset(second, first,
                "Having population details when we don't does break subset");
        second.setPopulation(new CommunityStats(10));
        assertNotSubset(first, second,
                "Having non-subset population details breaks subset");
    }
}
