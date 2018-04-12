import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures.resources {
	Mine
}
import strategicprimer.model.map.fixtures.towns {
	TownStatus
}
import strategicprimer.model.xmlio {
	Warning
}
object dbMineHandler extends AbstractDatabaseWriter<Mine, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS mines (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(128) NOT NULL,
			   status VARCHAR(9) NOT NULL
				   CHECK(status IN ('abandoned', 'active', 'burned', 'ruined')),
			   image VARCHAR(255)
		   )"""
	];
	shared actual void write(Sql db, Mine obj, Point context) {
		db.Insert("""INSERT INTO mines (row, column, id, kind, status, image) VALUES(?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, obj.kind, obj.status.string, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM mines""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"], is Integer id = dbRow["id"],
				is String kind = dbRow["kind"], is String statusString = dbRow["status"],
				is TownStatus status = TownStatus.parse(statusString), is String? image = dbRow["image"]);
			value mine = Mine(kind, status, id);
			if (exists image) {
				mine.image = image;
			}
			map.addFixture(pointFactory(row, column), mine);
		}
	}
}