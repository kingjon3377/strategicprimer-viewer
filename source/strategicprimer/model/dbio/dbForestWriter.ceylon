import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures.terrain {
	Forest
}
import ceylon.dbc {
	Sql
}
object dbForestWriter satisfies DatabaseWriter<Forest, Point> {
	shared actual void write(Sql db, Forest obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS forests (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                id INTEGER NOT NULL,
			                kind VARCHAR(32) NOT NULL,
			                rows BOOLEAN NOT NULL,
			                acres VARCHAR(128) CHECK (acres NOT LIKE '%[^0-9.]%' AND acres NOT LIKE '%.%.%'),
			                image VARCHAR(255)
		                )""").execute();
		db.Insert("""INSERT INTO forests(row, column, id, kind, rows, acres, image) VALUES(?, ?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, obj.kind, obj.rows, obj.acres.string, obj.image);
	}
}