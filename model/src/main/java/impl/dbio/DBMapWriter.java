package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Collection;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;

import common.map.IMutableMapNG;
import common.map.IMapNG;
import common.map.River;
import common.map.Player;
import common.map.TileType;
import common.map.Direction;
import common.map.TileFixture;
import common.map.Point;

final class DBMapWriter extends AbstractDatabaseWriter<IMutableMapNG, IMapNG> {
	public DBMapWriter(SPDatabaseWriter parent, AbstractDatabaseWriter<Player, IMapNG> playerWriter) {
		super(IMutableMapNG.class, IMapNG.class);
		this.parent = parent;
		this.playerWriter = playerWriter;
	}

	/**
	 * What we use to write members. Called "parent" because *it* actually owns *this* object.
	 */
	private final SPDatabaseWriter parent;

	public static int currentTurn = -1; // TODO: Make private and provide accessor---other classes don't need to write, right?

	private static final Iterable<String> INITIALIZERS = Collections.unmodifiableList(Arrays.asList(
		"CREATE TABLE IF NOT EXISTS metadata (" +
			"    version INTEGER NOT NULL," +
			"    rows INTEGER NOT NULL," +
			"    columns INTEGER NOT NULL," +
			"    current_turn INTEGER NOT NULL" +
			");",
		"CREATE TABLE IF NOT EXISTS terrain (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    terrain VARCHAR(16) NOT NULL" +
			"        CHECK (terrain IN ('', 'tundra', 'desert', 'mountain', 'boreal_forest'," +
			"            'temperate_forest', 'ocean', 'plains', 'jungle', 'steppe', 'swamp'))," +
			"    mountainous BOOLEAN NOT NULL," +
			"    north_river BOOLEAN NOT NULL," +
			"    south_river BOOLEAN NOT NULL," +
			"    east_river BOOLEAN NOT NULL," +
			"    west_river BOOLEAN NOT NULL," +
			"    lake BOOLEAN NOT NULL" +
			");",
		"CREATE TABLE IF NOT EXISTS bookmarks (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    player INTEGER NOT NULL" +
			");",
		"CREATE TABLE IF NOT EXISTS roads (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    direction VARCHAR(9) NOT NULL" +
			"        CHECK (direction IN ('north', 'south', 'east', 'west', 'northeast'," +
			"            'southeast', 'northwest', 'southwest'))," +
			"    quality INTEGER NOT NULL" +
			")"));

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private AbstractDatabaseWriter<Player, IMapNG> playerWriter;

	private static final String INSERT_TERRAIN =
		"INSERT INTO terrain (row, column, terrain, mountainous, north_river, " +
			"   south_river, east_river, west_river, lake) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private static final String INSERT_BOOKMARK =
		"INSERT INTO bookmarks (row, column, player) VALUES(?, ?, ?)";

	private static final String INSERT_ROADS = 
		"INSERT INTO roads (row, column, direction, quality) VALUES(?, ?, ?, ?)";

	@Override
	public void write(DB db, IMutableMapNG obj, IMapNG context) {
		db.update("INSERT INTO metadata (version, rows, columns, current_turn) VALUES(?, ?, ?, ?)",
			obj.getDimensions().getVersion(), obj.getDimensions().getRows(),
			obj.getDimensions().getColumns(), obj.getCurrentTurn()).execute();
		currentTurn = obj.getCurrentTurn();
		playerWriter.initialize(db);
		for (Player player : obj.getPlayers()) {
			playerWriter.write(db, player, obj);
		}
		int count = 0;
		int fixtureCount = 0;
		for (Point location : obj.getLocations()) {
			Collection<River> rivers = obj.getRivers(location);
			db.update(INSERT_TERRAIN, location.getRow(), location.getColumn(),
				Optional.ofNullable(obj.getBaseTerrain(location))
					.map(t -> t.getXml()).orElse(""),
				obj.isMountainous(location), rivers.contains(River.North),
				rivers.contains(River.South), rivers.contains(River.East),
				rivers.contains(River.West), rivers.contains(River.Lake)).execute();
			for (TileFixture fixture : obj.getFixtures(location)) {
				parent.writeSPObjectInContext(db, fixture, location);
				fixtureCount++;
			}
			for (Player player : obj.getAllBookmarks(location)) {
				db.update(INSERT_BOOKMARK, location.getRow(), location.getColumn(),
					player.getPlayerId()).execute();
			}
			for (Map.Entry<Direction, Integer> entry : obj.getRoads(location).entrySet()) {
				db.update(INSERT_ROADS, location.getRow(), location.getColumn(),
					entry.getKey().toString(), entry.getValue()).execute();
			}
			count++;
			if (count % 25 == 0) {
				log.fine(String.format("Wrote %d points with %d fixtures so far",
					count, fixtureCount));
			}
		}
	}
}
