import strategicprimer.model.map.fixtures.resources {
	Mine
}
import strategicprimer.model.map {
	Point
}
import ceylon.dbc {
	Sql
}
object dbMineWriter satisfies DatabaseWriter<Mine, Point> {
	shared actual void write(Sql db, Mine obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS mines (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                id INTEGER NOT NULL,
			                kind VARCHAR(128) NOT NULL,
			                status VARCHAR(9) NOT NULL CHECK(status IN ('abandoned', 'active', 'burned', 'ruined')),
			                image VARCHAR(255)
		                )""").execute();
		db.Insert("""INSERT INTO mines (row, column, id, kind, status, image) VALUES(?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, obj.kind, obj.status.string, obj.image);
	}
}