package impl.dbio;

import common.map.IFixture;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;

import common.map.fixtures.resources.Shrub;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

final class DBShrubHandler extends AbstractDatabaseWriter<Shrub, Point> implements MapContentsReader {
	public DBShrubHandler() {
		super(Shrub.class, Point.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
		Query.of("CREATE TABLE IF NOT EXISTS shrubs (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(64) NOT NULL," +
			"    count INTEGER," +
			"    image VARCHAR(255)" +
			");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL = Query.of(
			"INSERT INTO shrubs (row, column, id, kind, count, image) VALUES(:row, :column, :id, :kind, :count, :image);");

	@Override
	public void write(final Transactional db, final Shrub obj, final Point context) throws SQLException {
		INSERT_SQL.on(value("row", context.getRow()), value("column", context.getColumn()),
				value("id", obj.getId()), value("kind", obj.getKind()),
				value("count", obj.getPopulation()),
				value("image", obj.getImage())).execute(db.connection());
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readShrub(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final Integer count = (Integer) dbRow.get("count");
			final String image = (String) dbRow.get("image");
			final Shrub shrub = new Shrub(kind, id, count == null ? -1 : count);
			if (image != null) {
				shrub.setImage(image);
			}
			map.addFixture(new Point(row, column), shrub);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM shrubs");
	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "shrubs", readShrub(map), SELECT);
	}
}
