import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures {
    Implement
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import strategicprimer.model.common.xmlio {
    Warning
}

object dbImplementHandler extends AbstractDatabaseWriter<Implement, IUnit|Fortress>()
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

    shared actual void write(Sql db, Implement obj, IUnit|Fortress context) {
        db.Insert("""INSERT INTO implements (parent, id, kind, count, image)
                     VALUES(?, ?, ?, ?, ?);""")
                .execute(context.id, obj.id, obj.kind, obj.count, obj.image);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {}

    void readImplement(IMutableMapNG map, Map<String, Object> row, Warning warner) {
        assert (is Integer parentId = row["parent"],
            is IUnit|Fortress parent = findById(map, parentId, warner),
            is Integer id = row["id"], is String kind = row["kind"],
            is Integer count = row["count"], is String|SqlNull image = row["image"]);
        value implement = Implement(kind, id, count);
        if (is String image) {
            implement.image = image;
        }
        if (is IUnit parent) {
            parent.addMember(implement);
        } else {
            parent.addMember(implement);
        }
    }

    shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "pieces of equipment",
                curry(readImplement)(map), """SELECT * FROM implements""");
}
