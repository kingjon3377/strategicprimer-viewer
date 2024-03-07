package legacy.dbio;

import legacy.map.IFixture;
import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import legacy.map.Point;
import legacy.map.IMutableLegacyMap;
import legacy.map.fixtures.resources.CacheFixture;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBCacheHandler extends AbstractDatabaseWriter<CacheFixture, Point> implements MapContentsReader {
	public DBCacheHandler() {
		super(CacheFixture.class, Point.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
			Query.of("CREATE TABLE IF NOT EXISTS caches (" +
					"    row INTEGER NOT NULL," +
					"    column INTEGER NOT NULL," +
					"    id INTEGER NOT NULL," +
					"    kind VARCHAR(32) NOT NULL," +
					"    contents VARCHAR(512) NOT NULL," +
					"    image VARCHAR(256)" +
					");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL =
			Query.of("INSERT INTO caches(row, column, id, kind, contents, image) " +
					"VALUES(:row, :column, :id, :kind, :contents, :image);");

	@Override
	public void write(final Transactional db, final CacheFixture obj, final Point context) throws SQLException {
		INSERT_SQL.on(value("row", context.row()), value("column", context.column()), value("id", obj.getId()),
				value("kind", obj.getKind()), value("contents", obj.getContents()),
				value("image", obj.getImage())).execute(db.connection());
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readCache(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final String contents = (String) dbRow.get("contents");
			final String image = (String) dbRow.get("image");
			final CacheFixture cache = new CacheFixture(kind, contents, id);
			if (!Objects.isNull(image)) {
				cache.setImage(image);
			}
			map.addFixture(new Point(row, column), cache);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM caches");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map, final Map<Integer, IFixture> containers,
								final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "caches", readCache(map), SELECT);
	}
}
