package impl.dbio;

import buckelieg.jdbc.fn.DB;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.explorable.AdventureFixture;
import common.xmlio.Warning;

import java.util.Collections;
import java.util.List;
import java.util.Map;

final class DBAdventureHandler extends AbstractDatabaseWriter<AdventureFixture, Point>
		implements MapContentsReader {
	public DBAdventureHandler() {
		super(AdventureFixture.class, Point.class);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS adventures (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    brief VARCHAR(255) NOT NULL," +
			"    full VARCHAR(512) NOT NULL," +
			"    owner INTEGER NOT NULL," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_QUERY =
		"INSERT INTO adventures (row, column, id, brief, full, owner, image) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final AdventureFixture obj, final Point context) {
		db.update(INSERT_QUERY, context.getRow(), context.getColumn(), obj.getId(),
			obj.getBriefDescription(), obj.getFullDescription(), obj.getOwner().getPlayerId(),
			obj.getImage()).execute();
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception>
			readAdventure(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
//			int row = Integer.parseInt(dbRow.get("row"));
			int column = (Integer) dbRow.get("column");
//			int column = Integer.parseInt(dbRow.get("column"));
			int id = (Integer) dbRow.get("id");
//			int id = Integer.parseInt(dbRow.get("id"));
			String brief = (String) dbRow.get("brief");
			String full = (String) dbRow.get("full");
			int ownerId = (Integer) dbRow.get("owner");
//			int ownerId = Integer.parseInt(dbRow.get("owner"));
			String image = (String) dbRow.get("image");
			AdventureFixture adventure = new AdventureFixture(map.getPlayers().getPlayer(ownerId),
				brief, full, id);
			if (image != null) {
				adventure.setImage(image);
			}
			map.addFixture(new Point(row, column), adventure);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "adventures", readAdventure(map),
				"SELECT * FROM adventures");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
