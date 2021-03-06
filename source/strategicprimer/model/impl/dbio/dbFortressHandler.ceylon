import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
    Point,
    IFixture,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.towns {
    FortressImpl,
    IFortress,
    TownSize
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

object dbFortressHandler extends AbstractDatabaseWriter<IFortress, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS fortresses (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               owner INTEGER NOT NULL,
               name VARCHAR(64) NOT NULL,
               size VARCHAR(6) NOT NULL
                   CHECK(size IN ('small', 'medium', 'large')),
               id INTEGER NOT NULL,
               image VARCHAR(255),
               portrait VARCHAR(255)
           );"""
    ];

    shared actual void write(Sql db, IFortress obj, Point context) {
        db.Insert("""INSERT INTO fortresses (row, column, owner, name, size, id, image,
                          portrait)
                     VALUES(?, ?, ?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.owner.playerId, obj.name,
                    obj.townSize.string, obj.id, obj.image, obj.portrait);
        for (member in obj) {
            spDatabaseWriter.writeSPObjectInContext(db, member, obj);
        }
    }

    void readFortress(IMutableMapNG map,
            MutableMap<Integer, IFixture> containers)(Map<String, Object> dbRow,
            Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer ownerId = dbRow["owner"], is String name = dbRow["name"],
            is String sizeString = dbRow["size"],
            is TownSize size = TownSize.parse(sizeString), is Integer id = dbRow["id"],
            is String|SqlNull image = dbRow["image"],
            is String|SqlNull portrait = dbRow["portrait"]);
        value fortress = FortressImpl(map.players.getPlayer(ownerId), name, id, size);
        if (is String image) {
            fortress.image = image;
        }
        if (is String portrait) {
            fortress.portrait = portrait;
        }
        map.addFixture(Point(row, column), fortress);
        containers.put(id, fortress);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, MutableMap<Integer, IFixture> containers,
            MutableMultimap<Integer, Object> containees, Warning warner) =>
                handleQueryResults(db, warner, "fortresses", readFortress(map, containers),
                    """SELECT * FROM fortresses""");
}
