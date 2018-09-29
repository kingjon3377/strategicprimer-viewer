import ceylon.dbc {
    Sql,
    SqlNull
}
import ceylon.decimal {
    Decimal,
    parseDecimal
}

import strategicprimer.model.common.map {
    Point
}

import strategicprimer.model.impl.map {
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.common.xmlio {
    Warning
}
object dbForestHandler extends AbstractDatabaseWriter<Forest, Point>()
        satisfies MapContentsReader {
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
           );"""
    ];
    shared actual void write(Sql db, Forest obj, Point context) {
        db.Insert("""INSERT INTO forests(row, column, id, kind, rows, acres, image)
                     VALUES(?, ?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.id, obj.kind, obj.rows,
                    obj.acres.string, obj.image);
    }
    void readForest(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String kind = dbRow["kind"],
            is Boolean rows = dbMapReader.databaseBoolean(dbRow["rows"]),
            is String acresString = dbRow["acres"],
            is String|SqlNull image = dbRow["image"]);
        Number<out Anything> acres;
        if (is Integer num = Integer.parse(acresString)) {
            acres = num;
        } else {
            assert (is Decimal num = parseDecimal(acresString));
            acres = num;
        }
        value forest = Forest(kind, rows, id, acres);
        if (is String image) {
            forest.image = image;
        }
        map.addFixture(Point(row, column), forest);
    }
    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "forests", curry(readForest)(map),
                """SELECT * FROM forests""");
}