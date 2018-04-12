import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures.resources {
	Grove
}
import strategicprimer.model.xmlio {
	Warning
}
object dbGroveHandler extends AbstractDatabaseWriter<Grove, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS groves (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   type VARCHAR(7) NOT NULL
				   CHECK (type IN ('grove', 'orchard')),
			   kind VARCHAR(64) NOT NULL,
			   cultivated BOOLEAN NOT NULL,
			   count INTEGER NOT NULL,
			   image VARCHAR(255)
		   )"""
	];
	shared actual void write(Sql db, Grove obj, Point context) {
		db.Insert("""INSERT INTO groves (row, column, id, type, kind, cultivated, count, image)
		             VALUES(?, ?, ?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, (obj.orchard) then "orchard" else "grove",
					obj.kind, obj.cultivated, obj.population, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM groves""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"], is Integer id = dbRow["id"],
				is String type = dbRow["type"], is String kind = dbRow["kind"],
				is Boolean cultivated = dbRow["cultivated"], is Integer count = dbRow["count"],
				is String? image = dbRow["image"]);
			Boolean orchard;
			switch (type)
			case ("grove") {
				orchard = false;
			}
			case ("orchard") {
				orchard = true;
			}
			else {
				throw AssertionError("Unexpected grove type");
			}
			value grove = Grove(orchard, cultivated, kind, id, count);
			if (exists image) {
				grove.image = image;
			}
			map.addFixture(pointFactory(row, column), grove);
		}
	}
}