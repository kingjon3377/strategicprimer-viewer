package legacy.dbio;

import legacy.map.IFixture;
import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import legacy.map.Point;
import legacy.map.IMutableLegacyMap;
import legacy.map.fixtures.resources.CultivationStatus;
import legacy.map.fixtures.resources.Grove;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBGroveHandler extends AbstractDatabaseWriter<Grove, Point> implements MapContentsReader {
	public DBGroveHandler() {
		super(Grove.class, Point.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
			Query.of("CREATE TABLE IF NOT EXISTS groves (" +
					"    row INTEGER NOT NULL," +
					"    column INTEGER NOT NULL," +
					"    id INTEGER NOT NULL," +
					"    type VARCHAR(7) NOT NULL" +
					"        CHECK (type IN ('grove', 'orchard'))," +
					"    kind VARCHAR(64) NOT NULL," +
					"    cultivated BOOLEAN NOT NULL," +
					"    count INTEGER NOT NULL," +
					"    image VARCHAR(255)" +
					");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL = Query.of(
			"INSERT INTO groves (row, column, id, type, kind, cultivated, count, image) " +
					"VALUES(:row, :column, :id, :type, :kind, :cultivated, :count, :image);");

	@Override
	public void write(final Transactional db, final Grove obj, final Point context) throws SQLException {
		INSERT_SQL.on(value("row", context.row()), value("column", context.column()),
				value("id", obj.getId()), value("type", obj.getType().toString()),
				value("kind", obj.getKind()), value("cultivated", obj.getCultivation() == CultivationStatus.CULTIVATED),
				value("count", obj.getPopulation()), value("image", obj.getImage())).execute(db.connection());
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readGrove(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final boolean cultivated = getBooleanValue(dbRow, "cultivated");
			final int count = (Integer) dbRow.get("count");
			final String image = (String) dbRow.get("image");
			final Grove.GroveType type;
			try {
				type = Grove.GroveType.parse((String) dbRow.get("type"));
			} catch (final ParseException except) {
				throw new IllegalArgumentException(except);
			}
			final Grove grove = new Grove(type, cultivated ? CultivationStatus.CULTIVATED : CultivationStatus.WILD,
					kind, id, count);
			if (!Objects.isNull(image)) {
				grove.setImage(image);
			}
			map.addFixture(new Point(row, column), grove);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM groves");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map,
	                            final Map<Integer, IFixture> containers, final Map<Integer, List<Object>> containees,
	                            final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "groves", readGrove(map), SELECT);
	}
}
