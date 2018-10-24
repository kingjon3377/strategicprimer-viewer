import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
    Point,
    IMutableMapNG
}

import strategicprimer.model.common.map.fixtures.resources {
    Shrub
}
import strategicprimer.model.common.xmlio {
    Warning
}

import lovelace.util.common {
    as
}

object dbShrubHandler extends AbstractDatabaseWriter<Shrub, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS shrubs (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               id INTEGER NOT NULL,
               kind VARCHAR(64) NOT NULL,
               count INTEGER,
               image VARCHAR(255)
           );"""
    ];

    shared actual void write(Sql db, Shrub obj, Point context) =>
        db.Insert("""INSERT INTO shrubs (row, column, id, kind, count, image)
                     VALUES(?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.id, obj.kind,
                    obj.population, obj.image);

    void readShrub(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String kind = dbRow["kind"],
            is Integer|SqlNull count = dbRow["count"],
            is String|SqlNull image = dbRow["image"]);
        value shrub = Shrub(kind, id, as<Integer>(count) else -1);
        if (is String image) {
            shrub.image = image;
        }
        map.addFixture(Point(row, column), shrub);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "shrubs", curry(readShrub)(map),
                """SELECT * FROM shrubs""");
}
