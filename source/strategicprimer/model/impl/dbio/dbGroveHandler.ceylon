import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
	Point
}

import strategicprimer.model.impl.map {
	IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.resources {
    Grove
}
import strategicprimer.model.common.xmlio {
    Warning
}
object dbGroveHandler extends AbstractDatabaseWriter<Grove, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS groves (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               id INTEGER NOT NULL,
               type VARCHAR(7) NOT NULL
                   CHECK (type IN ('grove', 'orchard')),
               kind VARCHAR(64) NOT NULL,
               cultivated BOOLEAN NOT NULL,
               count INTEGER NOT NULL,
               image VARCHAR(255)
           );"""
    ];
    shared actual void write(Sql db, Grove obj, Point context) {
        db.Insert("""INSERT INTO groves (row, column, id, type, kind, cultivated, count,
                         image)
                     VALUES(?, ?, ?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.id,
                    (obj.orchard) then "orchard" else "grove",
                    obj.kind, obj.cultivated, obj.population, obj.image);
    }
    void readGrove(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String type = dbRow["type"],
            is String kind = dbRow["kind"],
            is Boolean cultivated = dbMapReader.databaseBoolean(dbRow["cultivated"]),
            is Integer count = dbRow["count"], is String|SqlNull image = dbRow["image"]);
        Boolean orchard;
        switch (type)
        case ("grove") {
            orchard = false;
        }
        case ("orchard") {
            orchard = true;
        }
        else {
            throw AssertionError("Unexpected grove type");
        }
        value grove = Grove(orchard, cultivated, kind, id, count);
        if (is String image) {
            grove.image = image;
        }
        map.addFixture(Point(row, column), grove);
    }
    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "groves", curry(readGrove)(map),
                """SELECT * FROM groves""");
}