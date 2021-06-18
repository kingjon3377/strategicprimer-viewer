import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
    IFixture,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures {
    Implement
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.common.map.fixtures.towns {
    IFortress
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

object dbImplementHandler extends AbstractDatabaseWriter<Implement, IUnit|IFortress>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS implements (
               parent INTEGER NOT NULL,
               id INTEGER NOT NULL,
               kind VARCHAR(255) NOT NULL,
               count INTEGER NOT NULL DEFAULT 1,
               image VARCHAR(255)
           );"""
    ];

    shared actual void write(Sql db, Implement obj, IUnit|IFortress context) {
        db.Insert("""INSERT INTO implements (parent, id, kind, count, image)
                     VALUES(?, ?, ?, ?, ?);""")
                .execute(context.id, obj.id, obj.kind, obj.count, obj.image);
    }

    void readImplement(IMutableMapNG map,
            MutableMultimap<Integer, Object> containees)(Map<String, Object> row,
            Warning warner) {
        assert (is Integer parentId = row["parent"],
            is Integer id = row["id"], is String kind = row["kind"],
            is Integer count = row["count"], is String|SqlNull image = row["image"]);
        value implement = Implement(kind, id, count);
        if (is String image) {
            implement.image = image;
        }
        containees.put(parentId, implement);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, MutableMap<Integer, IFixture> containers,
            MutableMultimap<Integer, Object> containees, Warning warner) =>
                handleQueryResults(db, warner, "pieces of equipment",
                    readImplement(map, containees), """SELECT * FROM implements""");
}
