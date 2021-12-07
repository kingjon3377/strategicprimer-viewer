package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.towns.FortressImpl;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.TownSize;
import common.xmlio.Warning;
import common.map.fixtures.FortressMember;

final class DBFortressHandler extends AbstractDatabaseWriter<IFortress, Point> implements MapContentsReader {
	public DBFortressHandler(SPDatabaseWriter parent) {
		super(IFortress.class, Point.class);
		this.parent = parent;
	}

	/**
	 * What we use to write members. Called "parent" because *it* actually owns *this* object.
	 */
	private final SPDatabaseWriter parent;

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
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
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO fortresses (row, column, owner, name, size, id, image, portrait) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void write(DB db, IFortress obj, Point context) {
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getOwner().getPlayerId(),
			obj.getName(), obj.getTownSize().toString(), obj.getId(), obj.getImage(),
			obj.getPortrait()).execute();
		for (FortressMember member : obj) {
			parent.writeSPObjectInContext(db, member, obj);
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readFortress(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int ownerId = (Integer) dbRow.get("owner");
			String name = (String) dbRow.get("name");
			TownSize size = TownSize.parseTownSize((String) dbRow.get("size"));
			int id = (Integer) dbRow.get("id");
			String image = (String) dbRow.get("image");
			String portrait = (String) dbRow.get("portrait");
			IMutableFortress fortress = new FortressImpl(map.getPlayers().getPlayer(ownerId),
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
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "fortresses", readFortress(map),
				"SELECT * FROM fortresses");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
