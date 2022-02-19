package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
	public DBUnitHandler(final SPDatabaseWriter parent) {
		super(IUnit.class, Object.class);
		this.parent = parent;
	}

	/**
	 * What we use to write members. Called "parent" because *it* actually owns *this* object.
	 */
	private final SPDatabaseWriter parent;

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return obj instanceof IUnit && (context instanceof Point || context instanceof IFortress);
	}

	private static final List<String> INITIALIZERS = Collections.unmodifiableList(Arrays.asList(
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
	public List<String> getInitializers() {
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
	public void write(final DB db, final IUnit obj, final Object context) {
		db.transaction(sql -> {
				final String portrait = obj.getPortrait();
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
				for (final Map.Entry<Integer, String> entry : obj.getAllOrders().entrySet()) {
					sql.update(INSERT_ORDER, obj.getId(), entry.getKey(),
						entry.getValue()).execute();
				}
				for (final Map.Entry<Integer, String> entry : obj.getAllResults().entrySet()) {
					sql.update(INSERT_RESULT, obj.getId(), entry.getKey(),
						entry.getValue()).execute();
				}
				return true;
			});
		for (final UnitMember member : obj) {
			parent.writeSPObjectInContext(db, member, obj);
		}
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readOrders(final IMutableUnit unit) {
		return (dbRow, warner) -> {
			final Integer turn = (Integer) dbRow.get("turn");
			final String orders = (String) dbRow.get("orders");
			unit.setOrders(turn == null ? -1 : turn, orders);
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readResults(final IMutableUnit unit) {
		return (dbRow, warner) -> {
			final Integer turn = (Integer) dbRow.get("turn");
			final String results = (String) dbRow.get("results");
			unit.setResults(turn == null ? -1 : turn, results);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readUnit(final IMutableMapNG map, final DB db) {
		return (dbRow, warner) -> {
			final int ownerNum = (Integer) dbRow.get("owner");
			final String kind = (String) dbRow.get("kind");
			final String name = (String) dbRow.get("name");
			final int id = (Integer) dbRow.get("id");
			final String image = (String) dbRow.get("image");
			final String portrait = (String) dbRow.get("portrait");
			final IMutableUnit unit = new Unit(map.getPlayers().getPlayer(ownerNum), kind, name, id);
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
			final Integer row = (Integer) dbRow.get("row");
			final Integer column = (Integer) dbRow.get("column");
			if (row != null && column != null) {
				map.addFixture(new Point(row, column), unit);
			} else {
				final IMutableFortress parent = (IMutableFortress) findById(map,
					(Integer) dbRow.get("parent"), warner);
				parent.addMember(unit);
			}
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "units outside fortresses",
				readUnit(map, db), "SELECT * FROM units WHERE row IS NOT NULL");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}

	@Override
	public void readExtraMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "units in fortresses",
				readUnit(map, db), "SELECT * FROM units WHERE parent IS NOT NULL");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
