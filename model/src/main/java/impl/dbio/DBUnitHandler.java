package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;

import java.sql.Types;

import common.map.IMutableMapNG;
import common.map.Point;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.xmlio.Warning;
import common.map.fixtures.UnitMember;

final class DBUnitHandler extends AbstractDatabaseWriter<IUnit, Object> implements MapContentsReader {
	public DBUnitHandler(SPDatabaseWriter parent) {
		super(IUnit.class, Object.class);
		this.parent = parent;
	}

	/**
	 * What we use to write members. Called "parent" because *it* actually owns *this* object.
	 */
	private final SPDatabaseWriter parent;

	@Override
	public boolean canWrite(Object obj, Object context) {
		return obj instanceof IUnit && (context instanceof Point || context instanceof IFortress);
	}

	private static final Iterable<String> INITIALIZERS = Collections.unmodifiableList(Arrays.asList(
		"CREATE TABLE IF NOT EXISTS units (" +
			"    row INTEGER," +
			"    column INTEGER CHECK ((row IS NOT NULL AND column IS NOT NULL) OR" +
			"       (row IS NULL AND column IS NULL))," +
			"    parent INTEGER CHECK ((row IS NOT NULL AND parent IS NULL) OR" +
			"       (row IS NULL AND parent IS NOT NULL))," +
			"    owner INTEGER NOT NULL," +
			"    kind VARCHAR(32) NOT NULL," +
			"    name VARCHAR(64) NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    image VARCHAR(255)," +
			"    portrait VARCHAR(255)" +
			");",
		"CREATE TABLE IF NOT EXISTS orders (" +
			"    unit INTEGER NOT NULL," +
			"    turn INTEGER," +
			"    orders VARCHAR(2048) NOT NULL" +
			");",
		"CREATE TABLE IF NOT EXISTS results (" +
			"    unit INTEGER NOT NULL," +
			"    turn INTEGER," +
			"    result VARCHAR(2048) NOT NULL" +
			");"));

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_UNIT =
		"INSERT INTO units (row, column, parent, owner, kind, name, id, image, portrait) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private static final String INSERT_ORDER =
		"INSERT INTO orders (unit, turn, orders) VALUES(?, ?, ?);";

	private static final String INSERT_RESULT =
		"INSERT INTO results (unit, turn, result) VALUES(?, ?, ?);";

	@Override
	public void write(DB db, IUnit obj, Object context) {
		db.transaction(sql -> {
				String portrait = obj.getPortrait();
				if (context instanceof Point) {
					sql.update(INSERT_UNIT, ((Point) context).getRow(),
						((Point) context).getColumn(), null,
						obj.getOwner().getPlayerId(), obj.getKind(), obj.getName(),
						obj.getId(), obj.getImage(), portrait).execute();
				} else if (context instanceof IFortress) {
					sql.update(INSERT_UNIT, null, null, ((IFortress) context).getId(),
						obj.getOwner().getPlayerId(), obj.getKind(), obj.getName(),
						obj.getId(), obj.getImage(), portrait).execute();
				} else {
					throw new IllegalArgumentException(
						"Context must be point or fortress");
				}
				for (Map.Entry<Integer, String> entry : obj.getAllOrders().entrySet()) {
					sql.update(INSERT_ORDER, obj.getId(), entry.getKey(),
						entry.getValue()).execute();
				}
				for (Map.Entry<Integer, String> entry : obj.getAllResults().entrySet()) {
					sql.update(INSERT_RESULT, obj.getId(), entry.getKey(),
						entry.getValue()).execute();
				}
				return true;
			});
		for (UnitMember member : obj) {
			parent.writeSPObjectInContext(db, member, obj);
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readOrders(IMutableUnit unit) {
		return (dbRow, warner) -> {
			Integer turn = (Integer) dbRow.get("turn");
			String orders = (String) dbRow.get("orders");
			unit.setOrders(turn == null ? -1 : turn, orders);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readResults(IMutableUnit unit) {
		return (dbRow, warner) -> {
			Integer turn = (Integer) dbRow.get("turn");
			String results = (String) dbRow.get("results");
			unit.setResults(turn == null ? -1 : turn, results);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readUnit(IMutableMapNG map, DB db) {
		return (dbRow, warner) -> {
			int ownerNum = (Integer) dbRow.get("owner");
			String kind = (String) dbRow.get("kind");
			String name = (String) dbRow.get("name");
			int id = (Integer) dbRow.get("id");
			String image = (String) dbRow.get("image");
			String portrait = (String) dbRow.get("portrait");
			IMutableUnit unit = new Unit(map.getPlayers().getPlayer(ownerNum), kind, name, id);
			if (image != null) {
				unit.setImage(image);
			}
			if (portrait != null) {
				unit.setPortrait(portrait);
			}
			handleQueryResults(db, warner, "turns' orders", readOrders(unit),
				"SELECT * from orders WHERE unit = ?", id);
			handleQueryResults(db, warner, "turns' results", readResults(unit),
				"SELECT * from results WHERE unit = ?", id);
			Integer row = (Integer) dbRow.get("row");
			Integer column = (Integer) dbRow.get("column");
			if (row != null && column != null) {
				map.addFixture(new Point(row, column), unit);
			} else {
				IMutableFortress parent = (IMutableFortress) findById(map,
					(Integer) dbRow.get("parent"), warner);
				parent.addMember(unit);
			}
		};
	}

	@Override
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "units outside fortresses",
				readUnit(map, db), "SELECT * FROM units WHERE row IS NOT NULL");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}

	@Override
	public void readExtraMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "units in fortresses",
				readUnit(map, db), "SELECT * FROM units WHERE parent IS NOT NULL");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
