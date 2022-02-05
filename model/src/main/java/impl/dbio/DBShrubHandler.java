package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;

import common.map.Point;
import common.map.IMutableMapNG;

import common.map.fixtures.resources.Shrub;
import common.xmlio.Warning;

final class DBShrubHandler extends AbstractDatabaseWriter<Shrub, Point> implements MapContentsReader {
	public DBShrubHandler() {
		super(Shrub.class, Point.class);
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
		"CREATE TABLE IF NOT EXISTS shrubs (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(64) NOT NULL," +
			"    count INTEGER," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO shrubs (row, column, id, kind, count, image) VALUES(?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final Shrub obj, final Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(), obj.getKind(),
			obj.getPopulation(), obj.getImage()).execute();
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readShrub(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int id = (Integer) dbRow.get("id");
			String kind = (String) dbRow.get("kind");
			Integer count = (Integer) dbRow.get("count");
			String image = (String) dbRow.get("image");
			Shrub shrub = new Shrub(kind, id, count == null ? -1 : count);
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
