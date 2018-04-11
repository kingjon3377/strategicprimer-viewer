import ceylon.dbc {
	Sql,
	SqlNull
}

import java.sql {
	Types
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG
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
object dbImplementHandler extends AbstractDatabaseWriter<Implement, Point|IUnit|Fortress>() satisfies MapContentsReader {
	shared actual {String+} initializers = [ // FIXME: An Implement can't be outside a unit or fortress; drop Point as parent.
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
	shared actual void readMapContents(Sql db, IMutableMapNG map) {}
	shared actual void readExtraMapContents(Sql db, IMutableMapNG map) {
		for (row in db.Select("""SELECT * FROM implements WHERE parent NOT NULL""").Results()) {
			assert (is Integer parentId = row["parent"], is IUnit|Fortress parent = findById(map, parentId),
				is Integer id = row["id"], is String kind = row["kind"], is Integer count = row["count"],
				is String? image = row["image"]);
			value implement = Implement(kind, id, count);
			if (exists image) {
				implement.image = image;
			}
			if (is IUnit parent) {
				parent.addMember(implement);
			} else {
				parent.addMember(implement);
			}
		}
	}
}