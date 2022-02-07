package impl.dbio;

import buckelieg.jdbc.fn.DB;

import common.map.IMutableMapNG;
import common.map.SPMapNG;
import common.map.MapDimensionsImpl;
import common.map.PlayerImpl;
import common.map.MutablePlayer;
import common.map.Point;
import common.map.TileType;
import common.map.River;
import common.map.IMutablePlayerCollection;
import common.map.PlayerCollection;
import common.map.Direction;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MapVersionException;

import java.util.Collections;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import java.text.ParseException;

import lovelace.util.Accumulator;
import lovelace.util.IntAccumulator;

final class DBMapReader {
	private static final Logger LOGGER = Logger.getLogger(DBMapReader.class.getName());
	// FIXME: Passing null when we don't want to construct the parent object is a *really* bad idea!
	private final List<MapContentsReader> readers = Collections.unmodifiableList(Arrays.asList(
		new DBPlayerHandler(), new DBCacheHandler(), new DBExplorableHandler(),
		new DBFieldHandler(), new DBFortressHandler(null), new DBUnitHandler(null),
		new DBGroundHandler(), new DBGroveHandler(), new DBImmortalHandler(),
		new DBImplementHandler(), new DBMineralHandler(), new DBMineHandler(),
		new DBPortalHandler(), new DBShrubHandler(), new DBSimpleTerrainHandler(),
		new DBTextHandler(), new DBTownHandler(), new DBVillageHandler(),
		new DBResourcePileHandler(), new DBAnimalHandler(), new DBCommunityStatsHandler(),
		new DBWorkerHandler(), new DBAdventureHandler(), new DBForestHandler()));

	/**
	 * If {@link field} is is an Integer and either 0 or 1, which is how
	 * SQLite stores Boolean values, convert to the equivalent Boolean and
	 * return that; otherwise, return the original value.
	 */
	public Object databaseBoolean(final Object field) {
		if (field instanceof Integer) {
			if ((Integer) field == 0) {
				return false;
			} else if ((Integer) field == 1) {
				return true;
			} else {
				return field;
			}
		} else {
			return field;
		}
	}

	public IMutableMapNG readMap(final DB db, final Warning warner) {
		// FIXME: Better exception
		Map<String, Object> metadata = db.select(
				"SELECT version, rows, columns, current_turn FROM metadata LIMIT 1")
			.single().orElseThrow(() -> new IllegalStateException("No metadata in database"));
		int version = (Integer) metadata.get("version");
		int rows = (Integer) metadata.get("rows");
		int columns = (Integer) metadata.get("columns");
		int turn = (Integer) metadata.get("current_turn");
		if (version != 2) {
			warner.handle(MapVersionException.nonXML(version, 2, 2));
		}
		IMutablePlayerCollection players = new PlayerCollection();
		LOGGER.fine("About to read players");
		// TODO: Here and elsewhere, adapt to the 'lambda to filter' model this library
		// assumes and encourages instead of doing *everything* in the "consumer".
		db.select("SELECT id, codename, current FROM players").execute().forEach(row -> {
				int id = (Integer) row.get("id");
				String codename = (String) row.get("codename");
				boolean current = (Boolean) databaseBoolean(row.get("current"));
				MutablePlayer player = new PlayerImpl(id, codename);
				if (current) {
					player.setCurrent(true);
				}
				players.add(player);
			});
		LOGGER.fine("Finished reading players, about to start on terrain");
		IMutableMapNG retval =
			new SPMapNG(new MapDimensionsImpl(rows, columns, version), players, turn);
		Accumulator<Integer> count = new IntAccumulator(0);
		db.select("SELECT * FROM terrain").execute().forEach(dbRow -> {
				int row = (Integer) dbRow.get("row");
				int column = (Integer) dbRow.get("column");
				String terrainString = (String) dbRow.get("terrain");
				boolean mtn = (Boolean) databaseBoolean(dbRow.get("mountainous"));
				boolean northR = (Boolean) databaseBoolean(dbRow.get("north_river"));
				boolean southR = (Boolean) databaseBoolean(dbRow.get("south_river"));
				boolean eastR = (Boolean) databaseBoolean(dbRow.get("east_river"));
				boolean westR = (Boolean) databaseBoolean(dbRow.get("west_river"));
				boolean lake = (Boolean) databaseBoolean(dbRow.get("lake"));
				Point location = new Point(row, column);
				if (terrainString != null && !terrainString.isEmpty()) {
					try {
						retval.setBaseTerrain(location, TileType.parse(terrainString));
					} catch (final ParseException except) {
						throw new IllegalArgumentException(except);
					}
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
					LOGGER.fine(String.format("Read terrain for %d tiles",
						count.getSum()));
				}
			});
		db.select("SELECT * FROM roads").execute().forEach(dbRow -> {
				int row = (Integer) dbRow.get("row");
				int column = (Integer) dbRow.get("column");
				Direction direction = Direction.parse((String) dbRow.get("direction"));
				int quality = (Integer) dbRow.get("quality");
				assert direction != null;
				retval.setRoadLevel(new Point(row, column), direction, quality);
			});
		LOGGER.fine("Finished reading terrain");
		db.select("SELECT * FROM bookmarks").execute().forEach(dbRow -> {
				int row = (Integer) dbRow.get("row");
				int column = (Integer) dbRow.get("column");
				int playerNum = (Integer) dbRow.get("player");
				retval.addBookmark(new Point(row, column), players.getPlayer(playerNum));
			});
		for (MapContentsReader reader : readers) {
			try {
				reader.readMapContents(db, retval, warner);
			} catch (final Exception exception) {
				if (exception.getMessage().contains("no such table")) {
					continue;
				} else {
					// TODO: declare checked exception instead?
					throw new RuntimeException(exception);
				}
			}
		}
		for (MapContentsReader reader : readers) {
			try {
				reader.readExtraMapContents(db, retval, warner);
			} catch (final Exception exception) {
				if (exception.getMessage().contains("no such table")) {
					continue;
				} else if (exception instanceof RuntimeException) {
					throw (RuntimeException) exception;
				} else {
					// TODO: declare checked exception instead?
					throw new RuntimeException(exception);
				}
			}
		}
		LOGGER.fine("Finished reading the map");
		return retval;
	}
}
