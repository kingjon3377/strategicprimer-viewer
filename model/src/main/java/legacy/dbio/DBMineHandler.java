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

import legacy.map.Point;
import legacy.map.IMutableLegacyMap;
import legacy.map.fixtures.resources.Mine;
import common.map.fixtures.towns.TownStatus;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBMineHandler extends AbstractDatabaseWriter<Mine, Point> implements MapContentsReader {
	public DBMineHandler() {
		super(Mine.class, Point.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
		Query.of("CREATE TABLE IF NOT EXISTS mines (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(128) NOT NULL," +
			"    status VARCHAR(9) NOT NULL" +
			"        CHECK(status IN ('abandoned', 'active', 'burned', 'ruined'))," +
			"    image VARCHAR(255)" +
			");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL =
		Query.of("INSERT INTO mines (row, column, id, kind, status, image) " +
			"VALUES(:row, :column, :id, :kind, :status, :image);");

	@Override
	public void write(final Transactional db, final Mine obj, final Point context) throws SQLException {
		INSERT_SQL.on(value("row", context.row()), value("column", context.column()),
				value("id", obj.getId()), value("kind", obj.getKind()),
				value("status", obj.getStatus().toString()), value("image", obj.getImage()))
			.execute(db.connection());
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readMine(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final TownStatus status = TownStatus.parse((String) dbRow.get("status"));
			final String image = (String) dbRow.get("image");
			final Mine mine = new Mine(kind, status, id);
			if (image != null) {
				mine.setImage(image);
			}
			map.addFixture(new Point(row, column), mine);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM mines");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map, final Map<Integer, IFixture> containers,
                                final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "mines", readMine(map), SELECT);
	}
}
