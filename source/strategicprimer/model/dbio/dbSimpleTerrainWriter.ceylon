import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures.terrain {
	Hill,
	Oasis,
	Sandbar
}
import ceylon.dbc {
	Sql
}
object dbSimpleTerrainWriter satisfies DatabaseWriter<Hill|Oasis|Sandbar, Point> {
	shared actual void write(Sql db, Hill|Oasis|Sandbar obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS simple_terrain (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                type VARCHAR(7) NOT NULL CHECK(type IN('hill', 'oasis', 'sandbar')),
			                id INTEGER NOT NULL,
			                image VARCHAR(255)
		                )""").execute();
		String type;
		switch (obj)
		case (is Hill) {
			type = "hill";
		}
		case (is Oasis) {
			type = "oasis";
		}
		case (is Sandbar) {
			type = "sandbar";
		}
		db.Insert("""INSERT INTO simple_terrain (row, column, type, id, image) VALUES(?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, type, obj.id, obj.image);
	}
}