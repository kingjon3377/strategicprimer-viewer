package impl.dbio;

import common.map.IFixture;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import common.map.IMutableMapNG;
import common.map.Point;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.towns.IFortress;
import common.xmlio.Warning;
import common.map.fixtures.UnitMember;
import org.jetbrains.annotations.Nullable;

import static io.jenetics.facilejdbc.Param.value;

final class DBUnitHandler extends AbstractDatabaseWriter<IUnit, Object> implements MapContentsReader {
	public DBUnitHandler(final @Nullable SPDatabaseWriter parent) {
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

	private static final List<Query> INITIALIZERS = List.of(
		Query.of("CREATE TABLE IF NOT EXISTS units (" +
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
			");"),
		Query.of("CREATE TABLE IF NOT EXISTS orders (" +
			"    unit INTEGER NOT NULL," +
			"    turn INTEGER," +
			"    orders VARCHAR(2048) NOT NULL" +
			");"),
		Query.of("CREATE TABLE IF NOT EXISTS results (" +
			"    unit INTEGER NOT NULL," +
			"    turn INTEGER," +
			"    result VARCHAR(2048) NOT NULL" +
			");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_UNIT =
		Query.of("INSERT INTO units (row, column, parent, owner, kind, name, id, image, portrait) " +
			"VALUES(:row, :column, :parent, :owner, :kind, :name, :id, :image, :portrait);");

	private static final Query INSERT_ORDER =
		Query.of("INSERT INTO orders (unit, turn, orders) VALUES(:unit, :turn, :orders);");

	private static final Query INSERT_RESULT =
		Query.of("INSERT INTO results (unit, turn, result) VALUES(:unit, :turn, :result);");

	@Override
	public void write(final Transactional db, final IUnit obj, final Object context) throws SQLException {
		db.transaction().accept(sql -> {
			final String portrait = obj.getPortrait();
			if (context instanceof final Point p) {
				INSERT_UNIT.on(value("row", p.row()),
					value("column", p.column()),
					value("owner", obj.owner().getPlayerId()),
					value("kind", obj.getKind()), value("name", obj.getName()),
					value("id", obj.getId()), value("image", obj.getImage()),
					value("portrait", portrait)).execute(sql);
			} else if (context instanceof final IFortress f) {
				INSERT_UNIT.on(
					value("parent", f.getId()),
					value("owner", obj.owner().getPlayerId()), value("kind", obj.getKind()),
					value("name", obj.getName()), value("id", obj.getId()),
					value("image", obj.getImage()), value("portrait", portrait)).execute(sql);
			} else {
				throw new IllegalArgumentException(
					"Context must be point or fortress");
			}
			for (final Map.Entry<Integer, String> entry : obj.getAllOrders().entrySet()) {
				INSERT_ORDER.on(value("unit", obj.getId()), value("turn", entry.getKey()),
					value("orders", entry.getValue())).execute(sql);
			}
			for (final Map.Entry<Integer, String> entry : obj.getAllResults().entrySet()) {
				INSERT_RESULT.on(value("unit", obj.getId()), value("turn", entry.getKey()),
					value("result", entry.getValue())).execute(sql);
			}
		});
		for (final UnitMember member : obj) {
			parent.writeSPObjectInContext(db, member, obj);
		}
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readOrders(final IMutableUnit unit) {
		return (dbRow, warner) -> {
			final Integer turn = (Integer) dbRow.get("turn");
			final String orders = (String) dbRow.get("orders");
			unit.setOrders(turn == null ? -1 : turn, orders);
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readResults(final IMutableUnit unit) {
		return (dbRow, warner) -> {
			final Integer turn = (Integer) dbRow.get("turn");
			final String results = (String) dbRow.get("results");
			unit.setResults(turn == null ? -1 : turn, results);
		};
	}

	private static final Query SELECT_ORDERS = Query.of("SELECT * FROM orders WHERE unit = ?"); // TODO: named param?
	private static final Query SELECT_RESULTS = Query.of("SELECT * FROM results WHERE unit = ?"); // TODO: named param?

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readUnit(final IMutableMapNG map, final Connection db,
																			   final Map<Integer, IFixture> containers, final Map<Integer, List<Object>> containees) {
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
				SELECT_ORDERS, id);
			handleQueryResults(db, warner, "turns' results", readResults(unit),
				SELECT_RESULTS, id);
			final Integer row = (Integer) dbRow.get("row");
			final Integer column = (Integer) dbRow.get("column");
			if (row != null && column != null) {
				map.addFixture(new Point(row, column), unit);
			} else {
				multimapPut(containees, (Integer) dbRow.get("parent"), unit);
			}
			containers.put(unit.getId(), unit);
		};
	}

	private static final Query SELECT_UNITS = Query.of("SELECT * FROM units");

	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
								final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		// FIXME: Move orders and results handling to here so we don't have to pass DB to the row-handler
		handleQueryResults(db, warner, "units", readUnit(map, db, containers, containees), SELECT_UNITS);
	}
}
