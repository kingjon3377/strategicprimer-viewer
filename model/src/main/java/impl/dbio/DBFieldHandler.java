package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;

import java.math.BigDecimal;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.FieldStatus;
import common.xmlio.Warning;

final class DBFieldHandler extends AbstractDatabaseWriter<Meadow, Point> implements MapContentsReader {
	public DBFieldHandler() {
		super(Meadow.class, Point.class);
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
		"CREATE TABLE IF NOT EXISTS fields (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    type VARCHAR(6) NOT NULL" +
			"        CHECK (type IN ('field', 'meadow'))," +
			"    kind VARCHAR(64) NOT NULL," +
			"    cultivated BOOLEAN NOT NULL," +
			"    status VARCHAR(7) NOT NULL" +
			"        CHECK (status IN ('fallow', 'seeding', 'growing', 'bearing'))," +
			"    acres VARCHAR(128)" +
			"        CHECK (acres NOT LIKE '%[^0-9.]%' AND acres NOT LIKE '%.%.%')," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO fields (row, column, id, type, kind, cultivated, status, acres, image) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void write(DB db, Meadow obj, Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(),
			(obj.isField()) ? "field" : "meadow", obj.getKind(), obj.isCultivated(),
			obj.getStatus().toString(), obj.getAcres().toString(), obj.getImage());
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readMeadow(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int id = (Integer) dbRow.get("id");
			String type = (String) dbRow.get("type");
			String kind = (String) dbRow.get("kind");
			Boolean cultivated = /*DBMapReader.databaseBoolean(dbRow.get("cultivated"))*///FIXME
				(Boolean) dbRow.get("cultivated"); // This will compile, probably won't work
			FieldStatus status = FieldStatus.parse((String) dbRow.get("status"));
			String acresString = (String) dbRow.get("acres");
			String image = (String) dbRow.get("image");
			Number acres;
			try {
				acres = Integer.parseInt(acresString);
			} catch (NumberFormatException except) {
				acres = new BigDecimal(acresString);
			}
			final boolean field;
			switch (type) {
			case "meadow":
				field = false;
				break;
			case "field":
				field = true;
				break;
			default:
				throw new IllegalArgumentException("Unhandled field type");
			}
			Meadow meadow = new Meadow(kind, field, cultivated, id, status, acres);
			if (image != null) {
				meadow.setImage(image);
			}
			map.addFixture(new Point(row, column), meadow);
		};
	}

	@Override
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "meadows", readMeadow(map),
				"SELECT * FROM fields");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
