import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures {
	Ground
}
import strategicprimer.model.xmlio {
	Warning
}
object dbGroundHandler extends AbstractDatabaseWriter<Ground, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS ground (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(32) NOT NULL,
			   exposed BOOLEAN NOT NULL,
			   image VARCHAR(255)
		   );"""
	];
	shared actual void write(Sql db, Ground obj, Point context) {
		db.Insert("""INSERT INTO ground (row, column, id, kind, exposed, image)
		             VALUES(?, ?, ?, ?, ?, ?);""")
				.execute(context.row, context.column, obj.id, obj.kind, obj.exposed, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM ground""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
				is Integer id = dbRow["id"], is String kind = dbRow["kind"],
				is Boolean exposed = dbMapReader.databaseBoolean(dbRow["exposed"]),
				is String? image = dbRow["image"]);
			value ground = Ground(id, kind, exposed);
			if (exists image) {
				ground.image = image;
			}
			map.addFixture(pointFactory(row, column), ground);
		}
	}
}