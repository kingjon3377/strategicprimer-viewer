package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

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
	public boolean canWrite(Object obj, Object context) {
		return (obj instanceof MineralVein || obj instanceof StoneDeposit) &&
			context instanceof Point;
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
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
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO minerals (row, column, type, id, kind, exposed, dc, image) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
	@Override
	public void write(DB db, MineralFixture obj, Point context) {
		String type;
		boolean exposed;
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

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readMineralVein(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int id = (Integer) dbRow.get("id");
			String kind = (String) dbRow.get("kind");
			Boolean exposed = /* DBMapReader.databaseBoolean(dbRow.get("exposed")) */ // FIXME
				(Boolean) dbRow.get("exposed"); // This will compile but probably won't work
			int dc = (Integer) dbRow.get("dc");
			String image = (String) dbRow.get("image");
			MineralVein mineral = new MineralVein(kind, exposed, dc, id);
			if (image != null) {
				mineral.setImage(image);
			}
			map.addFixture(new Point(row, column), mineral);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readStoneDeposit(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int id = (Integer) dbRow.get("id");
			StoneKind kind = StoneKind.parse((String) dbRow.get("kind"));
			int dc = (Integer) dbRow.get("dc");
			String image = (String) dbRow.get("image");
			StoneDeposit stone = new StoneDeposit(kind, dc, id);
			if (image != null) {
				stone.setImage(image);
			}
			map.addFixture(new Point(row, column), stone);
		};
	}

	@Override
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "stone deposits", readStoneDeposit(map),
				"SELECT row, column, id, kind, dc, image FROM minerals WHERE type = 'stone'");
			handleQueryResults(db, warner, "mineral veins", readMineralVein(map),
				"SELECT row, column, id, kind, exposed, dc, image FROM minerals " +
					"WHERE type = 'mineral'");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
