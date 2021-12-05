package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;

import common.map.IMutableMapNG;
import common.map.TileFixture;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Quantity;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.xmlio.Warning;

final class DBResourcePileHandler
		extends AbstractDatabaseWriter<IResourcePile, /*IUnit|IFortress*/TileFixture>
		implements MapContentsReader {
	public DBResourcePileHandler() {
		super(IResourcePile.class, TileFixture.class);
	}

	@Override
	public boolean canWrite(Object obj, Object context) {
		return obj instanceof IResourcePile &&
			(context instanceof IFortress || context instanceof IUnit);
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
		"CREATE TABLE IF NOT EXISTS resource_piles (" +
			"    parent INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    kind VARCHAR(64) NOT NULL," +
			"    contents VARCHAR(64) NOT NULL," +
			"    quantity VARCHAR(128) NOT NULL" +
			"        CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%')," +
			"    units VARCHAR(32) NOT NULL," +
			"    created INTEGER," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO resource_piles (parent, id, kind, contents, quantity, units, created, image) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void write(DB db, IResourcePile obj, TileFixture context) {
		db.update(INSERT_SQL, context.getId(), obj.getId(), obj.getKind(), obj.getContents(),
			obj.getQuantity().getNumber().toString(), obj.getQuantity().getUnits(),
			obj.getCreated(), obj.getImage()).execute();
	}

	@Override
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readResourcePile(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int parentId = (Integer) dbRow.get("parent");
			TileFixture parent = (TileFixture) findById(map, parentId, warner);
			int id = (Integer) dbRow.get("id");
			String kind = (String) dbRow.get("kind");
			String contents = (String) dbRow.get("contents");
			String qtyString = (String) dbRow.get("quantity");
			String units = (String) dbRow.get("units");
			Integer created = (Integer) dbRow.get("created");
			String image = (String) dbRow.get("image");
			Number quantity;
			try {
				quantity = Integer.parseInt(qtyString);
			} catch (NumberFormatException except) {
				quantity = new BigDecimal(qtyString);
			}
			IMutableResourcePile pile = new ResourcePileImpl(id, kind, contents,
				new Quantity(quantity, units));
			if (image != null) {
				pile.setImage(image);
			}
			if (created != null) {
				pile.setCreated(created);
			}
			if (parent instanceof IMutableUnit) {
				((IMutableUnit) parent).addMember(pile);
			} else if (parent instanceof IMutableFortress) {
				((IMutableFortress) parent).addMember(pile);
			} else {
				throw new IllegalArgumentException("parent must be unit or fortress");
			}
		};
	}

	@Override
	public void readExtraMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "resource piles", readResourcePile(map),
				"SELECT * FROM resource_piles");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
