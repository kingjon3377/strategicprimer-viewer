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
    CacheFixture
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

object dbCacheHandler extends AbstractDatabaseWriter<CacheFixture, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers =
            ["""CREATE TABLE IF NOT EXISTS caches (
                    row INTEGER NOT NULL,
                    column INTEGER NOT NULL,
                    id INTEGER NOT NULL,
                    kind VARCHAR(32) NOT NULL,
                    contents VARCHAR(512) NOT NULL,
                    image VARCHAR(256)
                );"""];

    shared actual void write(Sql db, CacheFixture obj, Point context) =>
        db.Insert(
            """INSERT INTO caches (row, column, id, kind, contents, image)
               VALUES(?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.id, obj.kind, obj.contents,
                    obj.image);

    void readCache(IMutableMapNG map)(Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String kind = dbRow["kind"],
            is String contents = dbRow["contents"],
            is String|SqlNull image = dbRow["image"]);
        value cache = CacheFixture(kind, contents, id);
        if (is String image) {
            cache.image = image;
        }
        map.addFixture(Point(row, column), cache);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, MutableMap<Integer, IFixture> containers,
            MutableMultimap<Integer, Object> containees, Warning warner) =>
                handleQueryResults(db, warner, "caches", readCache(map),
                    """SELECT * FROM caches""");
}
