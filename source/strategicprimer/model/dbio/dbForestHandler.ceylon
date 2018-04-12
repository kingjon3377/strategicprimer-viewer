import ceylon.dbc {
	Sql
}
import ceylon.decimal {
	Decimal,
	parseDecimal
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures.terrain {
	Forest
}
import strategicprimer.model.xmlio {
	Warning
}
object dbForestHandler extends AbstractDatabaseWriter<Forest, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS forests (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(32) NOT NULL,
			   rows BOOLEAN NOT NULL,
			   acres VARCHAR(128)
				   CHECK (acres NOT LIKE '%[^0-9.]%' AND acres NOT LIKE '%.%.%'),
			   image VARCHAR(255)
		   )"""
	];
	shared actual void write(Sql db, Forest obj, Point context) {
		db.Insert("""INSERT INTO forests(row, column, id, kind, rows, acres, image) VALUES(?, ?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, obj.kind, obj.rows, obj.acres.string, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM forests""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
				is Integer id = dbRow["id"], is String kind = dbRow["kind"], is Boolean rows = dbRow["rows"],
				is String acresString = dbRow["acres"], is String? image = dbRow["image"]);
			Number<out Anything> acres;
			if (is Integer num = Integer.parse(acresString)) {
				acres = num;
			} else {
				assert (is Decimal num = parseDecimal(acresString));
				acres = num;
			}
			value forest = Forest(kind, rows, id, acres);
			if (exists image) {
				forest.image = image;
			}
			map.addFixture(pointFactory(row, column), forest);
		}
	}
}