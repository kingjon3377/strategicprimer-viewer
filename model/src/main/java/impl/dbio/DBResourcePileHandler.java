package impl.dbio;

import common.map.IFixture;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.math.BigDecimal;

import common.map.IMutableMapNG;
import common.map.TileFixture;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Quantity;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.towns.IFortress;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

final class DBResourcePileHandler
		extends AbstractDatabaseWriter<IResourcePile, /*IUnit|IFortress*/TileFixture>
		implements MapContentsReader {
	public DBResourcePileHandler() {
		super(IResourcePile.class, TileFixture.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return obj instanceof IResourcePile &&
			(context instanceof IFortress || context instanceof IUnit);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
		Query.of("CREATE TABLE IF NOT EXISTS resource_piles (" +
			"    parent INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(64) NOT NULL," +
			"    contents VARCHAR(64) NOT NULL," +
			"    quantity VARCHAR(128) NOT NULL" +
			"        CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%')," +
			"    units VARCHAR(32) NOT NULL," +
			"    created INTEGER," +
			"    image VARCHAR(255)" +
			");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL =
		Query.of("INSERT INTO resource_piles (parent, id, kind, contents, quantity, units, created, image) " +
			"VALUES(:parent, :id, :kind, :contents, :quantity, :units, :created, :image);");

	@Override
	public void write(final Transactional db, final IResourcePile obj, final TileFixture context) throws SQLException {
		INSERT_SQL.on(value("parent", context.getId()), value("id", obj.getId()), value("kind", obj.getKind()),
				value("contents", obj.getContents()), value("quantity", obj.getQuantity().number().toString()),
				value("units", obj.getQuantity().units()), value("created", obj.getCreated()),
				value("image", obj.getImage())).execute(db.connection());
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readResourcePile(final IMutableMapNG map,
			final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final int parentId = (Integer) dbRow.get("parent");
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final String contents = (String) dbRow.get("contents");
			final String qtyString = (String) dbRow.get("quantity");
			final String units = (String) dbRow.get("units");
			final Integer created = (Integer) dbRow.get("created");
			final String image = (String) dbRow.get("image");
			Number quantity;
			try {
				quantity = Integer.parseInt(qtyString);
			} catch (final NumberFormatException except) {
				quantity = new BigDecimal(qtyString);
			}
			final IMutableResourcePile pile = new ResourcePileImpl(id, kind, contents,
				new Quantity(quantity, units));
			if (image != null) {
				pile.setImage(image);
			}
			if (created != null) {
				pile.setCreated(created);
			}
			multimapPut(containees, parentId, pile);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM resource_piles");
	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "resource piles", readResourcePile(map, containees), SELECT);
	}
}
