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
import legacy.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.FieldStatus;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBFieldHandler extends AbstractDatabaseWriter<Meadow, Point> implements MapContentsReader {
	public DBFieldHandler() {
		super(Meadow.class, Point.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
			Query.of("CREATE TABLE IF NOT EXISTS fields (" +
					"    row INTEGER NOT NULL," +
					"    column INTEGER NOT NULL," +
					"    id INTEGER NOT NULL," +
					"    type VARCHAR(6) NOT NULL" +
					"        CHECK (type IN ('field', 'meadow'))," +
					"    kind VARCHAR(64) NOT NULL," +
					"    cultivated BOOLEAN NOT NULL," +
					"    status VARCHAR(7) NOT NULL" +
					"        CHECK (status IN ('fallow', 'seeding', 'growing', 'bearing'))," +
					"    acres VARCHAR(128)" +
					"        CHECK (acres NOT LIKE '%[^0-9.]%' AND acres NOT LIKE '%.%.%')," +
					"    image VARCHAR(255)" +
					");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL =
			Query.of("INSERT INTO fields (row, column, id, type, kind, cultivated, status, acres, image) " +
					"VALUES(:row, :column, :id, :type, :kind, :cultivated, :status, :acres, :image);");

	@Override
	public void write(final Transactional db, final Meadow obj, final Point context) throws SQLException {
		INSERT_SQL.on(value("row", context.row()), value("column", context.column()),
				value("id", obj.getId()), value("type", obj.getType().toString()),
				value("kind", obj.getKind()), value("cultivated", obj.getCultivation() == CultivationStatus.CULTIVATED),
				value("status", obj.getStatus().toString()), value("acres", obj.getAcres().toString()),
				value("image", obj.getImage())).execute(db.connection());
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readMeadow(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final int id = (Integer) dbRow.get("id");
			final Meadow.MeadowType type;
			try {
				type = Meadow.MeadowType.parse((String) dbRow.get("type"));
			} catch (ParseException except) {
				throw new IllegalArgumentException(except);
			}
			final String kind = (String) dbRow.get("kind");
			final CultivationStatus cultivation = getBooleanValue(dbRow, "cultivated") ?
					CultivationStatus.CULTIVATED : CultivationStatus.WILD;
			final FieldStatus status = FieldStatus.parse((String) dbRow.get("status"));
			final String image = (String) dbRow.get("image");
			final Number acres = parseNumber((String) dbRow.get("acres"));
			final Meadow meadow = new Meadow(kind, type, cultivation, id, status, acres);
			if (Objects.nonNull(image)) {
				meadow.setImage(image);
			}
			map.addFixture(new Point(row, column), meadow);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM fields");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map,
	                            final Map<Integer, IFixture> containers, final Map<Integer, List<Object>> containees,
	                            final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "meadows", readMeadow(map), SELECT);
	}
}
