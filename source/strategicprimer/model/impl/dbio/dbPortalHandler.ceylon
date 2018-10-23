import ceylon.dbc {
    Sql,
    SqlNull
}

import java.sql {
    Types
}

import strategicprimer.model.common.map {
    Point,
    IMutableMapNG
}

import strategicprimer.model.common.map.fixtures.explorable {
    Portal
}
import strategicprimer.model.common.xmlio {
    Warning
}

import lovelace.util.common {
    as
}

object dbPortalHandler extends AbstractDatabaseWriter<Portal, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS portals (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               id INTEGER NOT NULL,
               image VARCHAR(255),
               destination_world VARCHAR(16),
               destination_row INTEGER,
               destination_column INTEGER
                   CHECK ((destination_row IS NOT NULL AND destination_column IS NOT NULL)
                       OR (destination_row IS NULL AND destination_column IS NULL))
           );"""
    ];

    shared actual void write(Sql db, Portal obj, Point context) {
        Integer[2]|SqlNull[2] destinationCoordinates;
        if (obj.destinationCoordinates.valid) {
            destinationCoordinates = [obj.destinationCoordinates.row,
                obj.destinationCoordinates.column];
        } else {
            destinationCoordinates = [SqlNull(Types.integer), SqlNull(Types.integer)];
        }
        db.Insert(
            """INSERT INTO portals (row, column, id, image, destination_world,
                   destination_row, destination_column)
               VALUES(?, ?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.id, obj.image,
                    obj.destinationWorld, *destinationCoordinates);
    }

    void readPortal(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"],
            is String|SqlNull destinationWorld = dbRow["destination_world"],
            is Integer|SqlNull destinationRow = dbRow["destination_row"],
            is Integer|SqlNull destinationColumn = dbRow["destination_column"],
            is String|SqlNull image = dbRow["image"]);
        value portal = Portal(as<String>(destinationWorld) else "unknown",
            Point(as<Integer>(destinationRow) else -1,
                as<Integer>(destinationColumn) else -1), id);
        if (is String image) {
            portal.image = image;
        }
        map.addFixture(Point(row, column), portal);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "portals", curry(readPortal)(map),
                """SELECT * FROM portals""");
}
