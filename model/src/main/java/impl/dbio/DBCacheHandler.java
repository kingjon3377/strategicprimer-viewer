package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.resources.CacheFixture;
import common.xmlio.Warning;

final class DBCacheHandler extends AbstractDatabaseWriter<CacheFixture, Point> implements MapContentsReader {
	public DBCacheHandler() {
		super(CacheFixture.class, Point.class);
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
		"CREATE TABLE IF NOT EXISTS caches (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(32) NOT NULL," +
			"    contents VARCHAR(512) NOT NULL," +
			"    image VARCHAR(256)" +
			");");

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO caches(row, column, id, kind, contents, image) VALUES(?, ?, ?, ?, ?, ?);";

	@Override
	public void write(DB db, CacheFixture obj, Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(), obj.getKind(),
			obj.getContents(), obj.getImage()).execute();
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readCache(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int id = (Integer) dbRow.get("id");
			String kind = (String) dbRow.get("kind");
			String contents = (String) dbRow.get("contents");
			String image = (String) dbRow.get("image");
			CacheFixture cache = new CacheFixture(kind, contents, id);
			if (image != null) {
				cache.setImage(image);
			}
			map.addFixture(new Point(row, column), cache);
		};
	}

	@Override
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "caches", readCache(map),
				"SELECT * FROM caches");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}