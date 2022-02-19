package impl.dbio;

import buckelieg.jdbc.fn.DB;

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

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS minerals (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    type VARCHAR(7) NOT NULL CHECK(type IN('stone', 'mineral'))," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(64) NOT NULL," +
			"    exposed BOOLEAN NOT NULL CHECK(exposed OR type IN('mineral'))," +
			"    dc INTEGER NOT NULL," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO minerals (row, column, type, id, kind, exposed, dc, image) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
	@Override
	public void write(final DB db, final MineralFixture obj, final Point context) {
		final String type;
		final boolean exposed;
		if (obj instanceof MineralVein) {
			type = "mineral";
			exposed = ((MineralVein) obj).isExposed();
		} else if (obj instanceof StoneDeposit) {
			type = "stone";
			exposed = true;
		} else {
			throw new IllegalArgumentException("Unhandled mineral fixture type");
		}
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), type,
			obj.getId(), obj.getKind(), exposed, obj.getDC(), ((HasImage) obj).getImage());
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readMineralVein(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final Boolean exposed = /* DBMapReader.databaseBoolean(dbRow.get("exposed")) */ // FIXME
				(Boolean) dbRow.get("exposed"); // This will compile but probably won't work
			final int dc = (Integer) dbRow.get("dc");
			final String image = (String) dbRow.get("image");
			final MineralVein mineral = new MineralVein(kind, exposed, dc, id);
			if (image != null) {
				mineral.setImage(image);
			}
			map.addFixture(new Point(row, column), mineral);
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readStoneDeposit(final IMutableMapNG map) {
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

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "stone deposits", readStoneDeposit(map),
				"SELECT row, column, id, kind, dc, image FROM minerals WHERE type = 'stone'");
			handleQueryResults(db, warner, "mineral veins", readMineralVein(map),
				"SELECT row, column, id, kind, exposed, dc, image FROM minerals " +
					"WHERE type = 'mineral'");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
