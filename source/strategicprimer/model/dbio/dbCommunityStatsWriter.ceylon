import strategicprimer.model.map.fixtures.towns {
	CommunityStats,
	ITownFixture
}
import ceylon.dbc {
	Sql
}
object dbCommunityStatsWriter extends AbstractDatabaseWriter<CommunityStats, ITownFixture>() {
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
			"""INSERT INTO town_production(town, id, kind, contents, quantity, units, created)
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
}