package legacy.dbio;

import legacy.map.IFixture;
import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import legacy.map.Point;
import legacy.map.IMutableLegacyMap;
import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.explorable.ExplorableFixture;
import legacy.map.fixtures.explorable.Cave;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBExplorableHandler extends AbstractDatabaseWriter<ExplorableFixture, Point>
		implements MapContentsReader {
	public DBExplorableHandler() {
		super(ExplorableFixture.class, Point.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return (obj instanceof Battlefield || obj instanceof Cave) && context instanceof Point;
	}

	private static final List<Query> INITIALIZERS = List.of(
			Query.of("CREATE TABLE IF NOT EXISTS caves (" +
					"    row INTEGER NOT NULL," +
					"    column INTEGER NOT NULL," +
					"    id INTEGER NOT NULL," +
					"    dc INTEGER NOT NULL," +
					"    image VARCHAR(255)" +
					");"),
			Query.of("CREATE TABLE IF NOT EXISTS battlefields (" +
					"    row INTEGER NOT NULL," +
					"    column INTEGER NOT NULL," +
					"    id INTEGER NOT NULL," +
					"    dc INTEGER NOT NULL," +
					"    image VARCHAR(255)" +
					");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query CAVE_INSERT =
			Query.of("INSERT INTO caves (row, column, id, dc, image) VALUES(:row, :column, :id, :dc, :image);");

	private static final Query BATTLEFIELD_INSERT =
			Query.of("INSERT INTO battlefields (row, column, id, dc, image) VALUES(:row, :column, :id, :dc, :image);");

	@Override
	public void write(final Transactional db, final ExplorableFixture obj, final Point context) throws SQLException {
		final Query sql;
		if (obj instanceof Cave) {
			sql = CAVE_INSERT;
		} else if (obj instanceof Battlefield) {
			sql = BATTLEFIELD_INSERT;
		} else {
			throw new IllegalArgumentException("Only supports caves and battlefields");
		}
		sql.on(value("row", context.row()), value("column", context.column()), value("id", obj.getId()),
				value("dc", obj.getDC()), value("image", obj.getImage())).execute(db.connection());
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readCave(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final int dc = (Integer) dbRow.get("dc");
			final String image = (String) dbRow.get("image");
			final Cave cave = new Cave(dc, id);
			if (!Objects.isNull(image)) {
				cave.setImage(image);
			}
			map.addFixture(new Point(row, column), cave);
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readBattlefield(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final int dc = (Integer) dbRow.get("dc");
			final String image = (String) dbRow.get("image");
			final Battlefield battlefield = new Battlefield(dc, id);
			if (!Objects.isNull(image)) {
				battlefield.setImage(image);
			}
			map.addFixture(new Point(row, column), battlefield);
		};
	}

	private static final Query SELECT_CAVES = Query.of("SELECT * FROM caves");
	private static final Query SELECT_BATTLES = Query.of("SELECT * FROM battlefields");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map, final Map<Integer, IFixture> containers,
								final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "caves", readCave(map), SELECT_CAVES);
		handleQueryResults(db, warner, "battlefields", readBattlefield(map), SELECT_BATTLES);
	}
}
