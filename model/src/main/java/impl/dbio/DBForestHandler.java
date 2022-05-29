package impl.dbio;

import common.map.IFixture;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.math.BigDecimal;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.terrain.Forest;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

final class DBForestHandler extends AbstractDatabaseWriter<Forest, Point> implements MapContentsReader {
	public DBForestHandler() {
		super(Forest.class, Point.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
		Query.of("CREATE TABLE IF NOT EXISTS forests (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(32) NOT NULL," +
			"    rows BOOLEAN NOT NULL," +
			"    acres VARCHAR(128)" +
			"        CHECK (acres NOT LIKE '%[^0-9.]%' AND acres NOT LIKE '%.%.%')," +
			"    image VARCHAR(255)" +
			");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL = Query.of("INSERT INTO forests(row, column, id, kind, rows, acres, image) " +
				"VALUES(:row, :column, :id, :kind, :rows, :acres, :image);");

	@Override
	public void write(final Transactional db, final Forest obj, final Point context) throws SQLException {
		INSERT_SQL.on(value("row", context.row()), value("column", context.column()),
				value("id", obj.getId()), value("kind", obj.getKind()), value("rows", obj.isRows()),
				value("acres", obj.getAcres().toString()), value("image", obj.getImage())).execute(db.connection());
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readForest(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final boolean rows = getBooleanValue(dbRow, "rows");
			final String acresString = (String) dbRow.get("acres");
			final String image = (String) dbRow.get("image");
			Number acres;
			try {
				acres = Integer.parseInt(acresString);
			} catch (final NumberFormatException except) {
				acres = new BigDecimal(acresString);
			}
			final Forest forest = new Forest(kind, rows, id, acres);
			if (image != null) {
				forest.setImage(image);
			}
			map.addFixture(new Point(row, column), forest);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM forests");
	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "forests", readForest(map), SELECT);
	}
}
