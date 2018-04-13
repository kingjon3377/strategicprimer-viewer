import ceylon.dbc {
	Sql,
	SqlNull
}

import java.sql {
	Types
}

import strategicprimer.model.map {
	Point,
	HasPortrait,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures.mobile {
	IUnit,
	Unit
}
import strategicprimer.model.map.fixtures.towns {
	Fortress
}
import strategicprimer.model.xmlio {
	Warning
}
object dbUnitHandler extends AbstractDatabaseWriter<IUnit, Point|Fortress>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS units (
			   row INTEGER,
			   column INTEGER CHECK ((row IS NOT NULL AND column IS NOT NULL) OR (row IS NULL AND column IS NULL)),
			   parent INTEGER CHECK ((row IS NOT NULL AND parent IS NULL) OR (row IS NULL AND parent IS NOT NULL)),
			   owner INTEGER NOT NULL,
			   kind VARCHAR(32) NOT NULL,
			   name VARCHAR(64) NOT NULL,
			   id INTEGER NOT NULL,
			   image VARCHAR(255),
			   portrait VARCHAR(255)
		   );""",
		"""CREATE TABLE IF NOT EXISTS orders (
			   unit INTEGER NOT NULL,
			   turn INTEGER,
			   orders VARCHAR(2048) NOT NULL
		   );""",
		"""CREATE TABLE IF NOT EXISTS results (
			   unit INTEGER NOT NULL,
			   turn INTEGER,
			   result VARCHAR(2048) NOT NULL
		   );"""
	];
	shared actual void write(Sql db, IUnit obj, Point|Fortress context) {
		value unit = db.Insert("""INSERT INTO units (row, column, parent, owner, kind, name, id, image, portrait)
		                          VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);""");
		value order = db.Insert("""INSERT INTO orders (unit, turn, orders) VALUES(?, ?, ?);""");
		value result = db.Insert("""INSERT INTO results (unit, turn, result) VALUES(?, ?, ?);""");
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
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM units WHERE row IS NOT NULL""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
				is Integer ownerNum = dbRow["owner"], is String kind = dbRow["kind"],
				is String name = dbRow["name"], is Integer id = dbRow["id"],
				is String? image = dbRow["image"], is String? portrait = dbRow["portrait"]);
			value unit = Unit(map.players.getPlayer(ownerNum), kind, name, id);
			if (exists image) {
				unit.image = image;
			}
			if (exists portrait) {
				unit.portrait = portrait;
			}
			for (ordersRow in db.Select("""SELECT * from orders WHERE unit = ?""").Results(id)) {
				assert (is Integer? turn = ordersRow["turn"], is String orders = ordersRow["orders"]);
				unit.setOrders(turn else -1, orders);
			}
			for (resultsRow in db.Select("""SELECT * from results WHERE unit = ?""").Results(id)) {
				assert (is Integer? turn = resultsRow["turn"], is String results = resultsRow["results"]);
				unit.setResults(turn else -1, results);
			}
			map.addFixture(pointFactory(row, column), unit);
		}
	}
	shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (dbRow in db.Select("""SELECT * FROM units WHERE parent IS NOT NULL""").Results()) {
			assert (is Integer parentNum = dbRow["parent"], is Fortress parent = super.findById(map, parentNum, warner),
				is Integer ownerNum = dbRow["owner"], is String kind = dbRow["kind"],
				is String name = dbRow["name"], is Integer id = dbRow["id"], is String? image = dbRow["image"],
				is String? portrait = dbRow["portrait"]);
			value unit = Unit(map.players.getPlayer(ownerNum), kind, name, id);
			if (exists image) {
				unit.image = image;
			}
			if (exists portrait) {
				unit.portrait = portrait;
			}
			for (ordersRow in db.Select("""SELECT * from orders WHERE unit = ?""").Results(id)) {
				assert (is Integer? turn = ordersRow["turn"], is String orders = ordersRow["orders"]);
				unit.setOrders(turn else -1, orders);
			}
			for (resultsRow in db.Select("""SELECT * from results WHERE unit = ?""").Results(id)) {
				assert (is Integer? turn = resultsRow["turn"], is String results = resultsRow["results"]);
				unit.setResults(turn else -1, results);
			}
			parent.addMember(unit);
		}
	}
}