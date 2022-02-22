package impl.dbio;

import buckelieg.jdbc.fn.DB;

import common.map.IFixture;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.Ground;
import common.xmlio.Warning;

final class DBGroundHandler extends AbstractDatabaseWriter<Ground, Point> implements MapContentsReader {
	public DBGroundHandler() {
		super(Ground.class, Point.class);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS ground (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(32) NOT NULL," +
			"    exposed BOOLEAN NOT NULL," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO ground (row, column, id, kind, exposed, image) VALUES(?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final Ground obj, final Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(), obj.getKind(),
			obj.isExposed(), obj.getImage());
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readGround(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final Boolean exposed = /* DBMapReader.databaseBoolean(dbRow.get("exposed")) */ // FIXME
				(Boolean) dbRow.get("exposed"); // This will compile, but probably won't work
			final String image = (String) dbRow.get("image");
			final Ground ground = new Ground(id, kind, exposed);
			if (image != null) {
				ground.setImage(image);
			}
			map.addFixture(new Point(row, column), ground);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) {
		try {
			handleQueryResults(db, warner, "ground", readGround(map),
				"SELECT * FROM ground");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
