package legacy.dbio;

import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import legacy.map.HasMutableImage;
import legacy.map.IMutableLegacyMap;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.towns.IFortress;
import common.xmlio.Warning;
import legacy.map.IFixture;

import static io.jenetics.facilejdbc.Param.value;

public final class DBImplementHandler extends AbstractDatabaseWriter<Implement, /*IUnit|IFortress|IWorker*/IFixture>
		implements MapContentsReader {
	public DBImplementHandler() {
		super(Implement.class, IFixture.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return obj instanceof Implement && (context instanceof IFortress || context instanceof IUnit);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
			Query.of("CREATE TABLE IF NOT EXISTS implements (" +
					"    parent INTEGER NOT NULL," +
					"    id INTEGER NOT NULL," +
					"    kind VARCHAR(255) NOT NULL," +
					"    count INTEGER NOT NULL DEFAULT 1," +
					"    image VARCHAR(255)" +
					");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL =
			Query.of("INSERT INTO implements (parent, id, kind, count, image) " +
					"VALUES(:parent, :id, :kind, :count, :image);");

	@Override
	public void write(final Transactional db, final Implement obj, final IFixture context) throws SQLException {
		INSERT_SQL.on(value("parent", context.getId()), value("id", obj.getId()),
				value("kind", obj.getKind()), value("count", obj.getCount()),
				value("image", obj.getImage())).execute(db.connection());
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readImplement(
			final IMutableLegacyMap map, final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final int parentId = (Integer) dbRow.get("parent");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final int count = (Integer) dbRow.get("count");
			final String image = (String) dbRow.get("image");
			final HasMutableImage implement = new Implement(kind, id, count);
			if (!Objects.isNull(image)) {
				implement.setImage(image);
			}
			multimapPut(containees, parentId, implement);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM implements");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map,
	                            final Map<Integer, IFixture> containers, final Map<Integer, List<Object>> containees,
	                            final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "pieces of equipment", readImplement(map, containees), SELECT);
	}
}
