import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures {
	Implement
}
import strategicprimer.model.map.fixtures.mobile {
	IUnit
}
import strategicprimer.model.map.fixtures.towns {
	Fortress
}
import ceylon.dbc {
	Sql,
	SqlNull
}
import java.sql {
	Types
}
object dbImplementWriter extends AbstractDatabaseWriter<Implement, Point|IUnit|Fortress>() {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS implements (
			   row INTEGER,
			   column INTEGER
				   CHECK ((row NOT NULL AND column NOT NULL)
					   OR (row IS NULL AND column IS NULL)),
			   parent INTEGER
				   CHECK ((row NOT NULL AND parent IS NULL)
					   OR (row IS NULL AND parent NOT NULL)),
			   id INTEGER NOT NULL,
			   kind VARCHAR(255) NOT NULL,
			   count INTEGER NOT NULL DEFAULT 1,
			   image VARCHAR(255)
		   )"""
	];
	shared actual void write(Sql db, Implement obj, Point|IUnit|Fortress context) {
		value insertion = db.Insert(
			"""INSERT INTO implements (row, column, parent, id, kind, count, image)
			   VALUES(?, ?, ?, ?, ?, ?, ?)""");
		if (is Point context) {
			insertion.execute(context.row, context.column, SqlNull(Types.integer), obj.id, obj.kind, obj.count, obj.image);
		} else {
			insertion.execute(SqlNull(Types.integer), SqlNull(Types.integer), context.id, obj.id, obj.kind, obj.count, obj.image);
		}
	}
}