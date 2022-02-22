package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.IMutableMapNG;
import common.map.fixtures.Implement;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.towns.IFortress;
import common.xmlio.Warning;
import common.map.TileFixture;
import common.map.IFixture;

final class DBImplementHandler extends AbstractDatabaseWriter<Implement, /*IUnit|IFortress|IWorker*/IFixture>
		implements MapContentsReader {
	public DBImplementHandler() {
		super(Implement.class, IFixture.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return obj instanceof Implement && (context instanceof IFortress || context instanceof IUnit);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS implements (" +
			"    parent INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(255) NOT NULL," +
			"    count INTEGER NOT NULL DEFAULT 1," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO implements (parent, id, kind, count, image) " +
			"VALUES(?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final Implement obj, final IFixture context) {
		db.update(INSERT_SQL, context.getId(), obj.getId(), obj.getKind(), obj.getCount(),
			obj.getImage()).execute();
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readImplement(final IMutableMapNG map,
			final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final int parentId = (Integer) dbRow.get("parent");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final int count = (Integer) dbRow.get("count");
			final String image = (String) dbRow.get("image");
			final Implement implement = new Implement(kind, id, count);
			if (image != null) {
				implement.setImage(image);
			}
			multimapPut(containees, parentId, implement);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) {
		try {
			handleQueryResults(db, warner, "pieces of equipment",
				readImplement(map, containees), "SELECT * FROM implements");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
