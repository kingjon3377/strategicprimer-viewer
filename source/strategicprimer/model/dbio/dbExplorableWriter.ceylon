import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures.explorable {
	Battlefield,
	Cave
}
import ceylon.dbc {
	Sql
}
object dbExplorableWriter satisfies DatabaseWriter<Cave|Battlefield, Point> {
	shared actual void write(Sql db, Cave|Battlefield obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS caves (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                id INTEGER NOT NULL,
			                dc INTEGER,
			                image VARCHAR(255)
		                )""").execute();
		db.Statement("""CREATE TABLE IF NOT EXISTS battlefields (
		                 row INTEGER NOT NULL,
		                 column INTEGER NOT NULL,
		                 id INTEGER NOT NULL,
		                 dc INTEGER,
		                 image VARCHAR(255)
		                )""").execute();
		Sql.Insert insertion;
		switch (obj)
		case (is Cave) {
			insertion = db.Insert("""INSERT INTO caves (row, column, id, dc, image) VALUES(?, ?, ?, ?, ?)""");
		}
		case (is Battlefield) {
			insertion = db.Insert("""INSERT INTO battlefields (row, column, id, dc, image) VALUES(?, ?, ?, ?, ?)""");
		}
		insertion.execute(context.row, context.column, obj.id, obj.dc, obj.image);
	}
}