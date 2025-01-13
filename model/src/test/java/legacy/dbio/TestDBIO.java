package legacy.dbio;

import static lovelace.util.SingletonRandom.SINGLETON_RANDOM;

import legacy.map.HasNotes;
import legacy.map.LegacyPlayerCollection;
import legacy.map.fixtures.LegacyQuantity;
import legacy.map.fixtures.mobile.IMutableWorker;
import io.jenetics.facilejdbc.Transactional;

import java.io.IOException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.math.BigDecimal;

import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;

import legacy.map.fixtures.resources.CultivationStatus;
import legacy.map.fixtures.resources.ExposureStatus;
import legacy.xmlio.TestXMLIO;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.sqlite.SQLiteDataSource;

import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import legacy.map.LegacyMap;
import legacy.map.MapDimensions;
import legacy.map.MapDimensionsImpl;
import legacy.map.PlayerImpl;
import legacy.map.HasOwner;
import legacy.map.Player;
import legacy.map.TileFixture;
import legacy.map.Point;
import legacy.map.Direction;
import legacy.map.TileType;
import legacy.map.fixtures.ResourcePileImpl;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.Ground;
import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.explorable.AdventureFixture;
import legacy.map.fixtures.explorable.Portal;
import legacy.map.fixtures.explorable.Cave;
import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.mobile.Unit;
import legacy.map.fixtures.mobile.Sphinx;
import legacy.map.fixtures.mobile.Immortal;
import legacy.map.fixtures.mobile.Djinn;
import legacy.map.fixtures.mobile.Griffin;
import legacy.map.fixtures.mobile.Minotaur;
import legacy.map.fixtures.mobile.Ogre;
import legacy.map.fixtures.mobile.Phoenix;
import legacy.map.fixtures.mobile.Simurgh;
import legacy.map.fixtures.mobile.Troll;
import legacy.map.fixtures.mobile.Centaur;
import legacy.map.fixtures.mobile.Dragon;
import legacy.map.fixtures.mobile.Fairy;
import legacy.map.fixtures.mobile.Worker;
import legacy.map.fixtures.mobile.Giant;
import legacy.map.fixtures.mobile.AnimalImpl;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.Snowbird;
import legacy.map.fixtures.mobile.Thunderbird;
import legacy.map.fixtures.mobile.Pegasus;
import legacy.map.fixtures.mobile.Unicorn;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Kraken;
import common.map.fixtures.mobile.worker.RaceFactory;
import legacy.map.fixtures.mobile.worker.Job;
import legacy.map.fixtures.mobile.worker.Skill;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.FieldStatus;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.StoneDeposit;
import legacy.map.fixtures.resources.StoneKind;
import legacy.map.fixtures.resources.Shrub;
import legacy.map.fixtures.terrain.Forest;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Oasis;
import common.map.fixtures.towns.TownStatus;
import legacy.map.fixtures.towns.Fortification;
import common.map.fixtures.towns.TownSize;
import legacy.map.fixtures.towns.CommunityStats;
import legacy.map.fixtures.towns.CommunityStatsImpl;
import legacy.map.fixtures.towns.City;
import legacy.map.fixtures.towns.Town;
import legacy.map.fixtures.towns.FortressImpl;
import legacy.map.fixtures.towns.Village;
import common.xmlio.Warning;
import legacy.map.fixtures.towns.IMutableFortress;
import legacy.map.fixtures.mobile.IMutableUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;
import java.util.Collections;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.stream.Collector;
import java.util.HashSet;

import legacy.map.fixtures.TerrainFixture;

/**
 * Tests of the SQLite database I/O functionality.
 *
 * TODO: All tests should be more robust, as if developed test-first
 *
 * TODO: Randomize string inputs
 *
 * TODO: Figure out a way to share tests with {@link TestXMLIO}
 */
public final class TestDBIO {
	private static final Collection<IntFunction<Immortal>> simpleImmortalConstructors =
			java.util.List.of(Sphinx::new, Djinn::new, Griffin::new, Minotaur::new, Ogre::new, Phoenix::new,
					Simurgh::new, Troll::new, Snowbird::new, Thunderbird::new, Pegasus::new, Unicorn::new,
					Kraken::new);


	@FunctionalInterface
	private interface StringIntConstructor<Type> {
		Type apply(String str, int num);
	}

	private static final Collection<StringIntConstructor<Immortal>> kindedImmortalConstructors =
			java.util.List.of(Centaur::new, Dragon::new, Fairy::new, Giant::new);

	private static final Collection<IntFunction<TileFixture>> simpleTerrainConstructors =
			java.util.List.of(Hill::new, Oasis::new);

	private static final Collection<String> races = new HashSet<>(RaceFactory.RACES);

	@SuppressWarnings("JDBCPrepareStatementWithNonConstantString") // This is a delegating wrapper class.
	private record PersistentConnection(Connection wrapped) implements Connection {

		@Override
		public Statement createStatement() throws SQLException {
			return wrapped.createStatement();
		}

		@Override
		public PreparedStatement prepareStatement(final String sql) throws SQLException {
			return wrapped.prepareStatement(sql);
		}

		@Override
		public CallableStatement prepareCall(final String sql) throws SQLException {
			return wrapped.prepareCall(sql);
		}

		@Override
		public String nativeSQL(final String sql) throws SQLException {
			return wrapped.nativeSQL(sql);
		}

		@Override
		public void setAutoCommit(final boolean autoCommit) throws SQLException {
			wrapped.setAutoCommit(autoCommit);
		}

		@Override
		public boolean getAutoCommit() throws SQLException {
			return wrapped.getAutoCommit();
		}

		@Override
		public void commit() throws SQLException {
			wrapped.commit();
		}

		@Override
		public void rollback() throws SQLException {
			wrapped.rollback();
		}

		@Override
		public void close() {
			// Deliberate noop
		}

		@Override
		public boolean isClosed() throws SQLException {
			return wrapped.isClosed();
		}

		@Override
		public DatabaseMetaData getMetaData() throws SQLException {
			return wrapped.getMetaData();
		}

		@Override
		public void setReadOnly(final boolean readOnly) throws SQLException {
			wrapped.setReadOnly(readOnly);
		}

		@Override
		public boolean isReadOnly() throws SQLException {
			return wrapped.isReadOnly();
		}

		@Override
		public void setCatalog(final String catalog) throws SQLException {
			wrapped.setCatalog(catalog);
		}

		@Override
		public String getCatalog() throws SQLException {
			return wrapped.getCatalog();
		}

		@Override
		public void setTransactionIsolation(final int level) throws SQLException {
			wrapped.setTransactionIsolation(level);
		}

		@Override
		public int getTransactionIsolation() throws SQLException {
			return wrapped.getTransactionIsolation();
		}

		@Override
		public SQLWarning getWarnings() throws SQLException {
			return wrapped.getWarnings();
		}

		@Override
		public void clearWarnings() throws SQLException {
			wrapped.clearWarnings();
		}

		@Override
		public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
			return wrapped.createStatement(resultSetType, resultSetConcurrency);
		}

		@Override
		public PreparedStatement prepareStatement(final String sql, final int resultSetType,
												  final int resultSetConcurrency) throws SQLException {
			return wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency);
		}

		@Override
		public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency)
				throws SQLException {
			return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency);
		}

		@Override
		public Map<String, Class<?>> getTypeMap() throws SQLException {
			return wrapped.getTypeMap();
		}

		@Override
		public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
			wrapped.setTypeMap(map);
		}

		@Override
		public void setHoldability(final int holdability) throws SQLException {
			wrapped.setHoldability(holdability);
		}

		@Override
		public int getHoldability() throws SQLException {
			return wrapped.getHoldability();
		}

		@Override
		public Savepoint setSavepoint() throws SQLException {
			return wrapped.setSavepoint();
		}

		@Override
		public Savepoint setSavepoint(final String name) throws SQLException {
			return wrapped.setSavepoint(name);
		}

		@Override
		public void rollback(final Savepoint savepoint) throws SQLException {
			wrapped.rollback(savepoint);
		}

		@Override
		public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
			wrapped.releaseSavepoint(savepoint);
		}

		@Override
		public Statement createStatement(final int resultSetType, final int resultSetConcurrency,
										 final int resultSetHoldability) throws SQLException {
			return wrapped.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		@Override
		public PreparedStatement prepareStatement(final String sql, final int resultSetType,
												  final int resultSetConcurrency, final int resultSetHoldability)
				throws SQLException {
			return wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		@Override
		public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency
				, final int resultSetHoldability) throws SQLException {
			return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		@Override
		public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
			return wrapped.prepareStatement(sql, autoGeneratedKeys);
		}

		@Override
		public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
			return wrapped.prepareStatement(sql, columnIndexes);
		}

		@Override
		public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
			return wrapped.prepareStatement(sql, columnNames);
		}

		@Override
		public Clob createClob() throws SQLException {
			return wrapped.createClob();
		}

		@Override
		public Blob createBlob() throws SQLException {
			return wrapped.createBlob();
		}

		@Override
		public NClob createNClob() throws SQLException {
			return wrapped.createNClob();
		}

		@Override
		public SQLXML createSQLXML() throws SQLException {
			return wrapped.createSQLXML();
		}

		@Override
		public boolean isValid(final int timeout) throws SQLException {
			return wrapped.isValid(timeout);
		}

		@Override
		public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
			wrapped.setClientInfo(name, value);
		}

		@Override
		public void setClientInfo(final Properties properties) throws SQLClientInfoException {
			wrapped.setClientInfo(properties);
		}

		@Override
		public String getClientInfo(final String name) throws SQLException {
			return wrapped.getClientInfo(name);
		}

		@Override
		public Properties getClientInfo() throws SQLException {
			return wrapped.getClientInfo();
		}

		@Override
		public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
			return wrapped.createArrayOf(typeName, elements);
		}

		@Override
		public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
			return wrapped.createStruct(typeName, attributes);
		}

		@Override
		public void setSchema(final String schema) throws SQLException {
			wrapped.setSchema(schema);
		}

		@Override
		public String getSchema() throws SQLException {
			return wrapped.getSchema();
		}

		@Override
		public void abort(final Executor executor) throws SQLException {
			wrapped.abort(executor);
		}

		@Override
		public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
			wrapped.setNetworkTimeout(executor, milliseconds);
		}

		@Override
		public int getNetworkTimeout() throws SQLException {
			return wrapped.getNetworkTimeout();
		}

		@Override
		public <T> T unwrap(final Class<T> iface) throws SQLException {
			return wrapped.unwrap(iface);
		}

		@Override
		public boolean isWrapperFor(final Class<?> iface) throws SQLException {
			return wrapped.isWrapperFor(iface);
		}

		public void reallyClose() throws SQLException {
			wrapped.close();
		}
	}

	private static final class TestDatabase implements Transactional {
		private @Nullable SQLiteDataSource source;
		private @Nullable PersistentConnection connection;

		public void tearDown() throws SQLException {
			if (Objects.nonNull(connection)) {
				connection.reallyClose();
			}
			connection = null;
			source = null;
		}

		@Override
		public Connection connection() throws SQLException {
			final SQLiteDataSource existingSource = source;
			if (Objects.isNull(existingSource)) {
				final SQLiteDataSource ds = new SQLiteDataSource();
				ds.setSharedCache(true);
				ds.setReadUncommitted(true);
				ds.setUrl("jdbc:sqlite::memory:");
				source = ds;
				connection = new PersistentConnection(ds.getConnection());
				connection.setAutoCommit(false);
				connection.setTransactionIsolation(
						Connection.TRANSACTION_READ_UNCOMMITTED);
				return connection;
			} else if (Objects.nonNull(connection)) {
				return connection;
			} else {
				connection = new PersistentConnection(existingSource.getConnection());
				connection.setAutoCommit(false);
				connection.setTransactionIsolation(
						Connection.TRANSACTION_READ_UNCOMMITTED);
				return connection;
			}
		}
	}

	private TestDatabase db;

	private final SPDatabaseWriter writer = new SPDatabaseWriter();

	private final SPDatabaseReader reader = new SPDatabaseReader();

	private @Nullable Connection connection;

	@BeforeEach
	public void setUp() {
		db = new TestDatabase();
	}

	@AfterEach
	public void tearDown() throws SQLException {
		db.tearDown();
	}

	private ILegacyMap assertDatabaseSerialization(final ILegacyMap map) throws SQLException, IOException {
		final TestDatabase db = new TestDatabase();
		writer.writeToDatabase(db, map);
		reader.clearCache();
		final ILegacyMap deserialized = reader.readMapFromDatabase(db, Warning.DIE);
		assertEquals(map, deserialized, "Deserialized form is the same as original");
		db.tearDown();
		return deserialized;
	}

	@SuppressWarnings("unchecked") // this is test code anyway
	private <FixtureType extends TileFixture> FixtureType
	assertFixtureSerialization(final FixtureType fixture) throws SQLException, IOException {
		final MapDimensions dimensions = new MapDimensionsImpl(2, 2, 2);
		final IMutableLegacyMap firstMap = new LegacyMap(dimensions, new LegacyPlayerCollection(), -1);
		firstMap.addFixture(new Point(0, 0), fixture);
		final IMutableLegacyMap secondMap = new LegacyMap(dimensions, new LegacyPlayerCollection(), -1);
		secondMap.addFixture(new Point(1, 1), fixture);
		if (fixture instanceof final HasOwner owned) {
			firstMap.addPlayer(owned.owner());
			secondMap.addPlayer(owned.owner());
		}
		final ILegacyMap deserializedFirst = assertDatabaseSerialization(firstMap);
		final ILegacyMap deserializedSecond = assertDatabaseSerialization(secondMap);
		assertNotEquals(deserializedFirst, deserializedSecond,
				"DB round-trip preserves not-equality of with and without fixture");
		return (FixtureType) deserializedFirst.getFixtures(new Point(0, 0))
				.stream().findFirst().get();
	}

	private static Stream<Arguments> fewIntegers() {
		return SINGLETON_RANDOM.ints(3).boxed().map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("fewIntegers")
	public void testAdventureSerialization(final int id) throws SQLException, IOException {
		assertFixtureSerialization(new AdventureFixture(new PlayerImpl(1, "independent"),
				"hook brief", "hook full", id));
	}

	private static Stream<Arguments> testPortalSerialization() {
		return SINGLETON_RANDOM.ints(2, -1, Integer.MAX_VALUE).boxed().flatMap(a ->
				SINGLETON_RANDOM.ints(2).boxed().flatMap(b ->
						SINGLETON_RANDOM.ints(2, -1, Integer.MAX_VALUE).boxed().map(c ->
								Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testPortalSerialization(final int id, final int row, final int column)
			throws SQLException, IOException {
		assumeTrue((row == -1 && column == -1) || (row >= 0 && column >= 0),
				"Destination row and column must be either both -1 or both nonnegative");
		assertFixtureSerialization(new Portal("portal dest", new Point(row, column), id));
	}

	// TODO: Extract helper to reduce verbosity of boxed() with ints(), as in TestXMLIO?
	private static Stream<Boolean> bools() {
		return Stream.of(true, false);
	}

	private static Stream<Arguments> testAnimalSerialization() {
		return SINGLETON_RANDOM.ints(2).boxed().flatMap(a ->
				bools().map(b -> Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testAnimalSerialization(final int id, final boolean talking) throws SQLException, IOException {
		assertFixtureSerialization(new AnimalImpl("animal kind", talking, "status", id, -1, 1));
	}

	@Test
	public void testTracksSerialization() throws SQLException, IOException {
		assertFixtureSerialization(new AnimalTracks("kind"));
	}

	@ParameterizedTest
	@MethodSource("fewIntegers")
	public void testCacheSerialization(final int id) throws SQLException, IOException {
		assertFixtureSerialization(new CacheFixture("kind", "contents", id));
	}

	private static Stream<Arguments> testCaveSerialization() {
		return SINGLETON_RANDOM.ints(2).boxed().flatMap(a ->
				SINGLETON_RANDOM.ints(2).boxed().map(b -> Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testCaveSerialization(final int id, final int dc) throws SQLException, IOException {
		assertFixtureSerialization(new Cave(dc, id));
	}

	@ParameterizedTest
	@MethodSource("testCaveSerialization")
	public void testBattlefieldSerialization(final int id, final int dc) throws SQLException, IOException {
		assertFixtureSerialization(new Battlefield(dc, id));
	}

	private static Stream<Arguments> testCitySerialization() {
		return SINGLETON_RANDOM.ints(2).boxed().flatMap(a ->
				Stream.of(TownStatus.values()).flatMap(b ->
						Stream.of(TownSize.values()).map(c ->
								Arguments.of(a, b, c, SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE)))));
	}

	@SuppressWarnings("MagicNumber")
	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testFortificationSerialization(final int id, final TownStatus status, final TownSize size, final int dc)
			throws SQLException, IOException {
		// TODO: We want more of the state to be random
		final Fortification town = new Fortification(status, size, dc, "name", id, new PlayerImpl(0, ""));
		final CommunityStats stats = new CommunityStatsImpl(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.addYearlyProduction(new ResourcePileImpl(1, "first", "first detail",
				new LegacyQuantity(5, "pounds")));
		stats.addYearlyProduction(new ResourcePileImpl(2, "second", "second detail",
				new LegacyQuantity(BigDecimal.ONE.divide(new BigDecimal(2)), "quarts")));
		stats.addYearlyConsumption(new ResourcePileImpl(3, "third", "third detail",
				new LegacyQuantity(8, "pecks")));
		stats.addYearlyConsumption(new ResourcePileImpl(4, "fourth", "fourth detail",
				new LegacyQuantity(new BigDecimal(5).divide(new BigDecimal(4)), "square feet")));
		town.setPopulation(stats);
		assertFixtureSerialization(town);
	}

	@SuppressWarnings("MagicNumber")
	@ParameterizedTest
	@MethodSource
	public void testCitySerialization(final int id, final TownStatus status, final TownSize size, final int dc)
			throws SQLException, IOException {
		// TODO: We want more of the state to be random
		final City town = new City(status, size, dc, "name", id, new PlayerImpl(0, ""));
		final CommunityStats stats = new CommunityStatsImpl(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.addYearlyProduction(new ResourcePileImpl(1, "first", "first detail",
				new LegacyQuantity(5, "pounds")));
		stats.addYearlyProduction(new ResourcePileImpl(2, "second", "second detail",
				new LegacyQuantity(BigDecimal.ONE.divide(new BigDecimal(2)), "quarts")));
		stats.addYearlyConsumption(new ResourcePileImpl(3, "third", "third detail",
				new LegacyQuantity(8, "pecks")));
		stats.addYearlyConsumption(new ResourcePileImpl(4, "fourth", "fourth detail",
				new LegacyQuantity(new BigDecimal(5).divide(new BigDecimal(4)), "square feet")));
		town.setPopulation(stats);
		assertFixtureSerialization(town);
	}

	@SuppressWarnings("MagicNumber")
	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testTownSerialization(final int id, final TownStatus status, final TownSize size, final int dc)
			throws SQLException, IOException {
		// TODO: We want more of the state to be random
		final Town town = new Town(status, size, dc, "name", id, new PlayerImpl(0, ""));
		final CommunityStats stats = new CommunityStatsImpl(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.addYearlyProduction(new ResourcePileImpl(1, "first", "first detail",
				new LegacyQuantity(5, "pounds")));
		stats.addYearlyProduction(new ResourcePileImpl(2, "second", "second detail",
				new LegacyQuantity(BigDecimal.ONE.divide(new BigDecimal(2)), "quarts")));
		stats.addYearlyConsumption(new ResourcePileImpl(3, "third", "third detail",
				new LegacyQuantity(8, "pecks")));
		stats.addYearlyConsumption(new ResourcePileImpl(4, "fourth", "fourth detail",
				new LegacyQuantity(new BigDecimal(5).divide(new BigDecimal(4)), "square feet")));
		town.setPopulation(stats);
		assertFixtureSerialization(town);
	}

	private static Stream<Arguments> testMeadowSerialization() {
		return Stream.of(Meadow.MeadowType.values()).flatMap(a -> Stream.of(CultivationStatus.values()).flatMap(b ->
				Stream.of(FieldStatus.values()).map(c ->
						Arguments.of(a, b, SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE), c,
								SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE)))));
	}

	@ParameterizedTest
	@MethodSource
	public void testMeadowSerialization(final Meadow.MeadowType type, final CultivationStatus cultivation, final int id,
										final FieldStatus status, final int acres) throws SQLException, IOException {
		assertFixtureSerialization(new Meadow("kind", type, cultivation, id, status, acres));
		assertFixtureSerialization(new Meadow("kind", type, cultivation, id, status,
				new BigDecimal(acres).add(BigDecimal.ONE.divide(new BigDecimal(2)))));
	}

	private static Stream<Arguments> testForestSerialization() {
		return bools().map(a -> Arguments.of(a, SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE),
				SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE)));
	}

	@ParameterizedTest
	@MethodSource
	public void testForestSerialization(final boolean rows, final int id, final int acres)
			throws SQLException, IOException {
		assertFixtureSerialization(new Forest("kind", rows, id, acres));
	}

	@ParameterizedTest
	@MethodSource("testForestSerialization")
	public void testFractionalForestSerialization(final boolean rows, final int id, final int acres)
			throws SQLException, IOException {
		assertFixtureSerialization(new Forest("kind", rows, id,
				new BigDecimal(acres).add(BigDecimal.ONE.divide(new BigDecimal(4)))));
	}

	private static Stream<Arguments> testFortressSerialization() {
		return SINGLETON_RANDOM.ints(2).boxed().flatMap(a ->
				Stream.of(TownSize.values()).map(b -> Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testFortressSerialization(final int id, final TownSize size) throws SQLException, IOException {
		final Player owner = new PlayerImpl(1, "owner");
		final IMutableFortress fortress = new FortressImpl(owner, "fortress", id, size);
		final IMutableUnit unit = new Unit(owner, "unitKind", "unitName", id + 2);
		unit.addMember(new Worker("workerName", "human", id + 3, new Job("jobName", 2)));
		unit.addMember(new Sphinx(id + 5));
		unit.addMember(new ResourcePileImpl(id + 6, "resource kind", "resource contents",
				new LegacyQuantity(8, "counts")));
		fortress.addMember(unit);
		fortress.addMember(new Implement("equipment", id + 4, 2));
		fortress.addMember(new ResourcePileImpl(id + 7, "second resource", "second contents",
				new LegacyQuantity(new BigDecimal(3).divide(new BigDecimal(4)), "gallon")));
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
		return SINGLETON_RANDOM.ints(2).boxed().flatMap(a ->
				Stream.of(ExposureStatus.values()).flatMap(b ->
						races.stream().collect(toShuffledStream(2)).map(c ->
								Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testGroundSerialization(final int id, final ExposureStatus exposure, final String kind)
			throws SQLException, IOException {
		assertFixtureSerialization(new Ground(id, kind, exposure));
	}

	private static Stream<Arguments> testGroveSerialization() {
		return Stream.of(Grove.GroveType.values()).flatMap(a -> Stream.of(CultivationStatus.values()).flatMap(b ->
				SINGLETON_RANDOM.ints(2).boxed().flatMap(c ->
						races.stream().collect(toShuffledStream(1)).map(d ->
								Arguments.of(a, b, c, SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE), d)))));
	}

	@ParameterizedTest
	@MethodSource
	public void testGroveSerialization(final Grove.GroveType type, final CultivationStatus cultivation, final int id,
	                                   final int count, final String kind) throws SQLException, IOException {
		assertFixtureSerialization(new Grove(type, cultivation, kind, id, count));
	}

	private static Stream<Arguments> testSimpleImmortalSerialization() {
		return simpleImmortalConstructors.stream().flatMap(a ->
				SINGLETON_RANDOM.ints(2).boxed().map(b ->
						Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testSimpleImmortalSerialization(final IntFunction<Immortal> constructor, final int id)
			throws SQLException, IOException {
		assertFixtureSerialization(constructor.apply(id));
	}

	private static Stream<Arguments> testKindedImmortalSerialization() {
		return kindedImmortalConstructors.stream().flatMap(a ->
				SINGLETON_RANDOM.ints(2).boxed().flatMap(b ->
						races.stream().collect(toShuffledStream(2)).map(c ->
								Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testKindedImmortalSerialization(final StringIntConstructor<Immortal> constructor, final int id,
												final String kind) throws SQLException, IOException {
		assertFixtureSerialization(constructor.apply(kind, id));
	}

	private static Stream<Arguments> testMineSerialization() {
		return Stream.of(TownStatus.values()).flatMap(a ->
				SINGLETON_RANDOM.ints(2).boxed().flatMap(b ->
						races.stream().collect(toShuffledStream(1)).map(c ->
								Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testMineSerialization(final TownStatus status, final int id, final String kind)
			throws SQLException, IOException {
		assertFixtureSerialization(new Mine(kind, status, id));
	}

	private static Stream<Arguments> testMineralSerialization() {
		return Stream.of(ExposureStatus.values()).flatMap(a ->
				SINGLETON_RANDOM.ints(2).boxed().flatMap(b ->
						SINGLETON_RANDOM.ints(2).boxed().flatMap(c ->
								races.stream().collect(toShuffledStream(1)).map(d ->
										Arguments.of(a, b, c, d)))));
	}

	@ParameterizedTest
	@MethodSource
	public void testMineralSerialization(final ExposureStatus exposure, final int dc, final int id, final String kind)
			throws SQLException, IOException {
		assertFixtureSerialization(new MineralVein(kind, exposure, dc, id));
	}

	private static Stream<Arguments> testStoneSerialization() {
		return Stream.of(StoneKind.values()).flatMap(a ->
				SINGLETON_RANDOM.ints(2).boxed().flatMap(b ->
						SINGLETON_RANDOM.ints(2).boxed().map(c ->
								Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testStoneSerialization(final StoneKind kind, final int dc, final int id)
			throws SQLException, IOException {
		assertFixtureSerialization(new StoneDeposit(kind, dc, id));
	}

	private static Stream<Arguments> testShrubSerialization() {
		return SINGLETON_RANDOM.ints(2).boxed().flatMap(a ->
				SINGLETON_RANDOM.ints(2).boxed().flatMap(b ->
						races.stream().collect(toShuffledStream(1)).map(c ->
								Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testShrubSerialization(final int id, final int count, final String kind)
			throws SQLException, IOException {
		assertFixtureSerialization(new Shrub(kind, id, count));
	}

	private static Stream<Arguments> testSimpleTerrainSerialization() {
		return simpleTerrainConstructors.stream().flatMap(a ->
				SINGLETON_RANDOM.ints(2).boxed().map(b ->
						Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testSimpleTerrainSerialization(final IntFunction<TerrainFixture> constructor, final int id)
			throws SQLException, IOException {
		assertFixtureSerialization(constructor.apply(id));
	}

	// TODO: randomize text
	@ParameterizedTest
	@MethodSource("fewIntegers")
	public void testTextSerialization(final int turn) throws SQLException, IOException {
		assumeTrue(turn >= 0, "Turn (as adjusted) must be greater than or equal to -1");
		assertFixtureSerialization(new TextFixture("test text", turn - 1));
	}

	@ParameterizedTest
	@MethodSource("fewIntegers")
	public void testUnitSerialization(final int id) throws SQLException, IOException {
		final Player owner = new PlayerImpl(1, "owner");
		final IMutableUnit unit = new Unit(owner, "unitKind", "unitName", id);
		unit.addMember(new Worker("worker name", "elf", id + 1, new Job("job name", 2,
				new Skill("first skill", 1, 2), new Skill("second skill", 3, 4)),
				new Job("second job", 4)));
		unit.addMember(new Centaur("horse", id + 5));
		unit.addMember(new AnimalImpl("elephant", false, "domesticated", id + 6, -1, 4));
		assertFixtureSerialization(unit);
	}

	@ParameterizedTest
	@MethodSource("fewIntegers")
	public void testWorkerEquipment(final int id) throws SQLException, IOException {
		final IMutableWorker worker = new Worker("worker name", "elf", id);
		worker.setMount(new AnimalImpl("animal kind", false, "tame", id + 1));
		worker.addEquipment(new Implement("equipment kind one", id + 2));
		worker.addEquipment(new Implement("equipment kind two", id + 3));
		final Player owner = new PlayerImpl(1, "owner"); // randomize player-ID, surely?
		final IMutableUnit unit = new Unit(owner, "unitKind", "unitName", id + 5);
		unit.addMember(worker);
		assertFixtureSerialization(unit);
	}

	private static Stream<Arguments> testVillageSerialization() {
		return Stream.of(TownStatus.values()).flatMap(a ->
				races.stream().collect(toShuffledStream(3)).map(b ->
						Arguments.of(a, SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE), b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testVillageSerialization(final TownStatus status, final int id, final String race)
			throws SQLException, IOException {
		assertFixtureSerialization(new Village(status, "village name", id,
				new PlayerImpl(1, "player name"), race));
	}

	private static Stream<Arguments> testNotesSerialization() {
		return races.stream().collect(toShuffledStream(2)).map(a ->
				Arguments.of(SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE),
						SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE), a));
	}

	@ParameterizedTest
	@MethodSource
	public void testNotesSerialization(final int id, final int player, final String note)
			throws SQLException, IOException {
		final Worker worker = new Worker("test worker", "human", id);
		final Player playerObj = new PlayerImpl(player, "");
		worker.setNote(playerObj, note);
		final Unit unit = new Unit(playerObj, "unitKind", "unitName", id + 1);
		unit.addMember(worker);
		final IUnit deserializedUnit = assertFixtureSerialization(unit);
		assertEquals(note, ((HasNotes) deserializedUnit.iterator().next()).getNote(playerObj),
				"Note was deserialized");
	}

	@Test
	public void testBookmarkSerialization() throws SQLException, IOException {
		final IMutableLegacyMap map = new LegacyMap(new MapDimensionsImpl(1, 1, 2), new LegacyPlayerCollection(), 1);
		final Player player = map.getPlayers().getPlayer(1);
		map.setCurrentPlayer(player);
		assertFalse(map.getBookmarks().contains(new Point(0, 0)),
				"Map by default doesn't have a bookmark");
		assertEquals(0, map.getAllBookmarks(new Point(0, 0)).size(),
				"Map by default has no bookmarks");
		map.addBookmark(new Point(0, 0));
		final ILegacyMap deserialized = assertDatabaseSerialization(map);
		assertNotSame(map, deserialized, "Deserialization doesn't just return the input");
		assertTrue(deserialized.getBookmarks().contains(new Point(0, 0)),
				"Deserialized map has the bookmark we saved");
	}

	private static Stream<Arguments> testRoadSerialization() {
		return Stream.of(Direction.values()).flatMap(a ->
				Stream.of(Direction.values()).map(b ->
						Arguments.of(a, SINGLETON_RANDOM.nextInt(7) + 1, b,
								SINGLETON_RANDOM.nextInt(7) + 1)));
	}

	@ParameterizedTest
	@MethodSource
	public void testRoadSerialization(final Direction directionOne, final int qualityOne, final Direction directionTwo,
									  final int qualityTwo) throws SQLException, IOException {
		assumeFalse(directionOne == directionTwo, "We can't have the same direction twice");
		assumeTrue(qualityOne >= 0 && qualityTwo >= 0, "Road quality must be nonnegative");
		final IMutableLegacyMap map = new LegacyMap(new MapDimensionsImpl(1, 1, 2), new LegacyPlayerCollection(), 1);
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
