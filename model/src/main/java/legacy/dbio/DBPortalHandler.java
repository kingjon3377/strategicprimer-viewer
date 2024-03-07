package legacy.dbio;

import legacy.map.IFixture;
import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import legacy.map.Point;
import legacy.map.IMutableLegacyMap;

import legacy.map.fixtures.explorable.Portal;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBPortalHandler extends AbstractDatabaseWriter<Portal, Point> implements MapContentsReader {
	public DBPortalHandler() {
		super(Portal.class, Point.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
			Query.of("CREATE TABLE IF NOT EXISTS portals (" +
					"    row INTEGER NOT NULL," +
					"    column INTEGER NOT NULL," +
					"    id INTEGER NOT NULL," +
					"    image VARCHAR(255)," +
					"    destination_world VARCHAR(16)," +
					"    destination_row INTEGER," +
					"    destination_column INTEGER" +
					"        CHECK ((destination_row IS NOT NULL AND destination_column IS NOT NULL)" +
					"            OR (destination_row IS NULL AND destination_column IS NULL))" +
					");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL =
			Query.of("INSERT INTO portals (row, column, id, image, destination_world, " +
					"    destination_row, destination_column) " +
					"VALUES(:row, :column, :id, :image, :destination_world, :destination_row, :destination_column);");


	@Override
	public void write(final Transactional db, final Portal obj, final Point context) throws SQLException {
		final List<Param> params = new ArrayList<>();
		params.add(value("row", context.row()));
		params.add(value("column", context.column()));
		params.add(value("id", obj.getId()));
		params.add(value("image", obj.getImage()));
		params.add(value("destination_world", obj.getDestinationWorld()));
		if (obj.getDestinationCoordinates().isValid()) {
			params.add(value("destination_row", obj.getDestinationCoordinates().row()));
			params.add(value("destination_column", obj.getDestinationCoordinates().column()));
		}
		INSERT_SQL.on(params).execute(db.connection());
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readPortal(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String destinationWorld = (String) dbRow.get("destination_world");
			final Integer destinationRow = (Integer) dbRow.get("destination_row");
			final Integer destinationColumn = (Integer) dbRow.get("destination_column");
			final String image = (String) dbRow.get("image");
			final Portal portal = new Portal(Objects.requireNonNullElse(destinationWorld, "unknown"),
					new Point(Objects.requireNonNullElse(destinationRow, -1),
							Objects.requireNonNullElse(destinationColumn, -1)), id);
			if (!Objects.isNull(image)) {
				portal.setImage(image);
			}
			map.addFixture(new Point(row, column), portal);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM portals");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map, final Map<Integer, IFixture> containers,
								final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "portals", readPortal(map), SELECT);
	}
}
