import ceylon.dbc {
	Sql
}
import ceylon.decimal {
	Decimal,
	parseDecimal
}

import strategicprimer.model.map {
	IMutableMapNG
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
object dbCommunityStatsHandler extends AbstractDatabaseWriter<CommunityStats, ITownFixture>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS town_expertise (
			   town INTEGER NOT NULL,
			   skill VARCHAR(255) NOT NULL,
			   level INTEGER NOT NULL
		   )""",
		"""CREATE TABLE IF NOT EXISTS town_worked_resources (
			   town INTEGER NOT NULL,
			   resource INTEGER NOT NULL
		   )""",
		"""CREATE TABLE IF NOT EXISTS town_production (
			   town INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(64) NOT NULL,
			   contents VARCHAR(64) NOT NULL,
			   quantity VARCHAR(128) NOT NULL
				   CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%'),
			   units VARCHAR(32) NOT NULL,
			   created INTEGER
		   )""",
		"""CREATE TABLE IF NOT EXISTS town_consumption (
			   town INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(64) NOT NULL,
			   contents VARCHAR(64) NOT NULL,
			   quantity VARCHAR(128) NOT NULL
				   CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%'),
			   units VARCHAR(32) NOT NULL,
			   created INTEGER
		   )"""
	];
	shared actual void write(Sql db, CommunityStats obj, ITownFixture context) {
		value expertise = db.Insert("""INSERT INTO town_expertise (town, skill, level) VALUES(?, ?, ?)""");
		value fields = db.Insert("""INSERT INTO town_worked_resources (town, resource) VALUES(?, ?)""");
		value production = db.Insert(
			"""INSERT INTO town_production(town, id, kind, contents, quantity, units, created)
			   VALUES(?, ?, ?, ?, ?, ?, ?)""");
		value consumption = db.Insert(
			"""INSERT INTO town_consumption(town, id, kind, contents, quantity, units, created)
			   VALUES(?, ?, ?, ?, ?, ?, ?)""");
		db.transaction(() {
			for (skill->level in obj.highestSkillLevels) {
				expertise.execute(context.id, skill, level);
			}
			for (field in obj.workedFields) {
				fields.execute(context.id, field);
			}
			for (resource in obj.yearlyProduction) {
				production.execute(context.id, resource.id, resource.kind, resource.contents, resource.quantity.number.string,
					resource.quantity.units, resource.created);
			}
			for (resource in obj.yearlyConsumption) {
				consumption.execute(context.id, resource.id, resource.kind, resource.contents, resource.quantity.number.string,
					resource.quantity.units, resource.created);
			}
			return true;
		});
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {}
	shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (row in db.Select("""SELECT * FROM town_expertise""").Results()) {
			assert (is Integer townId = row["town"], is ITownFixture town = findById(map, townId),
				exists population = town.population, is String skill = row["skill"],
				is Integer level = row["level"]);
			population.setSkillLevel(skill, level);
		}
		for (row in db.Select("""SELECT * FROM town_worked_resources""").Results()) {
			assert (is Integer townId = row["town"], is ITownFixture town = findById(map, townId),
				exists population = town.population, is Integer resource = row["resource"]);
			population.addWorkedField(resource);
		}
		for (row in db.Select("""SELECT * FROM town_production""").Results()) {
			assert (is Integer townId = row["town"], is ITownFixture town = findById(map, townId),
				exists population = town.population, is Integer id = row["id"], is String kind = row["kind"],
				is String contents = row["contents"], is String qtyString = row["quantity"],
				is String units = row["units"], is Integer? created = row["created"]);
			Number<out Anything> quantity;
			if (is Integer num = Integer.parse(qtyString)) {
				quantity = num;
			} else {
				assert (is Decimal num = parseDecimal(qtyString));
				quantity = num;
			}
			value pile = ResourcePile(id, kind, contents, Quantity(quantity, units));
			if (exists created) {
				pile.created = created;
			}
			population.yearlyProduction.add(pile);
		}
		for (row in db.Select("""SELECT * FROM town_consumption""").Results()) {
			assert (is Integer townId = row["town"], is ITownFixture town = findById(map, townId),
				exists population = town.population, is Integer id = row["id"], is String kind = row["kind"],
				is String contents = row["contents"], is String qtyString = row["quantity"],
				is String units = row["units"], is Integer? created = row["created"]);
			Number<out Anything> quantity;
			if (is Integer num = Integer.parse(qtyString)) {
				quantity = num;
			} else {
				assert (is Decimal num = parseDecimal(qtyString));
				quantity = num;
			}
			value pile = ResourcePile(id, kind, contents, Quantity(quantity, units));
			if (exists created) {
				pile.created = created;
			}
			population.yearlyConsumption.add(pile);
		}
	}
}