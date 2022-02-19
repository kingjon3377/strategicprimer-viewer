package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;

import common.map.fixtures.resources.Shrub;
import common.xmlio.Warning;

final class DBShrubHandler extends AbstractDatabaseWriter<Shrub, Point> implements MapContentsReader {
	public DBShrubHandler() {
		super(Shrub.class, Point.class);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS shrubs (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(64) NOT NULL," +
			"    count INTEGER," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO shrubs (row, column, id, kind, count, image) VALUES(?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final Shrub obj, final Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(), obj.getKind(),
			obj.getPopulation(), obj.getImage()).execute();
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readShrub(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final Integer count = (Integer) dbRow.get("count");
			final String image = (String) dbRow.get("image");
			final Shrub shrub = new Shrub(kind, id, count == null ? -1 : count);
			if (image != null) {
				shrub.setImage(image);
			}
			map.addFixture(new Point(row, column), shrub);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "shrubs", readShrub(map),
				"SELECT * FROM shrubs");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
