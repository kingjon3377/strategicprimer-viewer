package impl.dbio;

import common.map.TileType;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import common.map.IMutableMapNG;
import common.map.IMapNG;
import common.map.River;
import common.map.Player;
import common.map.Direction;
import common.map.TileFixture;
import common.map.Point;
import lovelace.util.LovelaceLogger;

import static io.jenetics.facilejdbc.Param.value;

final class DBMapWriter extends AbstractDatabaseWriter<IMutableMapNG, IMapNG> {
	public DBMapWriter(final SPDatabaseWriter parent, final AbstractDatabaseWriter<Player, IMapNG> playerWriter) {
		super(IMutableMapNG.class, IMapNG.class);
		this.parent = parent;
		this.playerWriter = playerWriter;
	}

	/**
	 * What we use to write members. Called "parent" because *it* actually owns *this* object.
	 */
	private final SPDatabaseWriter parent;

	public static int currentTurn = -1; // TODO: Make private and provide accessor---other classes don't need to write, right?

	private static final List<Query> INITIALIZERS = List.of(
			Query.of("CREATE TABLE IF NOT EXISTS metadata (" +
					         "    version INTEGER NOT NULL," +
					         "    rows INTEGER NOT NULL," +
					         "    columns INTEGER NOT NULL," +
					         "    current_turn INTEGER NOT NULL" +
					         ");"),
			Query.of("CREATE TABLE IF NOT EXISTS terrain (" +
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
					         ");"),
			Query.of("CREATE TABLE IF NOT EXISTS bookmarks (" +
					         "    row INTEGER NOT NULL," +
					         "    column INTEGER NOT NULL," +
					         "    player INTEGER NOT NULL" +
					         ");"),
			Query.of("CREATE TABLE IF NOT EXISTS roads (" +
					         "    row INTEGER NOT NULL," +
					         "    column INTEGER NOT NULL," +
					         "    direction VARCHAR(9) NOT NULL" +
					         "        CHECK (direction IN ('north', 'south', 'east', 'west', 'northeast'," +
					         "            'southeast', 'northwest', 'southwest'))," +
					         "    quality INTEGER NOT NULL" +
					         ");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private final AbstractDatabaseWriter<Player, IMapNG> playerWriter;

	private static final Query INSERT_TERRAIN =
		Query.of("INSERT INTO terrain (row, column, terrain, mountainous, north_river, " +
			"   south_river, east_river, west_river, lake) " +
			"VALUES(:row, :column, :terrain, :mountain, :north, :south, :east, :west, :lake);");

	private static final Query INSERT_BOOKMARK =
		Query.of("INSERT INTO bookmarks (row, column, player) VALUES(:row, :column, :player)");

	private static final Query INSERT_ROADS =
		Query.of("INSERT INTO roads (row, column, direction, quality) VALUES(:row, :column, :direction, :quality)");

	private static final Query INSERT_METADATA = Query.of(
			"INSERT INTO metadata (version, rows, columns, current_turn) VALUES(:version, :rows, :columns, :turn);");

	@Override
	public void write(final Transactional db, final IMutableMapNG obj, final IMapNG context) throws SQLException {
		final Connection conn = db.connection(); // TODO: work inside a transaction instead, surely?
		INSERT_METADATA.on(value("version", obj.getDimensions().version()),
				value("rows", obj.getDimensions().rows()),
				value("columns", obj.getDimensions().columns()),
				value("turn", obj.getCurrentTurn())).execute(conn);
		currentTurn = obj.getCurrentTurn(); // TODO: move up to reuse it in previous insertion query
		playerWriter.initialize(db);
		for (final Player player : obj.getPlayers()) {
			playerWriter.write(db, player, obj);
		}
		int count = 0;
		int fixtureCount = 0;
		for (final Point location : obj.getLocations()) {
			final Collection<River> rivers = obj.getRivers(location);
			INSERT_TERRAIN.on(value("row", location.row()), value("column", location.column()),
					value("terrain", Optional.ofNullable(obj.getBaseTerrain(location))
							.map(TileType::getXml).orElse("")),
					value("mountain", obj.isMountainous(location)),
					value("north", rivers.contains(River.North)),
					value("south", rivers.contains(River.South)),
					value("east", rivers.contains(River.East)),
					value("west", rivers.contains(River.West)),
					value("lake", rivers.contains(River.Lake))).execute(conn);
			for (final TileFixture fixture : obj.getFixtures(location)) {
				parent.writeSPObjectInContext(db, fixture, location);
				fixtureCount++;
			}
			for (final Player player : obj.getAllBookmarks(location)) {
				INSERT_BOOKMARK.on(value("row", location.row()), value("column", location.column()),
						value("player", player.getPlayerId())).execute(conn);
			}
			for (final Map.Entry<Direction, Integer> entry : obj.getRoads(location).entrySet()) {
				INSERT_ROADS.on(value("row", location.row()), value("column", location.column()),
						value("direction", entry.getKey().toString()),
						value("quality", entry.getValue())).execute(conn);
			}
			count++;
			if (count % 25 == 0) {
				LovelaceLogger.debug("Wrote %d points with %d fixtures so far",
					count, fixtureCount);
			}
		}
	}
}
