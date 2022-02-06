package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.explorable.Battlefield;
import common.map.fixtures.explorable.ExplorableFixture;
import common.map.fixtures.explorable.Cave;
import common.xmlio.Warning;

final class DBExplorableHandler extends AbstractDatabaseWriter<ExplorableFixture, Point>
		implements MapContentsReader {
	public DBExplorableHandler() {
		super(ExplorableFixture.class, Point.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return (obj instanceof Battlefield || obj instanceof Cave) && context instanceof Point;
	}

	private static final List<String> INITIALIZERS = Collections.unmodifiableList(Arrays.asList(
		"CREATE TABLE IF NOT EXISTS caves (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    dc INTEGER NOT NULL," +
			"    image VARCHAR(255)" +
			");",
		"CREATE TABLE IF NOT EXISTS battlefields (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    dc INTEGER NOT NULL," +
			"    image VARCHAR(255)" +
			");"));

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String CAVE_INSERT =
		"INSERT INTO caves (row, column, id, dc, image) VALUES(?, ?, ?, ?, ?);";

	private static final String BATTLEFIELD_INSERT =
		"INSERT INTO battlefields (row, column, id, dc, image) VALUES(?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final ExplorableFixture obj, final Point context) {
		String sql;
		if (obj instanceof Cave) {
			sql = CAVE_INSERT;
		} else if (obj instanceof Battlefield) {
			sql = BATTLEFIELD_INSERT;
		} else {
			throw new IllegalArgumentException("Only supports caves and battlefields");
		}
		db.update(sql, context.getRow(), context.getColumn(), obj.getId(), obj.getDC(),
			obj.getImage()).execute();
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readCave(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int id = (Integer) dbRow.get("id");
			int dc = (Integer) dbRow.get("dc");
			String image = (String) dbRow.get("image");
			Cave cave = new Cave(dc, id);
			if (image != null) {
				cave.setImage(image);
			}
			map.addFixture(new Point(row, column), cave);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readBattlefield(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int id = (Integer) dbRow.get("id");
			int dc = (Integer) dbRow.get("dc");
			String image = (String) dbRow.get("image");
			Battlefield battlefield = new Battlefield(dc, id);
			if (image != null) {
				battlefield.setImage(image);
			}
			map.addFixture(new Point(row, column), battlefield);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "caves", readCave(map),
				"SELECT * FROM caves");
			handleQueryResults(db, warner, "battlefields", readBattlefield(map),
				"SELECT * FROM battlefields");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
