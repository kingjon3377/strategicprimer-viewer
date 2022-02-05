package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Map;

import java.math.BigDecimal;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.terrain.Forest;
import common.xmlio.Warning;

final class DBForestHandler extends AbstractDatabaseWriter<Forest, Point> implements MapContentsReader {
	public DBForestHandler() {
		super(Forest.class, Point.class);
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
		"CREATE TABLE IF NOT EXISTS forests (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(32) NOT NULL," +
			"    rows BOOLEAN NOT NULL," +
			"    acres VARCHAR(128)" +
			"        CHECK (acres NOT LIKE '%[^0-9.]%' AND acres NOT LIKE '%.%.%')," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO forests(row, column, id, kind, rows, acres, image) VALUES(?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final Forest obj, final Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(), obj.getKind(),
			obj.isRows(), obj.getAcres().toString(), obj.getImage()).execute();
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readForest(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int id = (Integer) dbRow.get("id");
			String kind = (String) dbRow.get("kind");
			Boolean rows = /* DBMapReader.databaseBoolean(dbRow.get("rows")) */ // FIXME
				(Boolean) dbRow.get("rows"); // This will compile, but probably won't work
			String acresString = (String) dbRow.get("acres");
			String image = (String) dbRow.get("image");
			Number acres;
			try {
				acres = Integer.parseInt(acresString);
			} catch (final NumberFormatException except) {
				acres = new BigDecimal(acresString);
			}
			Forest forest = new Forest(kind, rows, id, acres);
			if (image != null) {
				forest.setImage(image);
			}
			map.addFixture(new Point(row, column), forest);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "forests", readForest(map),
				"SELECT * FROM forests");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
