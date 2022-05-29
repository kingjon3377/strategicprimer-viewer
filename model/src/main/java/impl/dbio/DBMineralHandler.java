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

import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.MineralFixture;
import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.StoneKind;
import common.xmlio.Warning;
import common.map.HasImage;

import static io.jenetics.facilejdbc.Param.value;

final class DBMineralHandler extends AbstractDatabaseWriter<MineralFixture, Point>
		implements MapContentsReader {
	public DBMineralHandler() {
		super(MineralFixture.class, Point.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return (obj instanceof MineralVein || obj instanceof StoneDeposit) &&
			context instanceof Point;
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
		Query.of("CREATE TABLE IF NOT EXISTS minerals (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    type VARCHAR(7) NOT NULL CHECK(type IN('stone', 'mineral'))," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(64) NOT NULL," +
			"    exposed BOOLEAN NOT NULL CHECK(exposed OR type IN('mineral'))," +
			"    dc INTEGER NOT NULL," +
			"    image VARCHAR(255)" +
			");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL =
		Query.of("INSERT INTO minerals (row, column, type, id, kind, exposed, dc, image) " +
			"VALUES(:row, :column, :type, :id, :kind, :exposed, :dc, :image);");
	@Override
	public void write(final Transactional db, final MineralFixture obj, final Point context) throws SQLException {
		final String type;
		final boolean exposed;
		if (obj instanceof MineralVein m) {
			type = "mineral";
			exposed = m.isExposed();
		} else if (obj instanceof StoneDeposit) {
			type = "stone";
			exposed = true;
		} else {
			throw new IllegalArgumentException("Unhandled mineral fixture type");
		}
		INSERT_SQL.on(value("row", context.getRow()), value("column", context.getColumn()),
				value("type", type), value("id", obj.getId()), value("kind", obj.getKind()),
				value("exposed", exposed), value("dc", obj.getDC()),
				value("image", ((HasImage) obj).getImage())).execute(db.connection());
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readMineralVein(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final boolean exposed = getBooleanValue(dbRow, "exposed");
			final int dc = (Integer) dbRow.get("dc");
			final String image = (String) dbRow.get("image");
			final MineralVein mineral = new MineralVein(kind, exposed, dc, id);
			if (image != null) {
				mineral.setImage(image);
			}
			map.addFixture(new Point(row, column), mineral);
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readStoneDeposit(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final StoneKind kind = StoneKind.parse((String) dbRow.get("kind"));
			final int dc = (Integer) dbRow.get("dc");
			final String image = (String) dbRow.get("image");
			final StoneDeposit stone = new StoneDeposit(kind, dc, id);
			if (image != null) {
				stone.setImage(image);
			}
			map.addFixture(new Point(row, column), stone);
		};
	}

	private static final Query SELECT_STONE =
			Query.of("SELECT row, column, id, kind, dc, image FROM minerals WHERE type = 'stone'");
	private static final Query SELECT_MINERAL =
			Query.of("SELECT row, column, id, kind, exposed, dc, image FROM minerals WHERE type = 'mineral'");
	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "stone deposits", readStoneDeposit(map), SELECT_STONE);
		handleQueryResults(db, warner, "mineral veins", readMineralVein(map), SELECT_MINERAL);
	}
}
