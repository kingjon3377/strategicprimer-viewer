package impl.dbio;

import common.map.IFixture;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.regex.Pattern;

import common.map.IMutableMapNG;
import common.map.Point;
import common.map.HasKind;
import common.map.HasImage;
import common.map.HasMutableImage;
import common.map.fixtures.mobile.Immortal;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.SimpleImmortal;
import common.map.fixtures.mobile.Centaur;
import common.map.fixtures.mobile.Dragon;
import common.map.fixtures.mobile.Fairy;
import common.map.fixtures.mobile.Giant;
import common.map.fixtures.mobile.Sphinx;
import common.map.fixtures.mobile.Djinn;
import common.map.fixtures.mobile.Griffin;
import common.map.fixtures.mobile.Minotaur;
import common.map.fixtures.mobile.Ogre;
import common.map.fixtures.mobile.Phoenix;
import common.map.fixtures.mobile.Simurgh;
import common.map.fixtures.mobile.Troll;
import common.map.fixtures.mobile.ImmortalAnimal;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

final class DBImmortalHandler extends AbstractDatabaseWriter<Immortal, /*Point|IUnit*/ Object>
		implements MapContentsReader {
	private static final Pattern SIMPLE_IMMORTAL_REFRESH_PATTERN = Pattern.compile("simple_immortals ");

	public DBImmortalHandler() {
		super(Immortal.class, Object.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return obj instanceof Immortal && (context instanceof Point || context instanceof IUnit);
	}

	private static final String SIMPLE_IMMORTALS_SCHEMA =
			"CREATE TABLE IF NOT EXISTS simple_immortals (" +
					"    row INTEGER," +
					"    column INTEGER" +
					"        CHECK ((row IS NOT NULL AND column IS NOT NULL)" +
					"            OR (row IS NULL AND column IS NULL))," +
					"    parent INTEGER" +
					"        CHECK ((row IS NOT NULL AND parent IS NULL)" +
					"            OR (row IS NULL AND parent IS NOT NULL))," +
					"    type VARCHAR(16) NOT NULL" +
					"        CHECK (type IN('sphinx', 'djinn', 'griffin', 'minotaur', 'ogre'," +
					"            'phoenix', 'simurgh', 'troll', 'snowbird', 'thunderbird'," +
					"            'pegasus', 'unicorn', 'kraken'))," +
					"    id INTEGER NOT NULL," +
					"    image VARCHAR(255)" +
					");";

	private static final List<Query> INITIALIZERS = List.of(
			Query.of(SIMPLE_IMMORTALS_SCHEMA),
			Query.of("CREATE TABLE IF NOT EXISTS kinded_immortals (" +
					         "    row INTEGER," +
					         "    column INTEGER" +
					         "        CHECK ((row IS NOT NULL AND column IS NOT NULL)" +
					         "            OR (row IS NULL AND column IS NULL))," +
					         "    parent INTEGER" +
					         "        CHECK ((row IS NOT NULL AND parent IS NULL)" +
					         "            OR (row IS NULL AND parent IS NOT NULL))," +
					         "    type VARCHAR(16) NOT NULL" +
					         "        CHECK (type IN ('centaur', 'dragon', 'fairy', 'giant'))," +
					         "    kind VARCHAR(32) NOT NULL," +
					         "    id INTEGER NOT NULL," +
					         "    image VARCHAR(255)" +
					         ");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static boolean containsSimpleImmortals(final String s) {
		return s.contains("simple_immortals");
	}

	private static final List<Query> SIMPLE_IMMORTAL_REFRESH =
			Collections.singletonList(Query.of(SIMPLE_IMMORTAL_REFRESH_PATTERN.matcher(SIMPLE_IMMORTALS_SCHEMA).replaceAll("simple_immortals_replacement ") +
					         "INSERT INTO simple_immortals_replacement SELECT * FROM simple_immortals;" +
					         "DROP TABLE simple_immortals;" + "ALTER TABLE simple_immortals_replacement RENAME TO simple_immortals;"));
	private static List<Query> refreshSimpleSchema() {
		return SIMPLE_IMMORTAL_REFRESH;
	}

	private static final Query INSERT_SIMPLE = Query.of(
		"INSERT INTO simple_immortals (row, column, parent, type, id, image) " +
			"VALUES(:row, :column, :parent, :type, :id, :image);");

	private static final Query INSERT_KINDED = Query.of(
		"INSERT INTO kinded_immortals (row, column, parent, type, kind, id, image) " +
			"VALUES(:row, :column, :parent, :type, :kind, :id, :image);");

	@Override
	public void write(final Transactional db, final Immortal obj, final Object context) throws SQLException {
		if (obj instanceof SimpleImmortal || obj instanceof ImmortalAnimal) {
			try {
				if (context instanceof Point p) {
					INSERT_SIMPLE.on(value("row", p.row()),
						value("column", p.column()),
						value("type", ((HasKind) obj).getKind()), value("id", obj.getId()),
						value("image", ((HasImage) obj).getImage())).executeUpdate(db.connection());
				} else if (context instanceof IUnit u) {
					INSERT_SIMPLE.on(
							value("parent", u.getId()), value("type", ((HasKind) obj).getKind()),
							value("id", obj.getId()), value("image", ((HasImage) obj).getImage())).execute(db.connection());
				} else {
					throw new IllegalArgumentException("context must be Point or IUnit");
				}
			} catch (final SQLException except) {
				if (except.getMessage().contains("constraint failed: simple_immortals)")) {
					db.transaction().accept(sql -> {
						for (final Query query : refreshSimpleSchema()) {
							query.execute(sql);
						}
					});
					write(db, obj, context);
				} else {
					throw except;
				}
			}
		} else {
			final String type;
			if (obj instanceof Centaur) {
				type = "centaur";
			} else if (obj instanceof Dragon) {
				type = "dragon";
			} else if (obj instanceof Fairy) {
				type = "fairy";
			} else if (obj instanceof Giant) {
				type = "giant";
			} else {
				throw new IllegalArgumentException("Unexpected immortal type");
			}
			if (context instanceof Point p) {
				INSERT_KINDED.on(value("row", p.row()),
						value("column", p.column()),
						value("type", type), value("kind", ((HasKind) obj).getKind()),
						value("id", obj.getId()), value("image", ((HasImage) obj).getImage())).execute(db.connection());
			} else if (context instanceof IUnit u) {
				INSERT_KINDED.on(
						value("parent", u.getId()), value("type", type),
						value("kind", ((HasKind) obj).getKind()), value("id", obj.getId()),
						value("image", ((HasImage) obj).getImage())).execute(db.connection());
			} else {
				throw new IllegalArgumentException("context must be Point or IUnit");
			}
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException>
			readSimpleImmortal(final IMutableMapNG map, final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final String type = (String) dbRow.get("type");
			final int id = (Integer) dbRow.get("id");
			final String image = (String) dbRow.get("image");
			final Immortal immortal = switch (type) {
				case "sphinx" -> new Sphinx(id);
				case "djinn" -> new Djinn(id);
				case "griffin" -> new Griffin(id);
				case "minotaur" -> new Minotaur(id);
				case "ogre" -> new Ogre(id);
				case "phoenix" -> new Phoenix(id);
				case "simurgh" -> new Simurgh(id);
				case "troll" -> new Troll(id);
				default -> ImmortalAnimal.parse(type).apply(id);
			};
			if (image != null) {
				((HasMutableImage) immortal).setImage(image);
			}
			final Integer row = (Integer) dbRow.get("row");
			final Integer column = (Integer) dbRow.get("column");
			final Integer parentId = (Integer) dbRow.get("parent");
			if (row != null && column != null) {
				map.addFixture(new Point(row, column), immortal);
			} else {
				multimapPut(containees, parentId, immortal);
			}
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException>
			readKindedImmortal(final IMutableMapNG map, final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final String type = (String) dbRow.get("type");
			final String kind = (String) dbRow.get("kind");
			final int id = (Integer) dbRow.get("id");
			final String image = (String) dbRow.get("image");
			final Immortal immortal = switch (type) {
				case "centaur" -> new Centaur(kind, id);
				case "dragon" -> new Dragon(kind, id);
				case "fairy" -> new Fairy(kind, id);
				case "giant" -> new Giant(kind, id);
				default -> throw new IllegalArgumentException("Unexpected immortal kind");
			};
			if (image != null) {
				((HasMutableImage) immortal).setImage(image);
			}
			final Integer row = (Integer) dbRow.get("row");
			final Integer column = (Integer) dbRow.get("column");
			final Integer parentId = (Integer) dbRow.get("parent");
			if (row != null && column != null) {
				map.addFixture(new Point(row, column), immortal);
			} else {
				multimapPut(containees, parentId, immortal);
			}
		};
	}

	private static final Query SIMPLE_SELECT = Query.of("SELECT * FROM simple_immortals");
	private static final Query KINDED_SELECT = Query.of("SELECT * FROM kinded_immortals");
	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "simple immortals", readSimpleImmortal(map, containees),
			SIMPLE_SELECT);
		handleQueryResults(db, warner, "immortals with kinds", readKindedImmortal(map, containees),
			KINDED_SELECT);
	}
}
