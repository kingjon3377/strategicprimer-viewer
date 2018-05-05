import ceylon.dbc {
	Sql,
	SqlNull
}

import java.sql {
	Types
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	Player,
	pointFactory
}
import strategicprimer.model.map.fixtures.towns {
	AbstractTown,
	TownStatus,
	TownSize,
	Fortification,
	City,
	Town,
	CommunityStats
}
import strategicprimer.model.xmlio {
	Warning
}
object dbTownHandler extends AbstractDatabaseWriter<AbstractTown, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS towns (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(13) NOT NULL
				   CHECK(kind IN ('town', 'city', 'fortification')),
			   status VARCHAR(9) NOT NULL
				   CHECK(status IN ('abandoned', 'active', 'burned', 'ruined')),
			   size VARCHAR(6) NOT NULL
				   CHECK(size IN ('small', 'medium', 'large')),
			   dc INTEGER,
			   name VARCHAR(128) NOT NULL,
			   owner INTEGER NOT NULL,
			   image VARCHAR(255),
			   portrait VARCHAR(255),
			   population INTEGER
		   );"""
	];
	shared actual void write(Sql db, AbstractTown obj, Point context) {
		db.Insert("""INSERT INTO towns (row, column, id, kind, status, size, dc, name, owner, image, portrait, population)
		             VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);""")
				.execute(context.row, context.column, obj.id, obj.kind, obj.status.string, obj.townSize.string,
					obj.dc, obj.name, obj.owner.playerId, obj.image, obj.portrait,
					obj.population?.population else SqlNull(Types.integer));
		if (exists stats = obj.population) {
			dbCommunityStatsHandler.initialize(db);
			dbCommunityStatsHandler.write(db, stats, obj);
		}
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		log.trace("About to read towns");
		variable Integer count = 0;
		for (dbRow in db.Select("""SELECT * FROM towns""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"], is Integer id = dbRow["id"],
				is String kind = dbRow["kind"], is String statusString = dbRow["status"],
				is TownStatus status = TownStatus.parse(statusString), is String sizeString = dbRow["size"],
				is TownSize size = TownSize.parse(sizeString), is Integer dc = dbRow["dc"],
				is String name = dbRow["name"], is Integer ownerNum = dbRow["owner"],
				is String|SqlNull image = dbRow["image"], is String|SqlNull portrait = dbRow["portrait"],
				is Integer|SqlNull population = dbRow["population"]);
			AbstractTown town;
			Player owner = map.players.getPlayer(ownerNum);
			switch (kind)
			case ("fortification") {
				town = Fortification(status, size, dc, name, id, owner);
			}
			case ("city") {
				town = City(status, size, dc, name, id, owner);
			}
			case ("town") {
				town = Town(status, size, dc, name, id, owner);
			}
			else {
				throw AssertionError("Unhandled kind of town");
			}
			if (is String image) {
				town.image = image;
			}
			if (is String portrait) {
				town.portrait = portrait;
			}
			if (is Integer population) {
				town.population = CommunityStats(population);
			}
			map.addFixture(pointFactory(row, column), town);
			count++;
			if (50.divides(count)) {
				log.trace("Read ``count`` towns");
			}
		}
		log.trace("Finished reading towns");
	}
}