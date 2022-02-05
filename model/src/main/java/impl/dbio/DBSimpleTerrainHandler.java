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
import common.map.HasImage;
import common.map.HasMutableImage;
import common.map.Point;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Oasis;
import common.xmlio.Warning;
import common.map.fixtures.TerrainFixture;

final class DBSimpleTerrainHandler extends AbstractDatabaseWriter<TerrainFixture, Point>
		implements MapContentsReader {
	public DBSimpleTerrainHandler() {
		super(TerrainFixture.class, Point.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return (obj instanceof Hill || obj instanceof Oasis) && context instanceof Point;
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
		"CREATE TABLE IF NOT EXISTS simple_terrain (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    type VARCHAR(7) NOT NULL" +
			"        CHECK(type IN('hill', 'oasis', 'sandbar'))," +
			"    id INTEGER NOT NULL," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	@Override
	public void write(final DB db, final TerrainFixture obj, final Point context) {
		final String type;
		if (obj instanceof Hill) {
			type = "hill";
		} else if (obj instanceof Oasis) {
			type = "oasis";
		} else {
			throw new IllegalArgumentException("Unhandled terrain fixture type");
		}
		db.update("INSERT INTO simple_terrain (row, column, type, id, image) VALUES(?, ?, ?, ?, ?);",
			context.getRow(), context.getColumn(), type, obj.getId(),
			((HasImage) obj).getImage()).execute();
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readSimpleTerrain(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			String type = (String) dbRow.get("type");
			int id = (Integer) dbRow.get("id");
			String image = (String) dbRow.get("image");
			TerrainFixture fixture;
			switch (type) {
			case "hill":
				fixture = new Hill(id);
				break;
			case "sandbar":
				log.info("Ignoring 'sandbar' with ID ``id```");
				return;
			case "oasis":
				fixture = new Oasis(id);
				break;
			default:
				throw new IllegalArgumentException("Unhandled simple terrain-fixture type");
			}
			if (image != null) {
				((HasMutableImage) fixture).setImage(image);
			}
			map.addFixture(new Point(row, column), fixture);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "simple terrain fixtures",
				readSimpleTerrain(map), "SELECT * FROM simple_terrain");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
