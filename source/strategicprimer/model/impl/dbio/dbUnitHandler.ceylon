import ceylon.dbc {
    Sql,
    SqlNull
}

import java.sql {
    Types
}

import strategicprimer.model.common.map {
    IMutableMapNG,
    HasPortrait,
    Point
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Unit
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import strategicprimer.model.common.xmlio {
    Warning
}

import lovelace.util.common {
    as
}

object dbUnitHandler extends AbstractDatabaseWriter<IUnit, Point|Fortress>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS units (
               row INTEGER,
               column INTEGER CHECK ((row IS NOT NULL AND column IS NOT NULL) OR
                   (row IS NULL AND column IS NULL)),
               parent INTEGER CHECK ((row IS NOT NULL AND parent IS NULL) OR
                   (row IS NULL AND parent IS NOT NULL)),
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
        value unit = db.Insert("""INSERT INTO units (row, column, parent, owner, kind,
                                      name, id, image, portrait)
                                  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);""");
        value order = db.Insert(
            """INSERT INTO orders (unit, turn, orders) VALUES(?, ?, ?);""");
        value result = db.Insert(
            """INSERT INTO results (unit, turn, result) VALUES(?, ?, ?);""");
        db.transaction(() {
            String|SqlNull portrait;
            if (is HasPortrait obj) {
                portrait = obj.portrait;
            } else {
                portrait = SqlNull(Types.varchar);
            }
            if (is Point context) {
                unit.execute(context.row, context.column, SqlNull(Types.integer),
                    obj.owner.playerId, obj.kind, obj.name, obj.id, obj.image, portrait);
            } else {
                unit.execute(SqlNull(Types.integer), SqlNull(Types.integer), context.id,
                    obj.owner.playerId, obj.kind, obj.name, obj.id, obj.image, portrait);
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

    void readOrders(IUnit unit, Map<String, Object> ordersRow, Warning warner) {
        assert (is Integer|SqlNull turn = ordersRow["turn"],
            is String orders = ordersRow["orders"]);
        unit.setOrders(as<Integer>(turn) else -1, orders);
    }

    void readResults(IUnit unit, Map<String, Object> resultsRow, Warning warner) {
        assert (is Integer|SqlNull turn = resultsRow["turn"],
            is String results = resultsRow["results"]);
        unit.setResults(as<Integer>(turn) else -1, results);
    }

    void readUnit(IMutableMapNG map, Sql db, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer ownerNum = dbRow["owner"], is String kind = dbRow["kind"],
            is String name = dbRow["name"], is Integer id = dbRow["id"],
            is String|SqlNull image = dbRow["image"],
            is String|SqlNull portrait = dbRow["portrait"]);
        value unit = Unit(map.players.getPlayer(ownerNum), kind, name, id);
        if (is String image) {
            unit.image = image;
        }
        if (is String portrait) {
            unit.portrait = portrait;
        }
        handleQueryResults(db, warner, "turns' orders", curry(readOrders)(unit),
            """SELECT * from orders WHERE unit = ?""", id);
        handleQueryResults(db, warner, "turns' results", curry(readResults)(unit),
            """SELECT * from results WHERE unit = ?""", id);
        if (is Integer row = dbRow["row"], is Integer column = dbRow["column"]) {
            map.addFixture(Point(row, column), unit);
        } else {
            assert (is Integer parentNum = dbRow["parent"],
                is Fortress parent = super.findById(map, parentNum, warner));
            parent.addMember(unit);
        }
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "units outside fortresses",
                curry(curry(readUnit)(map))(db),
                """SELECT * FROM units WHERE row IS NOT NULL""");

    shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "units in fortresses",
                curry(curry(readUnit)(map))(db),
                """SELECT * FROM units WHERE parent IS NOT NULL""");
}
