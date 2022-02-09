package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.towns.TownStatus;
import common.xmlio.Warning;

final class DBMineHandler extends AbstractDatabaseWriter<Mine, Point> implements MapContentsReader {
	public DBMineHandler() {
		super(Mine.class, Point.class);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS mines (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(128) NOT NULL," +
			"    status VARCHAR(9) NOT NULL" +
			"        CHECK(status IN ('abandoned', 'active', 'burned', 'ruined'))," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO mines (row, column, id, kind, status, image) VALUES(?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final Mine obj, final Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(), obj.getKind(),
			obj.getStatus().toString(), obj.getImage());
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readMine(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final TownStatus status = TownStatus.parse((String) dbRow.get("status"));
			final String image = (String) dbRow.get("image");
			final Mine mine = new Mine(kind, status, id);
			if (image != null) {
				mine.setImage(image);
			}
			map.addFixture(new Point(row, column), mine);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "mines", readMine(map),
				"SELECT * FROM mines");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
