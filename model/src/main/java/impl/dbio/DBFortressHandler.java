package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.towns.FortressImpl;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.TownSize;
import common.xmlio.Warning;
import common.map.fixtures.FortressMember;

final class DBFortressHandler extends AbstractDatabaseWriter<IFortress, Point> implements MapContentsReader {
	public DBFortressHandler(final SPDatabaseWriter parent) {
		super(IFortress.class, Point.class);
		this.parent = parent;
	}

	/**
	 * What we use to write members. Called "parent" because *it* actually owns *this* object.
	 */
	private final SPDatabaseWriter parent;

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS fortresses (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    owner INTEGER NOT NULL," +
			"    name VARCHAR(64) NOT NULL," +
			"    size VARCHAR(6) NOT NULL" +
			"        CHECK(size IN ('small', 'medium', 'large'))," +
			"    id INTEGER NOT NULL," +
			"    image VARCHAR(255)," +
			"    portrait VARCHAR(255)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO fortresses (row, column, owner, name, size, id, image, portrait) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final IFortress obj, final Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getOwner().getPlayerId(),
			obj.getName(), obj.getTownSize().toString(), obj.getId(), obj.getImage(),
			obj.getPortrait()).execute();
		for (final FortressMember member : obj) {
			parent.writeSPObjectInContext(db, member, obj);
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readFortress(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int ownerId = (Integer) dbRow.get("owner");
			final String name = (String) dbRow.get("name");
			final TownSize size = TownSize.parseTownSize((String) dbRow.get("size"));
			final int id = (Integer) dbRow.get("id");
			final String image = (String) dbRow.get("image");
			final String portrait = (String) dbRow.get("portrait");
			final IMutableFortress fortress = new FortressImpl(map.getPlayers().getPlayer(ownerId),
				name, id, size);
			if (image != null) {
				fortress.setImage(image);
			}
			if (portrait != null) {
				fortress.setPortrait(portrait);
			}
			map.addFixture(new Point(row, column), fortress);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "fortresses", readFortress(map),
				"SELECT * FROM fortresses");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
