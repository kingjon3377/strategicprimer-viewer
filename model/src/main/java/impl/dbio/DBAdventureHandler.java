package impl.dbio;

import common.map.IFixture;
import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.explorable.AdventureFixture;
import common.xmlio.Warning;

import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.jenetics.facilejdbc.Param.value;

final class DBAdventureHandler extends AbstractDatabaseWriter<AdventureFixture, Point>
		implements MapContentsReader {
	public DBAdventureHandler() {
		super(AdventureFixture.class, Point.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
		Query.of("CREATE TABLE IF NOT EXISTS adventures (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    id INTEGER NOT NULL," +
			"    brief VARCHAR(255) NOT NULL," +
			"    full VARCHAR(512) NOT NULL," +
			"    owner INTEGER NOT NULL," +
			"    image VARCHAR(255)" +
			");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_QUERY = Query.of(
		"INSERT INTO adventures (row, column, id, brief, full, owner, image) " +
			"VALUES(:row, :column, :id, :brief, :full, :owner, :image);");

	@Override
	public void write(final Transactional db, final AdventureFixture obj, final Point context) throws SQLException {
		INSERT_QUERY.on(value("row", context.row()), value("column", context.column()),
				value("id", obj.getId()), value("brief", obj.getBriefDescription()),
				value("full", obj.getFullDescription()), value("owner", obj.owner().getPlayerId()),
				value("image", obj.getImage())).execute(db.connection());
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException>
			readAdventure(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
//			final int row = Integer.parseInt(dbRow.get("row"));
			final int column = (Integer) dbRow.get("column");
//			final int column = Integer.parseInt(dbRow.get("column"));
			final int id = (Integer) dbRow.get("id");
//			final int id = Integer.parseInt(dbRow.get("id"));
			final String brief = (String) dbRow.get("brief");
			final String full = (String) dbRow.get("full");
			final int ownerId = (Integer) dbRow.get("owner");
//			final int ownerId = Integer.parseInt(dbRow.get("owner"));
			final String image = (String) dbRow.get("image");
			final AdventureFixture adventure = new AdventureFixture(map.getPlayers().getPlayer(ownerId),
				brief, full, id);
			if (image != null) {
				adventure.setImage(image);
			}
			map.addFixture(new Point(row, column), adventure);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM adventures");
	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "adventures", readAdventure(map), SELECT);
	}
}
