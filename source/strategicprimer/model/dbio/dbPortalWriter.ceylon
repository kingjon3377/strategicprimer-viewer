import strategicprimer.model.map.fixtures.explorable {
	Portal
}
import strategicprimer.model.map {
	Point
}
import ceylon.dbc {
	Sql,
	SqlNull
}
import java.sql {
	Types
}
object dbPortalWriter extends AbstractDatabaseWriter<Portal, Point>() {
	shared actual {String+} initializers = [
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
}