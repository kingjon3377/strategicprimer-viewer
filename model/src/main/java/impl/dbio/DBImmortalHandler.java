package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import common.map.IMutableMapNG;
import common.map.Point;
import common.map.HasKind;
import common.map.HasImage;
import common.map.HasMutableImage;
import common.map.fixtures.mobile.Immortal;
import common.map.fixtures.mobile.IMutableUnit;
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

final class DBImmortalHandler extends AbstractDatabaseWriter<Immortal, /*Point|IUnit*/ Object>
		implements MapContentsReader {
	public DBImmortalHandler() {
		super(Immortal.class, Object.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return obj instanceof Immortal && (context instanceof Point || context instanceof IUnit);
	}

	private static final List<String> INITIALIZERS = List.of("CREATE TABLE IF NOT EXISTS simple_immortals (" +
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
			                                                         ");", "CREATE TABLE IF NOT EXISTS kinded_immortals (" +
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
					                                                               ");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static boolean containsSimpleImmortals(final String s) {
		return s.contains("simple_immortals");
	}

	private static List<String> refreshSimpleSchema() {
		return INITIALIZERS.stream().filter(DBImmortalHandler::containsSimpleImmortals)
			.map(s -> s.replaceAll("simple_immortals ", "simple_immortals_replacement "))
			.map(s -> s +
				"INSERT INTO simple_immortals_replacement SELECT * FROM simple_immortals;")
			.map(s -> s + "DROP TABLE simple_immortals;")
			.map(s -> s + "ALTER TABLE simple_immortals_replacement RENAME TO simple_immortals;")
			.collect(Collectors.toList());
	}

	private static final String INSERT_SIMPLE =
		"INSERT INTO simple_immortals (row, column, parent, type, id, image) " +
			"VALUES(?, ?, ?, ?, ?, ?);";

	private static final String INSERT_KINDED =
		"INSERT INTO kinded_immortals (row, column, parent, type, kind, id, image) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?);";
	@Override
	public void write(final DB db, final Immortal obj, final Object context) {
		if (obj instanceof SimpleImmortal || obj instanceof ImmortalAnimal) {
			try {
				if (context instanceof Point) {
					db.update(INSERT_SIMPLE, ((Point) context).getRow(),
						((Point) context).getColumn(), null,
						((HasKind) obj).getKind(), obj.getId(), obj.getId(),
						((HasImage) obj).getImage()).execute();
				} else if (context instanceof IUnit) {
					db.update(INSERT_SIMPLE, null, null, ((IUnit) context).getId(),
						((HasKind) obj).getKind(), obj.getId(),
						((HasImage) obj).getImage()).execute();
				} else {
					throw new IllegalArgumentException("context must be Point or IUnit");
				}
			} catch (final Exception except) {
				if (except.getMessage().contains("constraint failed: simple_immortals)")) {
					db.transaction(sql -> {
						for (final String initializer : refreshSimpleSchema()) {
							sql.script(initializer).execute();
						}
						return true;
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
			if (context instanceof Point) {
				db.update(INSERT_KINDED, ((Point) context).getRow(),
					((Point) context).getColumn(), null, type, ((HasKind) obj).getKind(),
					obj.getId(), ((HasImage) obj).getImage()).execute();
			} else if (context instanceof IUnit) {
				db.update(INSERT_KINDED, null, null, ((IUnit) context).getId(), type,
					((HasKind) obj).getKind(), obj.getId(),
					((HasImage) obj).getImage()).execute();
			} else {
				throw new IllegalArgumentException("context must be Point or IUnit");
			}
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception>
			readSimpleImmortal(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final String type = (String) dbRow.get("type");
			final int id = (Integer) dbRow.get("id");
			final String image = (String) dbRow.get("image");
			final Immortal immortal;
			switch (type) {
			case "sphinx":
				immortal = new Sphinx(id);
				break;
			case "djinn":
				immortal = new Djinn(id);
				break;
			case "griffin":
				immortal = new Griffin(id);
				break;
			case "minotaur":
				immortal = new Minotaur(id);
				break;
			case "ogre":
				immortal = new Ogre(id);
				break;
			case "phoenix":
				immortal = new Phoenix(id);
				break;
			case "simurgh":
				immortal = new Simurgh(id);
				break;
			case "troll":
				immortal = new Troll(id);
				break;
			default:
				immortal = ImmortalAnimal.parse(type).apply(id);
			}
			if (image != null) {
				((HasMutableImage) immortal).setImage(image);
			}
			final Integer row = (Integer) dbRow.get("row");
			final Integer column = (Integer) dbRow.get("column");
			final Integer parentId = (Integer) dbRow.get("parent");
			if (row != null && column != null) {
				map.addFixture(new Point(row, column), immortal);
			} else {
				final IMutableUnit parent = (IMutableUnit) findById(map, parentId, warner);
				parent.addMember(immortal);
			}
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception>
			readKindedImmortal(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final String type = (String) dbRow.get("type");
			final String kind = (String) dbRow.get("kind");
			final int id = (Integer) dbRow.get("id");
			final String image = (String) dbRow.get("image");
			final Immortal immortal;
			switch (type) {
			case "centaur":
				immortal = new Centaur(kind, id);
				break;
			case "dragon":
				immortal = new Dragon(kind, id);
				break;
			case "fairy":
				immortal = new Fairy(kind, id);
				break;
			case "giant":
				immortal = new Giant(kind, id);
				break;
			default:
				throw new IllegalArgumentException("Unexpected immortal kind");
			}
			if (image != null) {
				((HasMutableImage) immortal).setImage(image);
			}
			final Integer row = (Integer) dbRow.get("row");
			final Integer column = (Integer) dbRow.get("column");
			final Integer parentId = (Integer) dbRow.get("parent");
			if (row != null && column != null) {
				map.addFixture(new Point(row, column), immortal);
			} else {
				final IMutableUnit parent = (IMutableUnit) findById(map, parentId, warner);
				parent.addMember(immortal);
			}
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "simple immortals", readSimpleImmortal(map),
				"SELECT * FROM simple_immortals WHERE row IS NOT NULL");
			handleQueryResults(db, warner, "immortals with kinds", readKindedImmortal(map),
				"SELECT * FROM kinded_immortals WHERE row IS NOT NULL");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}

	@Override
	public void readExtraMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "simple immortals in units",
				readSimpleImmortal(map),
				"SELECT * FROM simple_immortals WHERE parent IS NOT NULL");
			handleQueryResults(db, warner, "immortals with kinds in units",
				readKindedImmortal(map),
				"SELECT * FROM kinded_immortals WHERE parent IS NOT NULL");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
