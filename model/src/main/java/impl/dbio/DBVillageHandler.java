package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import java.sql.Types;

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

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
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
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO villages (row, column, status, name, id, owner, race, " +
			"    image, portrait, population) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private static final AbstractDatabaseWriter<CommunityStats, ITownFixture> CS_WRITER =
		new DBCommunityStatsHandler();

	@Override
	public void write(DB db, Village obj, Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getStatus().toString(),
			obj.getName(), obj.getId(), obj.getOwner().getPlayerId(), obj.getRace(),
			obj.getImage(), obj.getPortrait(),
			Optional.ofNullable(obj.getPopulation()).map(CommunityStats::getPopulation)
				.orElse(null)).execute();
		CommunityStats stats = obj.getPopulation();
		if (stats != null) {
			CS_WRITER.initialize(db);
			CS_WRITER.write(db, stats, obj);
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readVillage(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			TownStatus status = TownStatus.parse((String) dbRow.get("status"));
			String name = (String) dbRow.get("name");
			int id = (Integer) dbRow.get("id");
			int ownerId = (Integer) dbRow.get("owner");
			String race = (String) dbRow.get("race");
			String image = (String) dbRow.get("image");
			String portrait = (String) dbRow.get("portrait");
			Integer population = (Integer) dbRow.get("population");
			Village village = new Village(status, name, id, map.getPlayers().getPlayer(ownerId),
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
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "villages", readVillage(map),
				"SELECT * from villages");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
