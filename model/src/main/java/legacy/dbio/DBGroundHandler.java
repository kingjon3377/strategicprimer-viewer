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
import legacy.map.fixtures.Ground;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBGroundHandler extends AbstractDatabaseWriter<Ground, Point> implements MapContentsReader {
	public DBGroundHandler() {
		super(Ground.class, Point.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
			Query.of("CREATE TABLE IF NOT EXISTS ground (" +
					"    row INTEGER NOT NULL," +
					"    column INTEGER NOT NULL," +
					"    id INTEGER NOT NULL," +
					"    kind VARCHAR(32) NOT NULL," +
					"    exposed BOOLEAN NOT NULL," +
					"    image VARCHAR(255)" +
					");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL = Query.of(
			"""
					INSERT INTO ground (row, column, id, kind, exposed, image)
					VALUES(:row, :column, :id, :kind, :exposed, :image);""");

	@Override
	public void write(final Transactional db, final Ground obj, final Point context) throws SQLException {
		INSERT_SQL.on(value("row", context.row()), value("column", context.column()),
				value("id", obj.getId()), value("kind", obj.getKind()),
				value("exposed", obj.isExposed()), value("image", obj.getImage())).execute(db.connection());
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readGround(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final boolean exposed = getBooleanValue(dbRow, "exposed");
			final String image = (String) dbRow.get("image");
			final Ground ground = new Ground(id, kind, exposed);
			if (!Objects.isNull(image)) {
				ground.setImage(image);
			}
			map.addFixture(new Point(row, column), ground);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM ground");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map,
	                            final Map<Integer, IFixture> containers, final Map<Integer, List<Object>> containees,
	                            final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "ground", readGround(map), SELECT);
	}
}
