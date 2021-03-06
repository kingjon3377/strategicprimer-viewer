import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
    Point,
    IFixture,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.resources {
    Mine
}
import strategicprimer.model.common.map.fixtures.towns {
    TownStatus
}
import strategicprimer.model.common.xmlio {
    Warning
}

import ceylon.collection {
    MutableMap
}

import com.vasileff.ceylon.structures {
    MutableMultimap
}

object dbMineHandler extends AbstractDatabaseWriter<Mine, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS mines (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               id INTEGER NOT NULL,
               kind VARCHAR(128) NOT NULL,
               status VARCHAR(9) NOT NULL
                   CHECK(status IN ('abandoned', 'active', 'burned', 'ruined')),
               image VARCHAR(255)
           );"""
    ];

    shared actual void write(Sql db, Mine obj, Point context) =>
        db.Insert("""INSERT INTO mines (row, column, id, kind, status, image)
                     VALUES(?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.id, obj.kind,
                    obj.status.string, obj.image);

    void readMine(IMutableMapNG map)(Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String kind = dbRow["kind"],
            is String statusString = dbRow["status"],
            is TownStatus status = TownStatus.parse(statusString),
            is String|SqlNull image = dbRow["image"]);
        value mine = Mine(kind, status, id);
        if (is String image) {
            mine.image = image;
        }
        map.addFixture(Point(row, column), mine);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, MutableMap<Integer, IFixture> containers,
            MutableMultimap<Integer, Object> containees, Warning warner) =>
                handleQueryResults(db, warner, "mines", readMine(map),
                    """SELECT * FROM mines""");
}
