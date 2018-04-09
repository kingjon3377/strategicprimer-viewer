import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures.resources {
	Shrub
}
import ceylon.dbc {
	Sql
}
object dbShrubWriter satisfies DatabaseWriter<Shrub, Point> {
	shared actual void write(Sql db, Shrub obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS shrubs (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                id INTEGER NOT NULL,
			                kind VARCHAR(64) NOT NULL,
			                count INTEGER,
			                image VARCHAR(255)
		                )""").execute();
		db.Insert("""INSERT INTO shrubs (row, column, id, kind, count, image)
		             VALUES(?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, obj.kind,
					obj.population, obj.image);
	}
}