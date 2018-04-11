import ceylon.dbc {
	Sql,
	SqlNull
}

import java.sql {
	Types
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures.explorable {
	Portal
}
object dbPortalHandler extends AbstractDatabaseWriter<Portal, Point>() satisfies MapContentsReader {
	shared actual {String+} initializers = [ // FIXME: Missing image field
		"""CREATE TABLE IF NOT EXISTS portals (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   destination_world VARCHAR(16),
			   destination_row INTEGER,
			   destination_column INTEGER
				   CHECK ((destination_row NOT NULL AND destination_column NOT NULL) OR
					   (destination_row IS NULL AND destination_column IS NULL))
		   )"""
	];
	shared actual void write(Sql db, Portal obj, Point context) {
		Integer[2]|SqlNull[2] destinationCoordinates;
		if (obj.destinationCoordinates.valid) {
			destinationCoordinates = [obj.destinationCoordinates.row, obj.destinationCoordinates.column];
		} else {
			destinationCoordinates = [SqlNull(Types.integer), SqlNull(Types.integer)];
		}
		db.Insert(
			"""INSERT INTO portals (row, column, id, destination_world, destination_row, destination_column)
			   VALUES(?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, obj.destinationWorld, *destinationCoordinates);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map) {
		for (dbRow in db.Select("""SELECT * FROM portals""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
				is Integer id = dbRow["id"], is String? destinationWorld = dbRow["destination_world"],
				is Integer? destinationRow = dbRow["destination_row"],
				is Integer? destinationColumn = dbRow["destination_column"], is String? image = dbRow["image"]);
			value portal = Portal(destinationWorld else "unknown",
				pointFactory(destinationRow else -1, destinationColumn else -1), id);
			if (exists image) {
				portal.image = image;
			}
			map.addFixture(pointFactory(row, column), portal);
		}
	}
}