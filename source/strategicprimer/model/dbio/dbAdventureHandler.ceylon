import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	PlayerImpl,
	pointFactory
}
import strategicprimer.model.map.fixtures.explorable {
	AdventureFixture
}
object dbAdventureHandler extends AbstractDatabaseWriter<AdventureFixture, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers =
			["""CREATE TABLE IF NOT EXISTS adventures (
				    row INTEGER NOT NULL,
				    column INTEGER NOT NULL,
				    id INTEGER NOT NULL,
				    brief VARCHAR(255) NOT NULL,
				    full VARCHAR(512) NOT NULL,
				    owner INTEGER NOT NULL,
				    image VARCHAR(255)
			    )"""];
	shared actual void write(Sql db, AdventureFixture obj, Point context) {
			db.Insert("""INSERT INTO adventures (row, column, id, brief, full, owner, image)
			             VALUES(?, ?, ?, ?, ?, ?, ?)""")
					.execute(context.row, context.column, obj.id, obj.briefDescription,
						obj.fullDescription, obj.owner.playerId, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map) {
		for (dbRow in db.Select("""SELECT * FROM adventures""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"], is Integer id = dbRow["id"],
				is String brief = dbRow["brief"], is String full = dbRow["full"],
				is Integer ownerId = dbRow["owner"], is String? image = dbRow["image"]);
			value adventure = AdventureFixture(map.players.find((player) => player.playerId == ownerId) else PlayerImpl(ownerId, ""),
				brief, full, id);
			if (exists image) {
				adventure.image = image;
			}
			map.addFixture(pointFactory(row, column), adventure);
		}
	}
}