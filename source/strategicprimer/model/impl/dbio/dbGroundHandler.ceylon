import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
    Point,
    IFixture,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures {
    Ground
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

object dbGroundHandler extends AbstractDatabaseWriter<Ground, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS ground (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               id INTEGER NOT NULL,
               kind VARCHAR(32) NOT NULL,
               exposed BOOLEAN NOT NULL,
               image VARCHAR(255)
           );"""
    ];

    shared actual void write(Sql db, Ground obj, Point context) {
        db.Insert("""INSERT INTO ground (row, column, id, kind, exposed, image)
                     VALUES(?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.id, obj.kind, obj.exposed,
                    obj.image);
    }

    void readGround(IMutableMapNG map)(Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String kind = dbRow["kind"],
            is Boolean exposed = dbMapReader.databaseBoolean(dbRow["exposed"]),
            is String|SqlNull image = dbRow["image"]);
        value ground = Ground(id, kind, exposed);
        if (is String image) {
            ground.image = image;
        }
        map.addFixture(Point(row, column), ground);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, MutableMap<Integer, IFixture> containers,
            MutableMultimap<Integer, Object> containees, Warning warner) =>
                handleQueryResults(db, warner, "ground", readGround(map),
                    """SELECT * FROM ground""");
}
