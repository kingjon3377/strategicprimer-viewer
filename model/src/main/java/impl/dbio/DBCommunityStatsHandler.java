package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import common.map.IMutableMapNG;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.towns.CommunityStats;
import common.map.fixtures.towns.ITownFixture;
import common.xmlio.Warning;

final class DBCommunityStatsHandler extends AbstractDatabaseWriter<CommunityStats, ITownFixture>
		implements MapContentsReader {
	public DBCommunityStatsHandler() {
		super(CommunityStats.class, ITownFixture.class);
	}

	private static final List<String> INITIALIZERS = List.of("CREATE TABLE IF NOT EXISTS town_expertise (" +
			                                                         "    town INTEGER NOT NULL," +
			                                                         "    skill VARCHAR(255) NOT NULL," +
			                                                         "    level INTEGER NOT NULL" +
			                                                         ");", "CREATE TABLE IF NOT EXISTS town_worked_resources (" +
					                                                               "    town INTEGER NOT NULL," +
					                                                               "    resource INTEGER NOT NULL" +
					                                                               ");", "CREATE TABLE IF NOT EXISTS town_production (" +
							                                                                     "    town INTEGER NOT NULL," +
							                                                                     "    id INTEGER NOT NULL," +
							                                                                     "    kind VARCHAR(64) NOT NULL," +
							                                                                     "    contents VARCHAR(64) NOT NULL," +
							                                                                     "    quantity VARCHAR(128) NOT NULL" +
							                                                                     "        CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%')," +
							                                                                     "    units VARCHAR(32) NOT NULL," +
							                                                                     "    created INTEGER" +
							                                                                     ");", "CREATE TABLE IF NOT EXISTS town_consumption (" +
									                                                                           "    town INTEGER NOT NULL," +
									                                                                           "    id INTEGER NOT NULL," +
									                                                                           "    kind VARCHAR(64) NOT NULL," +
									                                                                           "    contents VARCHAR(64) NOT NULL," +
									                                                                           "    quantity VARCHAR(128) NOT NULL" +
									                                                                           "        CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%')," +
									                                                                           "    units VARCHAR(32) NOT NULL," +
									                                                                           "    created INTEGER" +
									                                                                           ");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_EXPERTISE =
		"INSERT INTO town_expertise (town, skill, level) VALUES(?, ?, ?);";

	private static final String INSERT_FIELDS =
		"INSERT INTO town_worked_resources (town, resource) VALUES(?, ?);";

	private static final String INSERT_PRODUCTION =
		"INSERT INTO town_production(town, id, kind, contents, quantity, units, created) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?);";

	private static final String INSERT_CONSUMPTION =
		"INSERT INTO town_consumption(town, id, kind, contents, quantity, units, created) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final CommunityStats obj, final ITownFixture context) {
		db.transaction(sql -> {
				for (final Map.Entry<String, Integer> entry :
						obj.getHighestSkillLevels().entrySet()) {
					sql.update(INSERT_EXPERTISE, context.getId(), entry.getKey(),
						entry.getValue()).execute();
				}
				for (final Integer field : obj.getWorkedFields()) {
					sql.update(INSERT_FIELDS, context.getId(), field).execute();
				}
				for (final IResourcePile resource : obj.getYearlyProduction()) {
					sql.update(INSERT_PRODUCTION, context.getId(), resource.getId(),
							resource.getKind(), resource.getContents(),
							resource.getQuantity().getNumber().toString(),
							resource.getQuantity().getUnits(),
							resource.getCreated())
						.execute();
				}
				for (final IResourcePile resource : obj.getYearlyConsumption()) {
					sql.update(INSERT_CONSUMPTION, context.getId(), resource.getId(),
							resource.getKind(), resource.getContents(),
							resource.getQuantity().getNumber().toString(),
							resource.getQuantity().getUnits(),
							resource.getCreated())
						.execute();
				}
				return true;
			});
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readTownExpertise(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int townId = (Integer) dbRow.get("town");
			final ITownFixture town = (ITownFixture) findById(map, townId, warner);
			final CommunityStats population = town.getPopulation();
			final String skill = (String) dbRow.get("skill");
			final int level = (Integer) dbRow.get("level");
			assert population != null;
			population.setSkillLevel(skill, level);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readWorkedResource(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int townId = (Integer) dbRow.get("town");
			final ITownFixture town = (ITownFixture) findById(map, townId, warner);
			final CommunityStats population = town.getPopulation();
			final int resource = (Integer) dbRow.get("resource");
			assert population != null;
			population.addWorkedField(resource);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception>
			readProducedResource(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int townId = (Integer) dbRow.get("town");
			final ITownFixture town = (ITownFixture) findById(map, townId, warner);
			final CommunityStats population = town.getPopulation();
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final String contents = (String) dbRow.get("contents");
			final String qtyString = (String) dbRow.get("quantity");
			final String units = (String) dbRow.get("units");
			final Integer created = (Integer) dbRow.get("created");
			Number quantity;
			try {
				quantity = Integer.parseInt(qtyString);
			} catch (final NumberFormatException except) {
				quantity = new BigDecimal(qtyString);
			}
			final IMutableResourcePile pile =
				new ResourcePileImpl(id, kind, contents, new Quantity(quantity, units));
			if (created != null) {
				pile.setCreated(created);
			}
			assert population != null;
			population.getYearlyProduction().add(pile);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception>
			readConsumedResource(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int townId = (Integer) dbRow.get("town");
			final ITownFixture town = (ITownFixture) findById(map, townId, warner);
			final CommunityStats population = town.getPopulation();
			final int id = (Integer) dbRow.get("id");
			final String kind = (String) dbRow.get("kind");
			final String contents = (String) dbRow.get("contents");
			final String qtyString = (String) dbRow.get("quantity");
			final String units = (String) dbRow.get("units");
			final Integer created = (Integer) dbRow.get("created");
			// TODO: Extract method so this (and the identical pattern elsewhere) can be final
			Number quantity;
			try {
				quantity = Integer.parseInt(qtyString);
			} catch (final NumberFormatException except) {
				quantity = new BigDecimal(qtyString);
			}
			final IMutableResourcePile pile =
				new ResourcePileImpl(id, kind, contents, new Quantity(quantity, units));
			if (created != null) {
				pile.setCreated(created);
			}
			assert population != null;
			population.getYearlyConsumption().add(pile);
		};
	}

	@Override
	public void readExtraMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "town expertise levels",
				readTownExpertise(map), "SELECT * FROM town_expertise");
			handleQueryResults(db, warner, "town worked resources",
				readWorkedResource(map), "SELECT * FROM town_worked_resources");
			handleQueryResults(db, warner, "town produced resources",
				readProducedResource(map), "SELECT * FROM town_production");
			handleQueryResults(db, warner, "town consumed resources",
				readConsumedResource(map), "SELECT * FROM town_consumption");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
