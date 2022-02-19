package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.resources.Grove;
import common.xmlio.Warning;

final class DBGroveHandler extends AbstractDatabaseWriter<Grove, Point> implements MapContentsReader {
	public DBGroveHandler() {
		super(Grove.class, Point.class);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS groves (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    type VARCHAR(7) NOT NULL" +
			"        CHECK (type IN ('grove', 'orchard'))," +
			"    kind VARCHAR(64) NOT NULL," +
			"    cultivated BOOLEAN NOT NULL," +
			"    count INTEGER NOT NULL," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO groves (row, column, id, type, kind, cultivated, count, image) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final Grove obj, final Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(),
			(obj.isOrchard()) ? "orchard" : "grove", obj.getKind(), obj.isCultivated(),
			obj.getPopulation(), obj.getImage());
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readGrove(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String type = (String) dbRow.get("type");
			final String kind = (String) dbRow.get("kind");
			final Boolean cultivated = /*DBMapReader.databaseBoolean(dbRow.get("cultivated"))*/ //FIXME
				(Boolean) dbRow.get("cultivated"); // This will compile, but won't work
			final int count = (Integer) dbRow.get("count");
			final String image = (String) dbRow.get("image");
			final boolean orchard;
			switch (type) {
			case "grove":
				orchard = false;
				break;
			case "orchard":
				orchard = true;
				break;
			default:
				throw new IllegalArgumentException("Unexpected grove type");
			}
			final Grove grove = new Grove(orchard, cultivated, kind, id, count);
			if (image != null) {
				grove.setImage(image);
			}
			map.addFixture(new Point(row, column), grove);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "groves", readGrove(map),
				"SELECT * FROM groves");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
