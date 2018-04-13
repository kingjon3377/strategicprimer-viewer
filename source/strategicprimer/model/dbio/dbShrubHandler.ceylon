import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures.resources {
	Shrub
}
import strategicprimer.model.xmlio {
	Warning
}
object dbShrubHandler extends AbstractDatabaseWriter<Shrub, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS shrubs (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(64) NOT NULL,
			   count INTEGER,
			   image VARCHAR(255)
		   );"""
	];
	shared actual void write(Sql db, Shrub obj, Point context) {
		db.Insert("""INSERT INTO shrubs (row, column, id, kind, count, image)
		             VALUES(?, ?, ?, ?, ?, ?);""")
				.execute(context.row, context.column, obj.id, obj.kind,
					obj.population, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM shrubs""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"], is Integer id = dbRow["id"],
				is String kind = dbRow["kind"], is Integer? count = dbRow["count"], is String? image = dbRow["image"]);
			value shrub = Shrub(kind, id, count else -1);
			if (exists image) {
				shrub.image = image;
			}
			map.addFixture(pointFactory(row, column), shrub);
		}
	}
}