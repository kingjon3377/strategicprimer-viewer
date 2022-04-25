package impl.dbio;

import common.map.IFixture;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.IMutableMapNG;
import common.map.HasImage;
import common.map.HasMutableImage;
import common.map.Point;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Oasis;
import common.xmlio.Warning;
import common.map.fixtures.TerrainFixture;
import lovelace.util.LovelaceLogger;

import static io.jenetics.facilejdbc.Param.value;

final class DBSimpleTerrainHandler extends AbstractDatabaseWriter<TerrainFixture, Point>
		implements MapContentsReader {
	public DBSimpleTerrainHandler() {
		super(TerrainFixture.class, Point.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return (obj instanceof Hill || obj instanceof Oasis) && context instanceof Point;
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
		Query.of("CREATE TABLE IF NOT EXISTS simple_terrain (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    type VARCHAR(7) NOT NULL" +
			"        CHECK(type IN('hill', 'oasis', 'sandbar'))," +
			"    id INTEGER NOT NULL," +
			"    image VARCHAR(255)" +
			");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT = Query.of(
			"INSERT INTO simple_terrain (row, column, type, id, image) " +
					"VALUES(:row, :column, :type, :id, :image);");
	@Override
	public void write(final Transactional db, final TerrainFixture obj, final Point context) throws SQLException {
		final String type;
		if (obj instanceof Hill) {
			type = "hill";
		} else if (obj instanceof Oasis) {
			type = "oasis";
		} else {
			throw new IllegalArgumentException("Unhandled terrain fixture type");
		}
		INSERT.on(value("row", context.getRow()), value("column", context.getColumn()), value("type", type),
				value("id", obj.getId()), value("image", ((HasImage) obj).getImage())).execute(db.connection());
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readSimpleTerrain(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final String type = (String) dbRow.get("type");
			final int id = (Integer) dbRow.get("id");
			final String image = (String) dbRow.get("image");
			final TerrainFixture fixture;
			switch (type) {
			case "hill":
				fixture = new Hill(id);
				break;
			case "sandbar":
				LovelaceLogger.info("Ignoring 'sandbar' with ID %d", id);
				return;
			case "oasis":
				fixture = new Oasis(id);
				break;
			default:
				throw new IllegalArgumentException("Unhandled simple terrain-fixture type");
			}
			if (image != null) {
				((HasMutableImage) fixture).setImage(image);
			}
			map.addFixture(new Point(row, column), fixture);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM simple_terrain");
	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "simple terrain fixtures", readSimpleTerrain(map), SELECT);
	}
}
