import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures {
	Ground
}
import ceylon.dbc {
	Sql
}
object dbGroundWriter satisfies DatabaseWriter<Ground, Point> {
	shared actual void write(Sql db, Ground obj, Point context) {
		db.Statement(
			"""CREATE TABLE IF NOT EXISTS ground (
			    row INTEGER NOT NULL,
			    column INTEGER NOT NULL,
			    id INTEGER NOT NULL,
			    kind VARCHAR(32),
			    exposed BOOLEAN,
			    image VARCHAR(255)
			   )""").execute();
		db.Insert("""INSERT INTO ground (row, column, id, kind, exposed, image)
		             VALUES(?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, obj.kind, obj.exposed, obj.image);
	}
}