package impl.dbio;

import buckelieg.jdbc.fn.DB;

import static lovelace.util.SingletonRandom.SINGLETON_RANDOM;

import java.sql.Connection;
import java.sql.SQLException;
import java.math.BigDecimal;

import org.sqlite.SQLiteDataSource;

import common.map.IMapNG;
import common.map.IMutableMapNG;
import common.map.SPMapNG;
import common.map.MapDimensions;
import common.map.MapDimensionsImpl;
import common.map.PlayerImpl;
import common.map.HasOwner;
import common.map.Player;
import common.map.TileFixture;
import common.map.Point;
import common.map.PlayerCollection;
import common.map.Direction;
import common.map.TileType;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Quantity;
import common.map.fixtures.Implement;
import common.map.fixtures.Ground;
import common.map.fixtures.TextFixture;
import common.map.fixtures.explorable.AdventureFixture;
import common.map.fixtures.explorable.Portal;
import common.map.fixtures.explorable.Cave;
import common.map.fixtures.explorable.Battlefield;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.mobile.Sphinx;
import common.map.fixtures.mobile.Immortal;
import common.map.fixtures.mobile.Djinn;
import common.map.fixtures.mobile.Griffin;
import common.map.fixtures.mobile.Minotaur;
import common.map.fixtures.mobile.Ogre;
import common.map.fixtures.mobile.Phoenix;
import common.map.fixtures.mobile.Simurgh;
import common.map.fixtures.mobile.Troll;
import common.map.fixtures.mobile.Centaur;
import common.map.fixtures.mobile.Dragon;
import common.map.fixtures.mobile.Fairy;
import common.map.fixtures.mobile.Worker;
import common.map.fixtures.mobile.Giant;
import common.map.fixtures.mobile.AnimalImpl;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.Snowbird;
import common.map.fixtures.mobile.Thunderbird;
import common.map.fixtures.mobile.Pegasus;
import common.map.fixtures.mobile.Unicorn;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Kraken;
import common.map.fixtures.mobile.worker.RaceFactory;
import common.map.fixtures.mobile.worker.Job;
import common.map.fixtures.mobile.worker.Skill;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.FieldStatus;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.StoneKind;
import common.map.fixtures.resources.Shrub;
import common.map.fixtures.terrain.Forest;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.Fortification;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.CommunityStats;
import common.map.fixtures.towns.City;
import common.map.fixtures.towns.Town;
import common.map.fixtures.towns.FortressImpl;
import common.map.fixtures.towns.Village;
import common.xmlio.Warning;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.mobile.IMutableUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.stream.Collector;
import java.util.HashSet;
import common.map.fixtures.TerrainFixture;

/**
 * Tests of the SQLite database I/O functionality.
 *
 * TODO: All tests should be more robust, as if developed test-first
 *
 * TODO: Randomize string inputs
 */
@Disabled("Until either jdbc-fn library is fixed or DBIO code ported to a different one")
public final class TestDBIO {
	private static final Collection<IntFunction<Immortal>> simpleImmortalConstructors =
		Collections.unmodifiableList(Arrays.asList(Sphinx::new, Djinn::new, Griffin::new,
			Minotaur::new, Ogre::new, Phoenix::new, Simurgh::new, Troll::new,
			Snowbird::new, Thunderbird::new, Pegasus::new, Unicorn::new, Kraken::new));


	@FunctionalInterface
	private interface StringIntConstructor<Type> {
		Type apply(String str, int num);
	}

	private static final Collection<StringIntConstructor<Immortal>> kindedImmortalConstructors =
		Collections.unmodifiableList(Arrays.asList(Centaur::new, Dragon::new, Fairy::new,
			Giant::new));

	private static final Collection<IntFunction<TileFixture>> simpleTerrainConstructors =
		Collections.unmodifiableList(Arrays.asList(Hill::new, Oasis::new));

	private static final Collection<String> races = new HashSet<>(RaceFactory.RACES);

	private final SQLiteDataSource source;

	private final SPDatabaseWriter writer = new SPDatabaseWriter();

	private final SPDatabaseReader reader = new SPDatabaseReader();

	private Connection connection;

	@BeforeEach
	public void setUp() throws SQLException {
		connection = source.getConnection();
	}

	public TestDBIO() {
		source = new SQLiteDataSource();
		source.setUrl("jdbc:sqlite:file::memory:?cache=shared");
	}

	private IMapNG assertDatabaseSerialization(final IMapNG map) {
		DB db = new DB(() -> connection);
		writer.writeToDatabase(db, map);
		IMapNG deserialized = reader.readMapFromDatabase(db, Warning.DIE);
		assertEquals(map, deserialized, "Deserialized form is the same as original");
		return deserialized;
	}

	private <FixtureType extends TileFixture> FixtureType
			assertFixtureSerialization(final FixtureType fixture) {
		MapDimensions dimensions = new MapDimensionsImpl(2, 2, 2);
		IMutableMapNG firstMap = new SPMapNG(dimensions, new PlayerCollection(), -1);
		firstMap.addFixture(new Point(0, 0), fixture);
		IMutableMapNG secondMap = new SPMapNG(dimensions, new PlayerCollection(), -1);
		secondMap.addFixture(new Point(1, 1), fixture);
		if (fixture instanceof HasOwner) {
			firstMap.addPlayer(((HasOwner) fixture).getOwner());
			secondMap.addPlayer(((HasOwner) fixture).getOwner());
		}
		IMapNG deserializedFirst = assertDatabaseSerialization(firstMap);
		IMapNG deserializedSecond = assertDatabaseSerialization(secondMap);
		assertNotEquals(deserializedFirst, deserializedSecond);
		FixtureType retval =
			(FixtureType) deserializedFirst.getFixtures(new Point(0, 0))
				.stream().findFirst().get();
		return retval;
	}

	private static Stream<Arguments> fewIntegers() {
		return SINGLETON_RANDOM.ints().boxed().limit(3).map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("fewIntegers")
	public void testAdventureSerialization(final int id) {
		assertFixtureSerialization(new AdventureFixture(new PlayerImpl(1, "independent"),
			"hook brief", "hook full", id));
	}

	private static Stream<Arguments> testPortalSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
				SINGLETON_RANDOM.ints().boxed().limit(2).map(c ->
					Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testPortalSerialization(final int id, final int row, final int column) {
		assertFixtureSerialization(new Portal("portal dest", new Point(row, column), id));
	}

	private static Stream<Arguments> testAnimalSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			Stream.of(true, false).map(b -> Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testAnimalSerialization(final int id, final boolean talking) {
		assertFixtureSerialization(new AnimalImpl("animal kind", talking, "status", id, -1, 1));
	}

	@Test
	public void testTracksSerialization() {
		assertFixtureSerialization(new AnimalTracks("kind"));
	}

	@ParameterizedTest
	@MethodSource("fewIntegers")
	public void testCacheSerialization(final int id) {
		assertFixtureSerialization(new CacheFixture("kind", "contents", id));
	}

	private static Stream<Arguments> testCaveSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).map(b -> Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testCaveSerialization(final int id, final int dc) {
		assertFixtureSerialization(new Cave(dc, id));
	}

	@ParameterizedTest
	@MethodSource("testCaveSerialization")
	public void testBattlefieldSerialization(final int id, final int dc) {
		assertFixtureSerialization(new Battlefield(dc, id));
	}

	private static Stream<Arguments> testCitySerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			Stream.of(TownStatus.values()).flatMap(b ->
				Stream.of(TownSize.values()).flatMap(c ->
					SINGLETON_RANDOM.ints().boxed().limit(1).map(d ->
						Arguments.of(a, b, c, d)))));
	}

	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testFortificationSerialization(final int id, final TownStatus status, final TownSize size, final int dc) {
		// TODO: We want more of the state to be random
		Fortification town = new Fortification(status, size, dc, "name", id, new PlayerImpl(0, ""));
		CommunityStats stats = new CommunityStats(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.getYearlyProduction().add(new ResourcePileImpl(1, "first", "first detail",
			new Quantity(5, "pounds")));
		stats.getYearlyProduction().add(new ResourcePileImpl(2, "second", "second detail",
			new Quantity(new BigDecimal(1).divide(new BigDecimal(2)), "quarts")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(3, "third", "third detail",
			new Quantity(8, "pecks")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(4, "fourth", "fourth detail",
			new Quantity(new BigDecimal(5).divide(new BigDecimal(4)), "square feet")));
		town.setPopulation(stats);
		assertFixtureSerialization(town);
	}

	@ParameterizedTest
	@MethodSource
	public void testCitySerialization(final int id, final TownStatus status, final TownSize size, final int dc) {
		// TODO: We want more of the state to be random
		City town = new City(status, size, dc, "name", id, new PlayerImpl(0, ""));
		CommunityStats stats = new CommunityStats(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.getYearlyProduction().add(new ResourcePileImpl(1, "first", "first detail",
			new Quantity(5, "pounds")));
		stats.getYearlyProduction().add(new ResourcePileImpl(2, "second", "second detail",
			new Quantity(new BigDecimal(1).divide(new BigDecimal(2)), "quarts")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(3, "third", "third detail",
			new Quantity(8, "pecks")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(4, "fourth", "fourth detail",
			new Quantity(new BigDecimal(5).divide(new BigDecimal(4)), "square feet")));
		town.setPopulation(stats);
		assertFixtureSerialization(town);
	}

	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testTownSerialization(final int id, final TownStatus status, final TownSize size, final int dc) {
		// TODO: We want more of the state to be random
		Town town = new Town(status, size, dc, "name", id, new PlayerImpl(0, ""));
		CommunityStats stats = new CommunityStats(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.getYearlyProduction().add(new ResourcePileImpl(1, "first", "first detail",
			new Quantity(5, "pounds")));
		stats.getYearlyProduction().add(new ResourcePileImpl(2, "second", "second detail",
			new Quantity(new BigDecimal(1).divide(new BigDecimal(2)), "quarts")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(3, "third", "third detail",
			new Quantity(8, "pecks")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(4, "fourth", "fourth detail",
			new Quantity(new BigDecimal(5).divide(new BigDecimal(4)), "square feet")));
		town.setPopulation(stats);
		assertFixtureSerialization(town);
	}

	private static Stream<Arguments> testMeadowSerialization() {
		return Stream.of(true, false).flatMap(a -> Stream.of(true, false).flatMap(b ->
			SINGLETON_RANDOM.ints().boxed().limit(1).flatMap(c ->
				Stream.of(FieldStatus.values()).flatMap(d ->
					SINGLETON_RANDOM.ints().boxed().limit(1).map(e ->
						Arguments.of(a, b, c, d, e))))));
	}

	@ParameterizedTest
	@MethodSource
	public void testMeadowSerialization(final boolean field, final boolean cultivated, final int id, final FieldStatus status,
	                                    final int acres) {
		assertFixtureSerialization(new Meadow("kind", field, cultivated, id, status, acres));
	}

	// TODO: Combine with testMeadowSerialization()?
	@ParameterizedTest
	@MethodSource("testMeadowSerialization")
	public void testFractionalMeadowSerialization(final boolean field, final boolean cultivated, final int id,
	                                              final FieldStatus status, final int acres) {
		assertFixtureSerialization(new Meadow("kind", field, cultivated, id, status,
			new BigDecimal(acres).add(new BigDecimal(1).divide(new BigDecimal(2)))));
	}

	private static Stream<Arguments> testForestSerialization() {
		return Stream.of(true, false).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(1).flatMap(b ->
				SINGLETON_RANDOM.ints().boxed().limit(1).map(c -> Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testForestSerialization(final boolean rows, final int id, final int acres) {
		assertFixtureSerialization(new Forest("kind", rows, id, acres));
	}

	@ParameterizedTest
	@MethodSource("testForestSerialization")
	public void testFractionalForestSerialization(final boolean rows, final int id, final int acres) {
		assertFixtureSerialization(new Forest("kind", rows, id,
			new BigDecimal(acres).add(new BigDecimal(1).divide(new BigDecimal(4)))));
	}

	private static Stream<Arguments> testFortressSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			Stream.of(TownSize.values()).map(b -> Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testFortressSerialization(final int id, final TownSize size) {
		Player owner = new PlayerImpl(1, "owner");
		IMutableFortress fortress = new FortressImpl(owner, "fortress", id, size);
		IMutableUnit unit = new Unit(owner, "unitKind", "unitName", id + 2);
		unit.addMember(new Worker("workerName", "human", id + 3, new Job("jobName", 2)));
		unit.addMember(new Sphinx(id + 5));
		unit.addMember(new ResourcePileImpl(id + 6, "resource kind", "resource contents",
			new Quantity(8, "counts")));
		fortress.addMember(unit);
		fortress.addMember(new Implement("equipment", id + 4, 2));
		fortress.addMember(new ResourcePileImpl(id + 7, "second resource", "second contents",
			new Quantity(new BigDecimal(3).divide(new BigDecimal(4)), "gallon")));
		assertFixtureSerialization(fortress);
	}

	// TODO: Move to lovelace.util
	static <T> Collector<T, ?, Stream<T>> toShuffledStream(final int limit) {
		return Collectors.collectingAndThen(Collectors.toList(), collected -> {
			Collections.shuffle(collected);
			return collected.stream().limit(limit);
		});
	}

	private static Stream<Arguments> testGroundSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			Stream.of(true, false).flatMap(b ->
				races.stream().collect(toShuffledStream(2)).map(c ->
					Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testGroundSerialization(final int id, final boolean exposed, final String kind) {
		assertFixtureSerialization(new Ground(id, kind, exposed));
	}

	private static Stream<Arguments> testGroveSerialization() {
		return Stream.of(true, false).flatMap(a -> Stream.of(true, false).flatMap(b ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(c ->
				SINGLETON_RANDOM.ints().boxed().limit(1).flatMap(d ->
					races.stream().collect(toShuffledStream(1)).map(e ->
						Arguments.of(a, b, c, d, e))))));
	}

	@ParameterizedTest
	@MethodSource
	public void testGroveSerialization(final boolean orchard, final boolean cultivated, final int id, final int count,
	                                   final String kind) {
		assertFixtureSerialization(new Grove(orchard, cultivated, kind, id, count));
	}

	private static Stream<Arguments> testSimpleImmortalSerialization() {
		return simpleImmortalConstructors.stream().flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).map(b ->
				Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testSimpleImmortalSerialization(final IntFunction<Immortal> constructor, final int id) {
		assertFixtureSerialization(constructor.apply(id));
	}

	private static Stream<Arguments> testKindedImmortalSerialization() {
		return kindedImmortalConstructors.stream().flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
				races.stream().collect(toShuffledStream(2)).map(c ->
					Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testKindedImmortalSerialization(final StringIntConstructor<Immortal> constructor, final int id,
	                                            final String kind) {
		assertFixtureSerialization(constructor.apply(kind, id));
	}

	private static Stream<Arguments> testMineSerialization() {
		return Stream.of(TownStatus.values()).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
				races.stream().collect(toShuffledStream(1)).map(c ->
					Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testMineSerialization(final TownStatus status, final int id, final String kind) {
		assertFixtureSerialization(new Mine(kind, status, id));
	}

	private static Stream<Arguments> testMineralSerialization() {
		return Stream.of(true, false).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
				SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(c ->
					races.stream().collect(toShuffledStream(1)).map(d ->
						Arguments.of(a, b, c, d)))));
	}

	@ParameterizedTest
	@MethodSource
	public void testMineralSerialization(final boolean exposed, final int dc, final int id, final String kind) {
		assertFixtureSerialization(new MineralVein(kind, exposed, dc, id));
	}

	private static Stream<Arguments> testStoneSerialization() {
		return Stream.of(StoneKind.values()).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
				SINGLETON_RANDOM.ints().boxed().limit(2).map(c ->
						Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testStoneSerialization(final StoneKind kind, final int dc, final int id) {
		assertFixtureSerialization(new StoneDeposit(kind, dc, id));
	}

	private static Stream<Arguments> testShrubSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
				SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
					races.stream().collect(toShuffledStream(1)).map(c ->
						Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testShrubSerialization(final int id, final int count, final String kind) {
		assertFixtureSerialization(new Shrub(kind, id, count));
	}

	private static Stream<Arguments> testSimpleTerrainSerialization() {
		return simpleTerrainConstructors.stream().flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).map(b ->
				Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testSimpleTerrainSerialization(final IntFunction<TerrainFixture> constructor, final int id) {
		assertFixtureSerialization(constructor.apply(id));
	}

	// TODO: randomize text
	@ParameterizedTest
	@MethodSource("fewIntegers")
	public void testTextSerialization(final int turn) {
		assertFixtureSerialization(new TextFixture("test text", turn - 1));
	}

	@ParameterizedTest
	@MethodSource("fewIntegers")
	public void testUnitSerialization(final int id) {
		Player owner = new PlayerImpl(1, "owner");
		IMutableUnit unit = new Unit(owner, "unitKind", "unitName", id);
		unit.addMember(new Worker("worker name", "elf", id + 1, new Job("job name", 2,
				new Skill("first skill", 1, 2), new Skill("second skill", 3, 4)),
			new Job("second job", 4)));
		unit.addMember(new Centaur("horse", id + 5));
		unit.addMember(new AnimalImpl("elephant", false, "domesticated", id + 6, -1, 4));
		assertFixtureSerialization(unit);
	}

	private static Stream<Arguments> testVillageSerialization() {
		return Stream.of(TownStatus.values()).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(1).flatMap(b ->
					races.stream().collect(toShuffledStream(3)).map(c ->
						Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testVillageSerialization(final TownStatus status, final int id, final String race) {
		assertFixtureSerialization(new Village(status, "village name", id,
			new PlayerImpl(1, "player name"), race));
	}

	private static Stream<Arguments> testNotesSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(1).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(1).flatMap(b ->
					races.stream().collect(toShuffledStream(2)).map(c ->
						Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testNotesSerialization(final int id, final int player, final String note) {
		Worker worker = new Worker("test worker", "human", id);
		Player playerObj = new PlayerImpl(player, "");
		worker.setNote(playerObj, note);
		Unit unit = new Unit(playerObj, "unitKind", "unitName", id + 1);
		unit.addMember(worker);
		IUnit deserializedUnit = assertFixtureSerialization(unit);
		assertEquals(note, ((IWorker) deserializedUnit.iterator().next()).getNote(playerObj),
			"Note was deserialized");
	}

	@Test
	public void testBookmarkSerialization() {
		IMutableMapNG map = new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 1);
		Player player = map.getPlayers().getPlayer(1);
		map.setCurrentPlayer(player);
		assertFalse(map.getBookmarks().contains(new Point(0, 0)),
			"Map by default doesn't have a bookmark");
		assertEquals(0, map.getAllBookmarks(new Point(0, 0)).size(),
			"Map by default has no bookmarks");
		map.addBookmark(new Point(0, 0));
		IMutableMapNG deserialized = (IMutableMapNG) assertDatabaseSerialization(map);
		assertFalse(map == deserialized, "Deserialization doesn't just return the input");
		assertTrue(deserialized.getBookmarks().contains(new Point(0, 0)),
			"Deserialized map has the bookmark we saved");
	}

	private static Stream<Arguments> testRoadSerialization() {
		return Stream.of(Direction.values()).flatMap(a ->
			SINGLETON_RANDOM.ints(7).map(i -> i + 1).boxed().limit(1).flatMap(b ->
				Stream.of(Direction.values()).flatMap(c ->
					SINGLETON_RANDOM.ints(7).map(i -> i + 1).boxed().limit(1)
						.map(d -> Arguments.of(a, b, c, d)))));
	}

	@ParameterizedTest
	@MethodSource
	public void testRoadSerialization(final Direction directionOne, final int qualityOne, final Direction directionTwo,
	                                  final int qualityTwo) {
		assumeFalse(directionOne == directionTwo,  "We can't have the same direction twice");
		IMutableMapNG map = new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 1);
		map.setBaseTerrain(new Point(0, 0), TileType.Plains);
		if (Direction.Nowhere != directionOne) {
			map.setRoadLevel(new Point(0, 0), directionOne, qualityOne);
		}
		if (Direction.Nowhere != directionTwo) {
			map.setRoadLevel(new Point(0, 0), directionTwo, qualityTwo);
		}
		assertDatabaseSerialization(map);
	}
}
