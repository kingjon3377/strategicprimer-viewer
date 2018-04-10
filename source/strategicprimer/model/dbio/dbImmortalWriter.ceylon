import ceylon.dbc {
	Sql,
	SqlNull
}

import java.sql {
	Types
}

import strategicprimer.model.map {
	Point,
	HasKind
}
import strategicprimer.model.map.fixtures.mobile {
	Immortal,
	IUnit,
	SimpleImmortal,
	Centaur,
	Dragon,
	Fairy,
	Giant
}
object dbImmortalWriter extends AbstractDatabaseWriter<Immortal, Point|IUnit>() {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS simple_immortals (
			   row INTEGER,
			   column INTEGER
				   CHECK ((row NOT NULL AND column NOT NULL)
					   OR (row IS NULL AND column IS NULL)),
			   parent INTEGER
				   CHECK ((row NOT NULL AND parent IS NULL)
					   OR (row IS NULL AND parent NOT NULL)),
			   type VARCHAR(16) NOT NULL,
			   id INTEGER NOT NULL,
			   dc INTEGER NOT NULL,
			   image VARCHAR(255)
		   )""",
	"""CREATE TABLE IF NOT EXISTS kinded_immortals (
		   row INTEGER,
		   column INTEGER
			   CHECK ((row NOT NULL AND column NOT NULL)
				   OR (row IS NULL AND column IS NULL)),
		   parent INTEGER
			   CHECK ((row NOT NULL AND parent IS NULL)
				   OR (row IS NULL AND parent NOT NULL)),
		   type VARCHAR(16) NOT NULL,
		   kind VARCHAR(32) NOT NULL,
		   id INTEGER NOT NULL,
		   dc INTEGER NOT NULL,
		   image VARCHAR(255)
	   )"""
	];
	shared actual void write(Sql db, Immortal obj, Point|IUnit context) {
		if (is SimpleImmortal obj) {
			value insertion = db.Insert("""INSERT INTO simple_immortals (row, column, parent, type, id, dc, image)
			                               VALUES(?, ?, ?, ?, ?, ?, ?)""");
			if (is Point context) {
				insertion.execute(context.row, context.column, SqlNull(Types.integer), obj.kind, obj.id,
					obj.dc, obj.image);
			} else {
				insertion.execute(SqlNull(Types.integer), SqlNull(Types.integer), context.id,
					obj.kind, obj.id, obj.dc, obj.image);
			}
		} else {
			assert (is HasKind obj);
			assert (is Centaur|Dragon|Fairy|Giant obj);
			String type;
			switch (obj)
			case (is Centaur) {
				type = "centaur";
			}
			case (is Dragon) {
				type = "dragon";
			}
			case (is Fairy) {
				type = "fairy";
			}
			case (is Giant) {
				type = "giant";
			}
			value insertion = db.Insert(
				"""INSERT INTO kinded_immortals (row, column, parent, type, kind, id, dc, image)
				   VALUES(?, ?, ?, ?, ?, ?, ?, ?)""");
			if (is Point context) {
				insertion.execute(context.row, context.column, SqlNull(Types.integer), type,
					obj.kind, obj.id, obj.dc, obj.image);
			} else {
				insertion.execute(SqlNull(Types.integer), SqlNull(Types.integer), context.id,
					type, obj.kind, obj.id, obj.dc, obj.image);
			}
		}
	}
}