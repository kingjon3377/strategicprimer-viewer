package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;

import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.CommunityStats;
import common.xmlio.Warning;
import common.map.fixtures.towns.ITownFixture;

final class DBVillageHandler extends AbstractDatabaseWriter<Village, Point> implements MapContentsReader {
	public DBVillageHandler() {
		super(Village.class, Point.class);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS villages (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    status VARCHAR(9) NOT NULL" +
			"        CHECK(status IN ('abandoned', 'active', 'burned', 'ruined'))," +
			"    name VARCHAR(128) NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    owner INTEGER NOT NULL," +
			"    race VARCHAR(32) NOT NULL," +
			"    image VARCHAR(255)," +
			"    portrait VARCHAR(255)," +
			"    population INTEGER" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO villages (row, column, status, name, id, owner, race, " +
			"    image, portrait, population) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private static final AbstractDatabaseWriter<CommunityStats, ITownFixture> CS_WRITER =
		new DBCommunityStatsHandler();

	@Override
	public void write(final DB db, final Village obj, final Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getStatus().toString(),
			obj.getName(), obj.getId(), obj.getOwner().getPlayerId(), obj.getRace(),
			obj.getImage(), obj.getPortrait(),
			Optional.ofNullable(obj.getPopulation()).map(CommunityStats::getPopulation)
				.orElse(null)).execute();
		final CommunityStats stats = obj.getPopulation();
		if (stats != null) {
			CS_WRITER.initialize(db);
			CS_WRITER.write(db, stats, obj);
		}
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readVillage(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final TownStatus status = TownStatus.parse((String) dbRow.get("status"));
			final String name = (String) dbRow.get("name");
			final int id = (Integer) dbRow.get("id");
			final int ownerId = (Integer) dbRow.get("owner");
			final String race = (String) dbRow.get("race");
			final String image = (String) dbRow.get("image");
			final String portrait = (String) dbRow.get("portrait");
			final Integer population = (Integer) dbRow.get("population");
			final Village village = new Village(status, name, id, map.getPlayers().getPlayer(ownerId),
				race);
			if (image != null) {
				village.setImage(image);
			}
			if (portrait != null) {
				village.setPortrait(portrait);
			}
			if (population != null) {
				village.setPopulation(new CommunityStats(population));
			}
			map.addFixture(new Point(row, column), village);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "villages", readVillage(map),
				"SELECT * from villages");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
