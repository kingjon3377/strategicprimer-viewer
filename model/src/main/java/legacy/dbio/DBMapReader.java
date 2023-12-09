package legacy.dbio;

import legacy.map.IFixture;
import legacy.map.IMutableLegacyMap;
import common.map.Player;
import legacy.map.LegacyMap;
import legacy.map.MapDimensionsImpl;
import common.map.PlayerImpl;
import common.map.MutablePlayer;
import legacy.map.Point;
import legacy.map.TileType;
import legacy.map.River;
import common.map.IMutablePlayerCollection;
import common.map.PlayerCollection;
import legacy.map.Direction;
import legacy.map.fixtures.FortressMember;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IMutableWorker;
import legacy.map.fixtures.towns.AbstractTown;
import legacy.map.fixtures.towns.CommunityStats;
import legacy.map.fixtures.towns.IMutableFortress;
import legacy.map.fixtures.towns.Village;
import common.xmlio.Warning;
import impl.dbio.MapContentsReader;
import impl.xmlio.exceptions.MapVersionException;

import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Row;
import io.jenetics.facilejdbc.RowParser;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.text.ParseException;

import java.util.stream.Stream;

import lovelace.util.Accumulator;
import lovelace.util.IntAccumulator;
import lovelace.util.LovelaceLogger;
import org.eclipse.jdt.annotation.Nullable;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Sextet;
import org.javatuples.Triplet;

public final class DBMapReader {
	// FIXME: Passing null when we don't want to construct the parent object is a *really* bad idea!
	private final List<MapContentsReader> readers = List.of(new DBPlayerHandler(), new DBCacheHandler(), new DBExplorableHandler(), new DBFieldHandler(), new DBFortressHandler(null), new DBUnitHandler(null), new DBGroundHandler(), new DBGroveHandler(), new DBImmortalHandler(), new DBImplementHandler(), new DBMineralHandler(), new DBMineHandler(), new DBPortalHandler(), new DBShrubHandler(), new DBSimpleTerrainHandler(), new DBTextHandler(), new DBTownHandler(), new DBVillageHandler(), new DBResourcePileHandler(), new DBAnimalHandler(), new DBCommunityStatsHandler(), new DBWorkerHandler(), new DBAdventureHandler(), new DBForestHandler());

	private final Map<Integer, IFixture> containers = new HashMap<>();
	private final Map<Integer, List<Object>> containees = new HashMap<>();

	private static final Query PLAYER_SELECT = Query.of("SELECT id, codename, current FROM players");

	private static Player parsePlayer(final Row row, final Connection sql) throws SQLException {
		final MutablePlayer retval = new PlayerImpl(row.getInt("id"), row.getString("codename"));
		if (row.getBoolean("current")) {
			retval.setCurrent(true);
		}
		return retval;
	}

	private static final Query METADATA_SELECT = Query.of("SELECT version, rows, columns, current_turn FROM metadata LIMIT 1");

	// FIXME: Define DTOs for metadata and terrain records, rather than using tuples.
	private static Quartet<Integer, Integer, Integer, Integer> parseMetadata(final Row row, final Connection sql) throws SQLException {
		return Quartet.with(row.getInt("version"), row.getInt("rows"), row.getInt("columns"), row.getInt("current_turn"));
	}

	private static @Nullable TileType parseTileType(final @Nullable String string) {
		if (string == null || string.isEmpty()) {
			return null;
		}
		try {
			return TileType.parse(string);
		} catch (final ParseException except) {
			throw new IllegalArgumentException(except);
		}
	}

	private static final Query TERRAIN_SELECT = Query.of("SELECT * FROM terrain");

	private static Triplet<Point, @Nullable TileType, Sextet<Boolean, Boolean, Boolean, Boolean, Boolean, Boolean>> parseTerrain(final Row row, final Connection sql) throws SQLException {
		return Triplet.with(new Point(row.getInt("row"), row.getInt("column")), parseTileType(row.getString("terrain")),
			Sextet.with(row.getBoolean("mountainous"), row.getBoolean("north_river"),
				row.getBoolean("south_river"), row.getBoolean("east_river"), row.getBoolean("west_river"),
				row.getBoolean("lake")));
	}

	private static final Query ROAD_SELECT = Query.of("SELECT * FROM roads");

	private static Triplet<Point, Direction, Integer> parseRoads(final Row row, final Connection sql) throws SQLException {
		return Triplet.with(new Point(row.getInt("row"), row.getInt("column")),
			Objects.requireNonNull(Direction.parse(row.getString("direction"))), row.getInt("quality"));
	}

	private static final Query BOOKMARK_SELECT = Query.of("SELECT * FROM BOOKMARKS");

	private static Pair<Point, Integer> parseBookmark(final Row row, final Connection sql) throws SQLException {
		return Pair.with(new Point(row.getInt("row"), row.getInt("column")), row.getInt("player"));
	}

	public IMutableLegacyMap readMap(final Transactional db, final Warning warner) throws SQLException {
		final Connection conn = db.connection();
		final @Nullable Quartet<Integer, Integer, Integer, Integer> metadata = METADATA_SELECT.as(((RowParser<Quartet<Integer, Integer, Integer, Integer>>) DBMapReader::parseMetadata).singleNull(), conn);
		if (metadata == null) {
			throw new IllegalStateException("No metadata in database");
		}
		final int version = metadata.getValue0();
		final int rows = metadata.getValue1();
		final int columns = metadata.getValue2();
		final int turn = metadata.getValue3();
		if (version != 2) {
			warner.handle(MapVersionException.nonXML(version, 2, 2));
		}
		final IMutablePlayerCollection players = new PlayerCollection();
		LovelaceLogger.debug("About to read players");
		// TODO: Players should have 'country' and 'portrait' flags as well
		try (final Stream<Player> playerStream = PLAYER_SELECT.as(((RowParser<Player>) DBMapReader::parsePlayer).stream(), conn)) {
			playerStream.forEach(players::add);
		}
		LovelaceLogger.debug("Finished reading players, about to start on terrain");
		final IMutableLegacyMap retval =
			new LegacyMap(new MapDimensionsImpl(rows, columns, version), players, turn);
		final Accumulator<Integer> count = new IntAccumulator(0);
		try (final Stream<Triplet<Point, @Nullable TileType, Sextet<Boolean, Boolean, Boolean, Boolean, Boolean, Boolean>>> terrainStream =
				 TERRAIN_SELECT.as(((RowParser<Triplet<Point, @Nullable TileType, Sextet<Boolean, Boolean, Boolean, Boolean, Boolean, Boolean>>>)
					 DBMapReader::parseTerrain).stream(), conn)) {
			terrainStream.forEach(dbRow -> {
				final Sextet<Boolean, Boolean, Boolean, Boolean, Boolean, Boolean> tuple = dbRow.getValue2();
				final boolean mtn = tuple.getValue0();
				final boolean northR = tuple.getValue1();
				final boolean southR = tuple.getValue2();
				final boolean eastR = tuple.getValue3();
				final boolean westR = tuple.getValue4();
				final boolean lake = tuple.getValue5();
				final Point location = dbRow.getValue0();
				final @Nullable TileType tileType = dbRow.getValue1();
				if (tileType != null) {
					retval.setBaseTerrain(location, tileType);
				}
				retval.setMountainous(location, mtn);
				if (northR) {
					retval.addRivers(location, River.North);
				}
				if (southR) {
					retval.addRivers(location, River.South);
				}
				if (eastR) {
					retval.addRivers(location, River.East);
				}
				if (westR) {
					retval.addRivers(location, River.West);
				}
				if (lake) {
					retval.addRivers(location, River.Lake);
				}
				count.add(1);
				if (count.getSum() % 50 == 0) {
					LovelaceLogger.debug("Read terrain for %d tiles",
						count.getSum());
				}
			});
		}
		try (final Stream<Triplet<Point, Direction, Integer>> rStream = ROAD_SELECT.as(((RowParser<Triplet<Point, Direction, Integer>>) DBMapReader::parseRoads).stream(), conn)) {
			rStream.forEach(t -> retval.setRoadLevel(t.getValue0(), t.getValue1(), t.getValue2()));
		}
		LovelaceLogger.debug("Finished reading terrain");
		try (final Stream<Pair<Point, Integer>> bStream = BOOKMARK_SELECT.as(((RowParser<Pair<Point, Integer>>) DBMapReader::parseBookmark).stream(), conn)) {
			bStream.forEach(p -> retval.addBookmark(p.getValue0(), players.getPlayer(p.getValue1())));
		}
		for (final MapContentsReader reader : readers) {
			try {
				reader.readMapContents(conn, retval, containers, containees, warner);
			} catch (final RuntimeException | SQLException exception) {
				if (exception.getMessage().contains("no such table")) {
					continue;
				} else {
					throw exception;
				}
			} catch (final Exception exception) {
				if (exception.getMessage().contains("no such table")) {
					continue;
				} else {
					// TODO: declare checked exception instead?
					throw new RuntimeException(exception);
				}
			}
		}
		LovelaceLogger.debug("Finished reading the map except adding members to parents");

		for (final Map.Entry<Integer, List<Object>> entry : containees.entrySet()) {
			final int parentId = entry.getKey();
			for (final Object member : entry.getValue()) {
				final IFixture parent = containers.get(parentId);
				if (!containers.containsKey(parentId)) {
					throw new IllegalArgumentException("No parent for " + member);
				} else if (parent == null) {
					throw new IllegalArgumentException("Null parent");
				} else if (member == null) {
					throw new IllegalArgumentException("Null member");
				} else if (parent instanceof final IMutableFortress p && member instanceof final FortressMember m) {
					p.addMember(m);
				} else if (parent instanceof final IMutableUnit p && member instanceof final UnitMember m) {
					p.addMember(m);
				} else if (parent instanceof final AbstractTown p && member instanceof final CommunityStats m &&
					p.getPopulation() == null) {
					p.setPopulation(m);
				} else if (parent instanceof final Village p && member instanceof final CommunityStats m &&
					p.getPopulation() == null) {
					p.setPopulation(m);
				} else if (parent instanceof final IMutableWorker p && member instanceof final Animal m &&
					p.getMount() == null) {
					p.setMount(m);
				} else if (parent instanceof final IMutableWorker p && member instanceof final Implement m) {
					p.addEquipment(m);
				} else if (parent instanceof final AbstractTown p && member instanceof CommunityStats && // TODO: combine with earlier AbstractTown case?
					p.getPopulation() != null) {
					throw new IllegalStateException("Community stats already set");
				} else {
					throw new IllegalStateException(String.format("DB parent-child type invariants not met (parent %s, child %s)",
						parent.getClass().getSimpleName(), member.getClass().getSimpleName()));
				}
			}
		}
		retval.setModified(false);
		LovelaceLogger.debug("Finished adding members to parents");
		return retval;
	}

	// TODO: Find a way to restrict access, or to make the two collections have their lifespan tied to a particular database
	public void clearCache() {
		containers.clear();
		containees.clear();
	}
}
