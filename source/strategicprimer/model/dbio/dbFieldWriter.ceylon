import strategicprimer.model.map.fixtures.resources {
	Meadow
}
import strategicprimer.model.map {
	Point
}
import ceylon.dbc {
	Sql
}
object dbFieldWriter extends AbstractDatabaseWriter<Meadow, Point>() {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS fields (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   type VARCHAR(6) NOT NULL
				   CHECK (type IN ('field', 'meadow')),
			   kind VARCHAR(64) NOT NULL,
			   cultivated BOOLEAN NOT NULL,
			   status VARCHAR(7) NOT NULL
				   CHECK (status IN ('fallow', 'seeding', 'growing', 'bearing')),
			   acres VARCHAR(128)
				   CHECK (acres NOT LIKE '%[^0-9.]%' AND acres NOT LIKE '%.%.%'),
			   image VARCHAR(255)
		   )"""
	];
	shared actual void write(Sql db, Meadow obj, Point context) {
		db.Insert("""INSERT INTO fields (row, column, id, type, kind, cultivated, status, acres, image)
		             VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, (obj.field) then "field" else "meadow", obj.kind,
					obj.cultivated, obj.status.string, obj.acres.string, obj.image);
	}
}