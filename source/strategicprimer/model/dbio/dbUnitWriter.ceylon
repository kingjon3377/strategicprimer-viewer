import ceylon.dbc {
	Sql,
	SqlNull
}

import java.sql {
	Types
}

import strategicprimer.model.map {
	Point,
	HasPortrait
}
import strategicprimer.model.map.fixtures.mobile {
	IUnit
}
import strategicprimer.model.map.fixtures.towns {
	Fortress
}
object dbUnitWriter satisfies DatabaseWriter<IUnit, Point|Fortress> {
	shared actual void write(Sql db, IUnit obj, Point|Fortress context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS units (
			                row INTEGER,
			                column INTEGER CHECK ((row NOT NULL AND column NOT NULL) OR (row IS NULL AND column IS NULL)),
			                parent INTEGER CHECK ((row NOT NULL AND parent IS NULL) OR (row IS NULL AND parent NOT NULL)),
			                owner INTEGER NOT NULL,
			                kind VARCHAR(32) NOT NULL,
			                name VARCHAR(64) NOT NULL,
			                id INTEGER NOT NULL,
			                image VARCHAR(255),
			                portrait VARCHAR(255)
		                )""").execute();
		db.Statement("""CREATE TABLE IF NOT EXISTS orders (
			                unit INTEGER NOT NULL,
			                turn INTEGER,
			                orders VARCHAR(2048) NOT NULL
		                )""").execute();
		db.Statement("""CREATE TABLE IF NOT EXISTS results (
		                 unit INTEGER NOT NULL,
		                 turn INTEGER,
		                 result VARCHAR(2048) NOT NULL
		                )""").execute();
		value unit = db.Insert("""INSERT INTO units (row, column, parent, owner, kind, name, id, image, portrait)
		                          VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)""");
		value order = db.Insert("""INSERT INTO orders (unit, turn, orders) VALUES(?, ?, ?)""");
		value result = db.Insert("""INSERT INTO results (unit, turn, result) VALUES(?, ?, ?)""");
		db.transaction(() {
			String|SqlNull portrait;
			if (is HasPortrait obj) {
				portrait = obj.portrait;
			} else {
				portrait = SqlNull(Types.varchar);
			}
			if (is Point context) {
				unit.execute(context.row, context.column, SqlNull(Types.integer), obj.owner.playerId, obj.kind,
					obj.name, obj.id, obj.image, portrait);
			} else {
				unit.execute(SqlNull(Types.integer), SqlNull(Types.integer), context.id, obj.owner.playerId, obj.kind,
					obj.name, obj.id, obj.image, portrait);
			}
			for (turn->orders in obj.allOrders) {
				order.execute(obj.id, turn, orders);
			}
			for (turn->results in obj.allResults) {
				result.execute(obj.id, turn, results);
			}
			return true;
		});
		for (member in obj) {
			spDatabaseWriter.writeSPObjectInContext(db, member, obj);
		}
	}
}