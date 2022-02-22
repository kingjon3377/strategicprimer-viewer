package impl.dbio;

import buckelieg.jdbc.fn.DB;

import common.map.IFixture;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;

import common.map.fixtures.explorable.Portal;
import common.xmlio.Warning;

final class DBPortalHandler extends AbstractDatabaseWriter<Portal, Point> implements MapContentsReader {
	public DBPortalHandler() {
		super(Portal.class, Point.class);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS portals (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    image VARCHAR(255)," +
			"    destination_world VARCHAR(16)," +
			"    destination_row INTEGER," +
			"    destination_column INTEGER" +
			"        CHECK ((destination_row IS NOT NULL AND destination_column IS NOT NULL)" +
			"            OR (destination_row IS NULL AND destination_column IS NULL))" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO portals (row, column, id, image, destination_world, " +
			"    destination_row, destination_column) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?);";


	@Override
	public void write(final DB db, final Portal obj, final Point context) {
		final Integer destinationRow;
		final Integer destinationColumn;
		if (obj.getDestinationCoordinates().isValid()) {
			destinationRow = obj.getDestinationCoordinates().getRow();
			destinationColumn = obj.getDestinationCoordinates().getColumn();
		} else {
			destinationRow = null;
			destinationColumn = null;
		}
		db.update(INSERT_SQL, context.getRow(), context.getColumn(), obj.getId(),
			obj.getImage(), obj.getDestinationWorld(), destinationRow,
			destinationColumn).execute();
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception> readPortal(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String destinationWorld = (String) dbRow.get("destination_world");
			final Integer destinationRow = (Integer) dbRow.get("destination_row");
			final Integer destinationColumn = (Integer) dbRow.get("destination_column");
			final String image = (String) dbRow.get("image");
			final Portal portal = new Portal(destinationWorld == null ? "unknown" : destinationWorld,
				new Point(destinationRow == null ? -1 : destinationRow,
					destinationColumn == null ? -1 : destinationColumn), id);
			if (image != null) {
				portal.setImage(image);
			}
			map.addFixture(new Point(row, column), portal);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) {
		try {
			handleQueryResults(db, warner, "portals", readPortal(map),
				"SELECT * FROM portals");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
