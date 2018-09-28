import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.impl.map {
    Point,
    IMutableMapNG,
    TileFixture,
    HasMutableImage
}
import strategicprimer.model.impl.map.fixtures.terrain {
    Hill,
    Oasis
}
import strategicprimer.model.impl.xmlio {
    Warning
}
object dbSimpleTerrainHandler extends AbstractDatabaseWriter<Hill|Oasis, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS simple_terrain (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               type VARCHAR(7) NOT NULL
                   CHECK(type IN('hill', 'oasis', 'sandbar')),
               id INTEGER NOT NULL,
               image VARCHAR(255)
           );"""
    ];
    shared actual void write(Sql db, Hill|Oasis obj, Point context) {
        String type;
        switch (obj)
        case (is Hill) {
            type = "hill";
        }
        case (is Oasis) {
            type = "oasis";
        }
        db.Insert("""INSERT INTO simple_terrain (row, column, type, id, image)
                     VALUES(?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, type, obj.id, obj.image);
    }
    void readSimpleTerrain(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is String type = dbRow["type"], is Integer id = dbRow["id"],
            is String|SqlNull image = dbRow["image"]);
        TileFixture&HasMutableImage fixture;
        switch (type)
        case ("hill") {
            fixture = Hill(id);
        }
        case ("sandbar") {
            log.info("Ignoring 'sandbar' with ID ``id```");
            return;
        }
        case ("oasis") {
            fixture = Oasis(id);
        }
        else {
            throw AssertionError("Unhandled simple terrain-fixture type");
        }
        if (is String image) {
            fixture.image = image;
        }
        map.addFixture(Point(row, column), fixture);
    }
    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "simple terrain fixtures",
                curry(readSimpleTerrain)(map), """SELECT * FROM simple_terrain""");
}