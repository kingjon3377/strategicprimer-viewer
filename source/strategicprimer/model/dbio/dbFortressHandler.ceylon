import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures.towns {
	Fortress,
	TownSize
}
import strategicprimer.model.xmlio {
	Warning
}
object dbFortressHandler extends AbstractDatabaseWriter<Fortress, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS fortresses (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   owner INTEGER NOT NULL,
			   name VARCHAR(64) NOT NULL,
			   size VARCHAR(6) NOT NULL
				   CHECK(size IN ('small', 'medium', 'large')),
			   id INTEGER NOT NULL,
			   image VARCHAR(255),
			   portrait VARCHAR(255)
		   );"""
	];
	shared actual void write(Sql db, Fortress obj, Point context) {
		db.Insert("""INSERT INTO fortresses (row, column, owner, name, size, id, image, portrait)
		             VALUES(?, ?, ?, ?, ?, ?, ?, ?);""")
				.execute(context.row, context.column, obj.owner.playerId, obj.name, obj.townSize.string,
					obj.id, obj.image, obj.portrait);
		for (member in obj) {
			spDatabaseWriter.writeSPObjectInContext(db, member, obj);
		}
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM fortresses""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
				is Integer ownerId = dbRow["owner"], is String name = dbRow["name"],
				is String sizeString = dbRow["size"], is TownSize size = TownSize.parse(sizeString),
				is Integer id = dbRow["id"], is String? image = dbRow["image"],
				is String? portrait = dbRow["portrait"]);
			value fortress = Fortress(map.players.getPlayer(ownerId), name, id, size);
			if (exists image) {
				fortress.image = image;
			}
			if (exists portrait) {
				fortress.portrait = portrait;
			}
			map.addFixture(pointFactory(row, column), fortress);
		}
	}
}