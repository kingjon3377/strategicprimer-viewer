import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	TileFixture,
	pointFactory,
	HasMutableImage
}
import strategicprimer.model.map.fixtures.terrain {
	Hill,
	Oasis,
	Sandbar
}
import strategicprimer.model.xmlio {
	Warning
}
object dbSimpleTerrainHandler extends AbstractDatabaseWriter<Hill|Oasis|Sandbar, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS simple_terrain (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   type VARCHAR(7) NOT NULL
				   CHECK(type IN('hill', 'oasis', 'sandbar')),
			   id INTEGER NOT NULL,
			   image VARCHAR(255)
		   )"""
	];
	shared actual void write(Sql db, Hill|Oasis|Sandbar obj, Point context) {
		String type;
		switch (obj)
		case (is Hill) {
			type = "hill";
		}
		case (is Oasis) {
			type = "oasis";
		}
		case (is Sandbar) {
			type = "sandbar";
		}
		db.Insert("""INSERT INTO simple_terrain (row, column, type, id, image) VALUES(?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, type, obj.id, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM simple_terrain""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
				is String type = dbRow["type"], is Integer id = dbRow["id"], is String? image = dbRow["image"]);
			TileFixture&HasMutableImage fixture;
			switch (type)
			case ("hill") {
				fixture = Hill(id);
			}
			case ("sandbar") {
				fixture = Sandbar(id);
			}
			case ("oasis") {
				fixture = Oasis(id);
			}
			else {
				throw AssertionError("Unhandled simple terrain-fixture type");
			}
			if (exists image) {
				fixture.image = image;
			}
			map.addFixture(pointFactory(row, column), fixture);
		}
	}
}