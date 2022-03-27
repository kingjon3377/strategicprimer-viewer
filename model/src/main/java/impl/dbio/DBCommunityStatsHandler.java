package impl.dbio;

import common.map.IFixture;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
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

import static io.jenetics.facilejdbc.Param.value;

final class DBCommunityStatsHandler extends AbstractDatabaseWriter<CommunityStats, ITownFixture>
		implements MapContentsReader {
	public DBCommunityStatsHandler() {
		super(CommunityStats.class, ITownFixture.class);
	}

	private static final List<Query> INITIALIZERS = List.of(
			Query.of("CREATE TABLE IF NOT EXISTS town_expertise (" +
					         "    town INTEGER NOT NULL," +
					         "    skill VARCHAR(255) NOT NULL," +
					         "    level INTEGER NOT NULL" +
					         ");"),
			Query.of("CREATE TABLE IF NOT EXISTS town_worked_resources (" +
					         "    town INTEGER NOT NULL," +
					         "    resource INTEGER NOT NULL" +
					         ");"),
			Query.of("CREATE TABLE IF NOT EXISTS town_production (" +
					         "    town INTEGER NOT NULL," +
					         "    id INTEGER NOT NULL," +
					         "    kind VARCHAR(64) NOT NULL," +
					         "    contents VARCHAR(64) NOT NULL," +
					         "    quantity VARCHAR(128) NOT NULL" +
					         "        CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%')," +
					         "    units VARCHAR(32) NOT NULL," +
					         "    created INTEGER" +
					         ");"),
			Query.of("CREATE TABLE IF NOT EXISTS town_consumption (" +
					         "    town INTEGER NOT NULL," +
					         "    id INTEGER NOT NULL," +
					         "    kind VARCHAR(64) NOT NULL," +
					         "    contents VARCHAR(64) NOT NULL," +
					         "    quantity VARCHAR(128) NOT NULL" +
					         "        CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%')," +
					         "    units VARCHAR(32) NOT NULL," +
					         "    created INTEGER" +
					         ");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_EXPERTISE =
		Query.of("INSERT INTO town_expertise (town, skill, level) VALUES(:town, :skill, :level);");

	private static final Query INSERT_FIELDS =
		Query.of("INSERT INTO town_worked_resources (town, resource) VALUES(:town, :resource);");

	private static final Query INSERT_PRODUCTION =
		Query.of("INSERT INTO town_production(town, id, kind, contents, quantity, units, created) " +
			"VALUES(:town, :id, :kind, :contents, :quantity, :units, :created);");

	private static final Query INSERT_CONSUMPTION =
		Query.of("INSERT INTO town_consumption(town, id, kind, contents, quantity, units, created) " +
			"VALUES(:town, :id, :kind, :contents, :quantity, :units, :created);");

	@Override
	public void write(final Transactional db, final CommunityStats obj, final ITownFixture context) throws SQLException {
		db.transaction().accept(sql -> {
				// TODO: Use batch mode
				for (final Map.Entry<String, Integer> entry :
						obj.getHighestSkillLevels().entrySet()) {
					INSERT_EXPERTISE.on(value("town", context.getId()), value("skill", entry.getKey()),
							value("level", entry.getValue())).execute(sql);
				}
				for (final Integer field : obj.getWorkedFields()) {
					INSERT_FIELDS.on(value("town", context.getId()), value("resource", field)).execute(sql);
				}
				for (final IResourcePile resource : obj.getYearlyProduction()) {
					INSERT_PRODUCTION.on(value("town", context.getId()), value("id", resource.getId()),
							value("kind", resource.getKind()), value("contents", resource.getContents()),
							value("quantity", resource.getQuantity().getNumber().toString()),
							value("units", resource.getQuantity().getUnits()),
							value("created", resource.getCreated())).execute(sql);
				}
				for (final IResourcePile resource : obj.getYearlyConsumption()) {
					INSERT_CONSUMPTION.on(value("town", context.getId()), value("id", resource.getId()),
							value("kind", resource.getKind()), value("contents", resource.getContents()),
							value("quantity", resource.getQuantity().getNumber().toString()),
							value("units", resource.getQuantity().getUnits()),
							value("created", resource.getCreated())).execute(sql);
				}
			});
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readTownPopulations(final IMutableMapNG map,
			final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final int id = (Integer) dbRow.get("id");
			final int population = (Integer) dbRow.get("population");
			if (!containees.containsKey(id) || containees.get(id).stream().noneMatch(CommunityStats.class::isInstance)) {
				multimapPut(containees, id, new CommunityStats(population));
			}
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readTownExpertise(final IMutableMapNG map,
			final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final int townId = (Integer) dbRow.get("town");
			final CommunityStats population = containees.get(townId).stream().filter(CommunityStats.class::isInstance)
					.map(CommunityStats.class::cast).findAny().orElse(null);
			final String skill = (String) dbRow.get("skill");
			final int level = (Integer) dbRow.get("level");
			assert population != null;
			population.setSkillLevel(skill, level);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readWorkedResource(final IMutableMapNG map,
			final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final int townId = (Integer) dbRow.get("town");
			final CommunityStats population = containees.get(townId).stream().filter(CommunityStats.class::isInstance)
					.map(CommunityStats.class::cast).findAny().orElse(null);
			final int resource = (Integer) dbRow.get("resource");
			assert population != null;
			population.addWorkedField(resource);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException>
			readProducedResource(final IMutableMapNG map, final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final int townId = (Integer) dbRow.get("town");
			final CommunityStats population = containees.get(townId).stream().filter(CommunityStats.class::isInstance)
					.map(CommunityStats.class::cast).findAny().orElse(null);
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

	private TryBiConsumer<Map<String, Object>, Warning, SQLException>
			readConsumedResource(final IMutableMapNG map, final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final int townId = (Integer) dbRow.get("town");
			final CommunityStats population = containees.get(townId).stream().filter(CommunityStats.class::isInstance)
					.map(CommunityStats.class::cast).findAny().orElse(null);
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

	private static final Query SELECT_EXPERTISE = Query.of("SELECT * FROM town_expertise");
	private static final Query SELECT_WORKED = Query.of("SELECT * FROM town_worked_resources");
	private static final Query SELECT_PRODUCTION = Query.of("SELECT * FROM town_production");
	private static final Query SELECT_CONSUMPTION = Query.of("SELECT * FROM town_consumption");
	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "town expertise levels",
			readTownExpertise(map, containees), SELECT_EXPERTISE);
		handleQueryResults(db, warner, "town worked resources",
			readWorkedResource(map, containees), SELECT_WORKED);
		handleQueryResults(db, warner, "town produced resources",
			readProducedResource(map, containees), SELECT_PRODUCTION);
		handleQueryResults(db, warner, "town consumed resources",
			readConsumedResource(map, containees), SELECT_CONSUMPTION);
	}
}
