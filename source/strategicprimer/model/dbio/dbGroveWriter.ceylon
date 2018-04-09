import strategicprimer.model.map.fixtures.resources {
	Grove
}
import strategicprimer.model.map {
	Point
}
import ceylon.dbc {
	Sql
}
object dbGroveWriter satisfies DatabaseWriter<Grove, Point> {
	shared actual void write(Sql db, Grove obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS groves (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                id INTEGER NOT NULL,
			                type VARCHAR(7) NOT NULL CHECK (type IN ('grove', 'orchard')),
			                kind VARCHAR(64) NOT NULL,
			                cultivated BOOLEAN NOT NULL,
			                count INTEGER NOT NULL,
			                image VARCHAR(255)
		                )""").execute();
		db.Insert("""INSERT INTO groves (row, column, id, type, kind, cultivated, count, image)
		             VALUES(?, ?, ?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, (obj.orchard) then "orchard" else "grove",
					obj.kind, obj.cultivated, obj.population, obj.image);
	}
}