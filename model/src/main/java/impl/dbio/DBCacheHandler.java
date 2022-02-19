package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.resources.CacheFixture;
import common.xmlio.Warning;

final class DBCacheHandler extends AbstractDatabaseWriter<CacheFixture, Point> implements MapContentsReader {
	public DBCacheHandler() {
		super(CacheFixture.class, Point.class);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS caches (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(32) NOT NULL," +
			"    contents VARCHAR(512) NOT NULL," +
			"    image VARCHAR(256)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO caches(row, column, id, kind, contents, image) VALUES(?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final CacheFixture obj, final Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(), obj.getKind(),
			obj.getContents(), obj.getImage()).execute();
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readCache(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final String contents = (String) dbRow.get("contents");
			final String image = (String) dbRow.get("image");
			final CacheFixture cache = new CacheFixture(kind, contents, id);
			if (image != null) {
				cache.setImage(image);
			}
			map.addFixture(new Point(row, column), cache);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "caches", readCache(map),
				"SELECT * FROM caches");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
