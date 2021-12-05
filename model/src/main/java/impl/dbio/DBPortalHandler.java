package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import java.sql.Types;

import common.map.Point;
import common.map.IMutableMapNG;

import common.map.fixtures.explorable.Portal;
import common.xmlio.Warning;

final class DBPortalHandler extends AbstractDatabaseWriter<Portal, Point> implements MapContentsReader {
	public DBPortalHandler() {
		super(Portal.class, Point.class);
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
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
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO portals (row, column, id, image, destination_world, " +
			"    destination_row, destination_column) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?);";


	@Override
	public void write(DB db, Portal obj, Point context) {
		Integer destinationRow;
		Integer destinationColumn;
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

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readPortal(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			int id = (Integer) dbRow.get("id");
			String destinationWorld = (String) dbRow.get("destination_world");
			Integer destinationRow = (Integer) dbRow.get("destination_row");
			Integer destinationColumn = (Integer) dbRow.get("destination_column");
			String image = (String) dbRow.get("image");
			Portal portal = new Portal(destinationWorld == null ? "unknown" : destinationWorld,
				new Point(destinationRow == null ? -1 : destinationRow,
					destinationColumn == null ? -1 : destinationColumn), id);
			if (image != null) {
				portal.setImage(image);
			}
			map.addFixture(new Point(row, column), portal);
		};
	}

	@Override
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "portals", readPortal(map),
				"SELECT * FROM portals");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
