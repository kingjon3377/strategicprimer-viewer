package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Map;

import common.map.IMutableMapNG;
import common.map.fixtures.Implement;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.xmlio.Warning;
import common.map.TileFixture;
import common.map.IFixture;

final class DBImplementHandler extends AbstractDatabaseWriter<Implement, /*IUnit|IFortress*/TileFixture>
		implements MapContentsReader {
	public DBImplementHandler() {
		super(Implement.class, TileFixture.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return obj instanceof Implement && (context instanceof IFortress || context instanceof IUnit);
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
		"CREATE TABLE IF NOT EXISTS implements (" +
			"    parent INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(255) NOT NULL," +
			"    count INTEGER NOT NULL DEFAULT 1," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO implements (parent, id, kind, count, image) " +
			"VALUES(?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final Implement obj, final TileFixture context) {
		db.update(INSERT_SQL, context.getId(), obj.getId(), obj.getKind(), obj.getCount(),
			obj.getImage()).execute();
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readImplement(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			int parentId = (Integer) dbRow.get("parent");
			IFixture parent = findById(map, parentId, warner);
			int id = (Integer) dbRow.get("id");
			String kind = (String) dbRow.get("kind");
			int count = (Integer) dbRow.get("count");
			String image = (String) dbRow.get("image");
			Implement implement = new Implement(kind, id, count);
			if (image != null) {
				implement.setImage(image);
			}
			if (parent instanceof IMutableUnit) {
				((IMutableUnit) parent).addMember(implement);
			} else if (parent instanceof IMutableFortress) {
				((IMutableFortress) parent).addMember(implement);
			} else {
				throw new IllegalArgumentException(
					"Implement can only be in a unit or fortress");
			}
		};
	}

	@Override
	public void readExtraMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "pieces of equipment",
				readImplement(map), "SELECT * FROM implements");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
