package impl.dbio;

import static lovelace.util.SingletonRandom.SINGLETON_RANDOM;

import common.map.fixtures.mobile.IMutableWorker;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
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
import common.map.fixtures.TerrainFixture;

/**
 * Tests of the SQLite database I/O functionality.
 *
 * TODO: All tests should be more robust, as if developed test-first
 *
 * TODO: Randomize string inputs
 *
 * TODO: Figure out a way to share tests with {@link impl.xmlio.TestXMLIO}
 */
public final class TestDBIO {
	private static final Collection<IntFunction<Immortal>> simpleImmortalConstructors =
			java.util.List.of(Sphinx::new, Djinn::new, Griffin::new, Minotaur::new, Ogre::new, Phoenix::new, Simurgh::new, Troll::new, Snowbird::new, Thunderbird::new, Pegasus::new, Unicorn::new, Kraken::new);


	@FunctionalInterface
	private interface StringIntConstructor<Type> {
		Type apply(String str, int num);
	}

	private static final Collection<StringIntConstructor<Immortal>> kindedImmortalConstructors =
			java.util.List.of(Centaur::new, Dragon::new, Fairy::new, Giant::new);

	private static final Collection<IntFunction<TileFixture>> simpleTerrainConstructors =
			java.util.List.of(Hill::new, Oasis::new);

	private static final Collection<String> races = new HashSet<>(RaceFactory.RACES);

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
			public void close() throws SQLException {
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
			public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
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
					final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
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

	private static class TestDatabase implements Transactional {
		private @Nullable SQLiteDataSource source;
		private @Nullable PersistentConnection connection;
		public void tearDown() throws SQLException {
			if (connection != null) {
				connection.close();
			}
			connection = null;
			source = null;
		}

		@Override
		public Connection connection() throws SQLException {
			SQLiteDataSource ds = source;
			if (ds == null) {
				ds = new SQLiteDataSource();
				ds.setSharedCache(true);
				ds.setReadUncommitted(true);
				ds.setUrl("jdbc:sqlite::memory:");
				source = ds;
				connection = new PersistentConnection(ds.getConnection());
				connection.setAutoCommit(false);
				connection.setTransactionIsolation(
						Connection.TRANSACTION_READ_UNCOMMITTED);
				return connection;
			} else if (connection != null) {
				return connection;
			} else {
				connection = new PersistentConnection(ds.getConnection());
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
	public void setUp() throws SQLException, IOException {
		db = new TestDatabase();
	}

	@AfterEach
	public void tearDown() throws SQLException, IOException {
		db.tearDown();
	}

	private IMapNG assertDatabaseSerialization(final IMapNG map) throws SQLException, IOException {
		final TestDatabase db = new TestDatabase();
		writer.writeToDatabase(db, map);
		reader.clearCache();
		final IMapNG deserialized = reader.readMapFromDatabase(db, Warning.DIE);
		assertEquals(map, deserialized, "Deserialized form is the same as original");
		db.tearDown();
		return deserialized;
	}

	private <FixtureType extends TileFixture> FixtureType
			assertFixtureSerialization(final FixtureType fixture) throws SQLException, IOException {
		final MapDimensions dimensions = new MapDimensionsImpl(2, 2, 2);
		final IMutableMapNG firstMap = new SPMapNG(dimensions, new PlayerCollection(), -1);
		firstMap.addFixture(new Point(0, 0), fixture);
		final IMutableMapNG secondMap = new SPMapNG(dimensions, new PlayerCollection(), -1);
		secondMap.addFixture(new Point(1, 1), fixture);
		if (fixture instanceof HasOwner owned) {
			firstMap.addPlayer(owned.owner());
			secondMap.addPlayer(owned.owner());
		}
		final IMapNG deserializedFirst = assertDatabaseSerialization(firstMap);
		final IMapNG deserializedSecond = assertDatabaseSerialization(secondMap);
		assertNotEquals(deserializedFirst, deserializedSecond,
				"DB round-trip preserves not-equality of with and without fixture");
		final FixtureType retval =
			(FixtureType) deserializedFirst.getFixtures(new Point(0, 0))
				.stream().findFirst().get();
		return retval;
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
		return SINGLETON_RANDOM.ints(2).boxed().flatMap(a ->
			SINGLETON_RANDOM.ints(2).boxed().flatMap(b ->
				SINGLETON_RANDOM.ints(2).boxed().map(c ->
					Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testPortalSerialization(final int id, final int row, final int column) throws SQLException, IOException {
		assumeTrue((row == -1 && column == -1) || (row >= 0 && column >= 0),
				"Destination row and column must be either both -1 or both nonnegative");
		assertFixtureSerialization(new Portal("portal dest", new Point(row, column), id));
	}

	private static final List<Boolean> BOOLS = List.of(true, false);

	// TODO: Reformat methods using this helper and ints()
	// TODO: Extract helper to reduce verbosity of boxed() with ints(), as in TestXMLIO?
	private static Stream<Boolean> bools() {
		return BOOLS.stream();
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

	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testFortificationSerialization(final int id, final TownStatus status, final TownSize size, final int dc)
			throws SQLException, IOException {
		// TODO: We want more of the state to be random
		final Fortification town = new Fortification(status, size, dc, "name", id, new PlayerImpl(0, ""));
		final CommunityStats stats = new CommunityStats(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.getYearlyProduction().add(new ResourcePileImpl(1, "first", "first detail",
			new Quantity(5, "pounds")));
		stats.getYearlyProduction().add(new ResourcePileImpl(2, "second", "second detail",
			new Quantity(BigDecimal.ONE.divide(new BigDecimal(2)), "quarts")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(3, "third", "third detail",
			new Quantity(8, "pecks")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(4, "fourth", "fourth detail",
			new Quantity(new BigDecimal(5).divide(new BigDecimal(4)), "square feet")));
		town.setPopulation(stats);
		assertFixtureSerialization(town);
	}

	@ParameterizedTest
	@MethodSource
	public void testCitySerialization(final int id, final TownStatus status, final TownSize size, final int dc)
			throws SQLException, IOException {
		// TODO: We want more of the state to be random
		final City town = new City(status, size, dc, "name", id, new PlayerImpl(0, ""));
		final CommunityStats stats = new CommunityStats(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.getYearlyProduction().add(new ResourcePileImpl(1, "first", "first detail",
			new Quantity(5, "pounds")));
		stats.getYearlyProduction().add(new ResourcePileImpl(2, "second", "second detail",
			new Quantity(BigDecimal.ONE.divide(new BigDecimal(2)), "quarts")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(3, "third", "third detail",
			new Quantity(8, "pecks")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(4, "fourth", "fourth detail",
			new Quantity(new BigDecimal(5).divide(new BigDecimal(4)), "square feet")));
		town.setPopulation(stats);
		assertFixtureSerialization(town);
	}

	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testTownSerialization(final int id, final TownStatus status, final TownSize size, final int dc)
			throws SQLException, IOException {
		// TODO: We want more of the state to be random
		final Town town = new Town(status, size, dc, "name", id, new PlayerImpl(0, ""));
		final CommunityStats stats = new CommunityStats(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.getYearlyProduction().add(new ResourcePileImpl(1, "first", "first detail",
			new Quantity(5, "pounds")));
		stats.getYearlyProduction().add(new ResourcePileImpl(2, "second", "second detail",
			new Quantity(BigDecimal.ONE.divide(new BigDecimal(2)), "quarts")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(3, "third", "third detail",
			new Quantity(8, "pecks")));
		stats.getYearlyConsumption().add(new ResourcePileImpl(4, "fourth", "fourth detail",
			new Quantity(new BigDecimal(5).divide(new BigDecimal(4)), "square feet")));
		town.setPopulation(stats);
		assertFixtureSerialization(town);
	}

	private static Stream<Arguments> testMeadowSerialization() {
		return bools().flatMap(a -> bools().flatMap(b ->
			Stream.of(FieldStatus.values()).map(c ->
				Arguments.of(a, b, SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE), c,
					SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE)))));
	}

	@ParameterizedTest
	@MethodSource
	public void testMeadowSerialization(final boolean field, final boolean cultivated, final int id, final FieldStatus status,
	                                    final int acres) throws SQLException, IOException {
		assertFixtureSerialization(new Meadow("kind", field, cultivated, id, status, acres));
	}

	// TODO: Combine with testMeadowSerialization()?
	@ParameterizedTest
	@MethodSource("testMeadowSerialization")
	public void testFractionalMeadowSerialization(final boolean field, final boolean cultivated, final int id,
	                                              final FieldStatus status, final int acres) throws SQLException, IOException {
		assertFixtureSerialization(new Meadow("kind", field, cultivated, id, status,
			new BigDecimal(acres).add(BigDecimal.ONE.divide(new BigDecimal(2)))));
	}

	private static Stream<Arguments> testForestSerialization() {
		return bools().map(a -> Arguments.of(a, SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE),
			SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE)));
	}

	@ParameterizedTest
	@MethodSource
	public void testForestSerialization(final boolean rows, final int id, final int acres) throws SQLException, IOException {
		assertFixtureSerialization(new Forest("kind", rows, id, acres));
	}

	@ParameterizedTest
	@MethodSource("testForestSerialization")
	public void testFractionalForestSerialization(final boolean rows, final int id, final int acres) throws SQLException, IOException {
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
		return SINGLETON_RANDOM.ints(2).boxed().flatMap(a ->
			bools().flatMap(b ->
				races.stream().collect(toShuffledStream(2)).map(c ->
					Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testGroundSerialization(final int id, final boolean exposed, final String kind) throws SQLException, IOException {
		assertFixtureSerialization(new Ground(id, kind, exposed));
	}

	private static Stream<Arguments> testGroveSerialization() {
		return bools().flatMap(a -> bools().flatMap(b ->
			SINGLETON_RANDOM.ints(2).boxed().flatMap(c ->
				races.stream().collect(toShuffledStream(1)).map(d ->
					Arguments.of(a, b, c, SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE), d)))));
	}

	@ParameterizedTest
	@MethodSource
	public void testGroveSerialization(final boolean orchard, final boolean cultivated, final int id, final int count,
	                                   final String kind) throws SQLException, IOException {
		assertFixtureSerialization(new Grove(orchard, cultivated, kind, id, count));
	}

	private static Stream<Arguments> testSimpleImmortalSerialization() {
		return simpleImmortalConstructors.stream().flatMap(a ->
			SINGLETON_RANDOM.ints(2).boxed().map(b ->
				Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testSimpleImmortalSerialization(final IntFunction<Immortal> constructor, final int id) throws SQLException, IOException {
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
	public void testMineSerialization(final TownStatus status, final int id, final String kind) throws SQLException, IOException {
		assertFixtureSerialization(new Mine(kind, status, id));
	}

	private static Stream<Arguments> testMineralSerialization() {
		return bools().flatMap(a ->
			SINGLETON_RANDOM.ints(2).boxed().flatMap(b ->
				SINGLETON_RANDOM.ints(2).boxed().flatMap(c ->
					races.stream().collect(toShuffledStream(1)).map(d ->
						Arguments.of(a, b, c, d)))));
	}

	@ParameterizedTest
	@MethodSource
	public void testMineralSerialization(final boolean exposed, final int dc, final int id, final String kind) throws SQLException, IOException {
		assertFixtureSerialization(new MineralVein(kind, exposed, dc, id));
	}

	private static Stream<Arguments> testStoneSerialization() {
		return Stream.of(StoneKind.values()).flatMap(a ->
			SINGLETON_RANDOM.ints(2).boxed().flatMap(b ->
				SINGLETON_RANDOM.ints(2).boxed().map(c ->
						Arguments.of(a, b, c))));
	}

	@ParameterizedTest
	@MethodSource
	public void testStoneSerialization(final StoneKind kind, final int dc, final int id) throws SQLException, IOException {
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
	public void testShrubSerialization(final int id, final int count, final String kind) throws SQLException, IOException {
		assertFixtureSerialization(new Shrub(kind, id, count));
	}

	private static Stream<Arguments> testSimpleTerrainSerialization() {
		return simpleTerrainConstructors.stream().flatMap(a ->
			SINGLETON_RANDOM.ints(2).boxed().map(b ->
				Arguments.of(a, b)));
	}

	@ParameterizedTest
	@MethodSource
	public void testSimpleTerrainSerialization(final IntFunction<TerrainFixture> constructor, final int id) throws SQLException, IOException {
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
	public void testVillageSerialization(final TownStatus status, final int id, final String race) throws SQLException, IOException {
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
	public void testNotesSerialization(final int id, final int player, final String note) throws SQLException, IOException {
		final Worker worker = new Worker("test worker", "human", id);
		final Player playerObj = new PlayerImpl(player, "");
		worker.setNote(playerObj, note);
		final Unit unit = new Unit(playerObj, "unitKind", "unitName", id + 1);
		unit.addMember(worker);
		final IUnit deserializedUnit = assertFixtureSerialization(unit);
		assertEquals(note, ((IWorker) deserializedUnit.iterator().next()).getNote(playerObj),
			"Note was deserialized");
	}

	@Test
	public void testBookmarkSerialization() throws SQLException, IOException {
		final IMutableMapNG map = new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 1);
		final Player player = map.getPlayers().getPlayer(1);
		map.setCurrentPlayer(player);
		assertFalse(map.getBookmarks().contains(new Point(0, 0)),
			"Map by default doesn't have a bookmark");
		assertEquals(0, map.getAllBookmarks(new Point(0, 0)).size(),
			"Map by default has no bookmarks");
		map.addBookmark(new Point(0, 0));
		final IMutableMapNG deserialized = (IMutableMapNG) assertDatabaseSerialization(map);
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
		assumeFalse(directionOne == directionTwo,  "We can't have the same direction twice");
		assumeTrue(qualityOne >= 0 && qualityTwo >= 0, "Road quality must be nonnegative");
		final IMutableMapNG map = new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 1);
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
