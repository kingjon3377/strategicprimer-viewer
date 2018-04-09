import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures.towns {
	Fortress
}
import ceylon.dbc {
	Sql
}
object dbFortressWriter satisfies DatabaseWriter<Fortress, Point> {
	shared actual void write(Sql db, Fortress obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS fortresses (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                owner INTEGER NOT NULL,
			                name VARCHAR(64) NOT NULL,
			                size VARCHAR(6) NOT NULL CHECK(size IN ('small', 'medium', 'large')),
			                id INTEGER NOT NULL,
			                image VARCHAR(255),
			                portrait VARCHAR(255)
		                )""").execute();
		db.Insert("""INSERT INTO fortresses (row, column, owner, name, size, id, image, portrait)
		             VALUES(?, ?, ?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.owner.playerId, obj.name, obj.townSize.string,
					obj.id, obj.image, obj.portrait);
		for (member in obj) {
			spDatabaseWriter.writeSPObjectInContext(db, member, obj);
		}
	}
}