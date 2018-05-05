import ceylon.dbc {
	Sql,
	SqlNull
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

import lovelace.util.common {
	as
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
		log.trace("About to read shrubs");
		variable Integer runningTotal = 0;
		for (dbRow in db.Select("""SELECT * FROM shrubs""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"], is Integer id = dbRow["id"],
				is String kind = dbRow["kind"], is Integer|SqlNull count = dbRow["count"],
				is String|SqlNull image = dbRow["image"]);
			value shrub = Shrub(kind, id, as<Integer>(count) else -1);
			if (is String image) {
				shrub.image = image;
			}
			map.addFixture(pointFactory(row, column), shrub);
			runningTotal++;
			if (50.divides(runningTotal)) {
				log.trace("Read ``runningTotal`` shrubs");
			}
		}
		log.trace("Finished reading shrubs");
	}
}