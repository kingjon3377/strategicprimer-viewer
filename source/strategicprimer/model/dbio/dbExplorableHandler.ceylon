import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures.explorable {
	Battlefield,
	Cave
}
import strategicprimer.model.xmlio {
	Warning
}
object dbExplorableHandler extends AbstractDatabaseWriter<Cave|Battlefield, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS caves (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   dc INTEGER NOT NULL,
			   image VARCHAR(255)
		   );""",
		"""CREATE TABLE IF NOT EXISTS battlefields (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   dc INTEGER NOT NULL,
			   image VARCHAR(255)
		   );"""
	];
	shared actual void write(Sql db, Cave|Battlefield obj, Point context) {
		Sql.Insert insertion;
		switch (obj)
		case (is Cave) {
			insertion = db.Insert("""INSERT INTO caves (row, column, id, dc, image) VALUES(?, ?, ?, ?, ?);""");
		}
		case (is Battlefield) {
			insertion = db.Insert("""INSERT INTO battlefields (row, column, id, dc, image) VALUES(?, ?, ?, ?, ?);""");
		}
		insertion.execute(context.row, context.column, obj.id, obj.dc, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM caves""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"], is Integer id = dbRow["id"],
				is Integer dc = dbRow["dc"], is String? image = dbRow["image"]);
			value cave = Cave(dc, id);
			if (exists image) {
				cave.image = image;
			}
			map.addFixture(pointFactory(row, column), cave);
		}
		for (dbRow in db.Select("""SELECT * FROM battlefields""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"], is Integer id = dbRow["id"],
				is Integer dc = dbRow["dc"], is String? image = dbRow["image"]);
			value battlefield = Battlefield(dc, id);
			if (exists image) {
				battlefield.image = image;
			}
			map.addFixture(pointFactory(row, column), battlefield);
		}
	}
}