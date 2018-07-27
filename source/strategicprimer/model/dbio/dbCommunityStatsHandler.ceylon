import ceylon.dbc {
	Sql,
	SqlNull
}
import ceylon.decimal {
	Decimal,
	parseDecimal
}

import strategicprimer.model.map {
	IMutableMapNG,
	IFixture,
	IMapNG
}
import strategicprimer.model.map.fixtures {
	ResourcePile,
	Quantity
}
import strategicprimer.model.map.fixtures.towns {
	CommunityStats,
	ITownFixture
}
import strategicprimer.model.xmlio {
	Warning
}
import ceylon.collection {
	MutableMap,
	HashMap
}
object dbCommunityStatsHandler
		extends AbstractDatabaseWriter<CommunityStats, ITownFixture>()
		satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS town_expertise (
			   town INTEGER NOT NULL,
			   skill VARCHAR(255) NOT NULL,
			   level INTEGER NOT NULL
		   );""",
		"""CREATE TABLE IF NOT EXISTS town_worked_resources (
			   town INTEGER NOT NULL,
			   resource INTEGER NOT NULL
		   );""",
		"""CREATE TABLE IF NOT EXISTS town_production (
			   town INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(64) NOT NULL,
			   contents VARCHAR(64) NOT NULL,
			   quantity VARCHAR(128) NOT NULL
				   CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%'),
			   units VARCHAR(32) NOT NULL,
			   created INTEGER
		   );""",
		"""CREATE TABLE IF NOT EXISTS town_consumption (
			   town INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(64) NOT NULL,
			   contents VARCHAR(64) NOT NULL,
			   quantity VARCHAR(128) NOT NULL
				   CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%'),
			   units VARCHAR(32) NOT NULL,
			   created INTEGER
		   );"""
	];
	MutableMap<[IMapNG, Integer], IFixture> cache = HashMap<[IMapNG, Integer], IFixture>();
	shared actual IFixture findById(IMapNG map, Integer id, Warning warner) { // TODO: Extract this from here and the superclass to a helper object, so other readers can make use of this memoization
		if (exists retval = cache[[map, id]]) {
			return retval;
		} else {
			value retval = super.findById(map, id, warner);
			cache[[map, id]] = retval;
			return retval;
		}
	}
	shared actual void write(Sql db, CommunityStats obj, ITownFixture context) {
		value expertise = db.Insert("""INSERT INTO town_expertise (town, skill, level)
		                               VALUES(?, ?, ?);""");
		value fields = db.Insert("""INSERT INTO town_worked_resources (town, resource)
		                            VALUES(?, ?);""");
		value production = db.Insert(
			"""INSERT INTO town_production(town, id, kind, contents, quantity, units, created)
			   VALUES(?, ?, ?, ?, ?, ?, ?);""");
		value consumption = db.Insert(
			"""INSERT INTO town_consumption(town, id, kind, contents, quantity, units, created)
			   VALUES(?, ?, ?, ?, ?, ?, ?);""");
		db.transaction(() {
			for (skill->level in obj.highestSkillLevels) {
				expertise.execute(context.id, skill, level);
			}
			for (field in obj.workedFields) {
				fields.execute(context.id, field);
			}
			for (resource in obj.yearlyProduction) {
				production.execute(context.id, resource.id, resource.kind, resource.contents,
					resource.quantity.number.string, resource.quantity.units, resource.created);
			}
			for (resource in obj.yearlyConsumption) {
				consumption.execute(context.id, resource.id, resource.kind, resource.contents,
					resource.quantity.number.string, resource.quantity.units, resource.created);
			}
			return true;
		});
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {}
	shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) {
		log.trace("Starting to read town population data");
		variable Integer count = 0;
		for (row in db.Select("""SELECT * FROM town_expertise""").Results()) {
			assert (is Integer townId = row["town"],
				is ITownFixture town = findById(map, townId, warner),
				exists population = town.population, is String skill = row["skill"],
				is Integer level = row["level"]);
			population.setSkillLevel(skill, level);
			count++;
			if (50.divides(count)) {
				log.trace("Finished reading ``count`` expertise levels");
			}
		}
		log.trace("Finished reading expertise levels, about to start worked resource sources");
		count = 0;
		for (row in db.Select("""SELECT * FROM town_worked_resources""").Results()) {
			assert (is Integer townId = row["town"],
				is ITownFixture town = findById(map, townId, warner),
				exists population = town.population, is Integer resource = row["resource"]);
			population.addWorkedField(resource);
			count++;
			if (50.divides(count)) {
				log.trace("Finished reading ``count`` worked resource sources");
			}
		}
		log.trace("Finished reading worked rsr sources, about to start produced resources");
		count = 0;
		for (row in db.Select("""SELECT * FROM town_production""").Results()) {
			assert (is Integer townId = row["town"],
				is ITownFixture town = findById(map, townId, warner),
				exists population = town.population, is Integer id = row["id"],
				is String kind = row["kind"], is String contents = row["contents"],
				is String qtyString = row["quantity"], is String units = row["units"],
				is Integer|SqlNull created = row["created"]);
			Number<out Anything> quantity;
			if (is Integer num = Integer.parse(qtyString)) {
				quantity = num;
			} else {
				assert (is Decimal num = parseDecimal(qtyString));
				quantity = num;
			}
			value pile = ResourcePile(id, kind, contents, Quantity(quantity, units));
			if (is Integer created) {
				pile.created = created;
			}
			population.yearlyProduction.add(pile);
			count++;
			if (50.divides(count)) {
				log.trace("Finished reading ``count`` produced resources");
			}
		}
		log.trace("Finished reading produced resources, about to start on consumed resources");
		count = 0;
		for (row in db.Select("""SELECT * FROM town_consumption""").Results()) {
			assert (is Integer townId = row["town"],
				is ITownFixture town = findById(map, townId, warner),
				exists population = town.population, is Integer id = row["id"],
				is String kind = row["kind"], is String contents = row["contents"],
				is String qtyString = row["quantity"], is String units = row["units"],
				is Integer|SqlNull created = row["created"]);
			Number<out Anything> quantity;
			if (is Integer num = Integer.parse(qtyString)) {
				quantity = num;
			} else {
				assert (is Decimal num = parseDecimal(qtyString));
				quantity = num;
			}
			value pile = ResourcePile(id, kind, contents, Quantity(quantity, units));
			if (is Integer created) {
				pile.created = created;
			}
			population.yearlyConsumption.add(pile);
			count++;
			if (50.divides(count)) {
				log.trace("Finished reading ``count`` consumed resources");
			}
		}
		log.trace("Finished reading consumed resources, and with town and village contents");
	}
}