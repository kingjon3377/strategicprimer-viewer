import ceylon.dbc {
    Sql,
    SqlNull
}
import ceylon.decimal {
    parseDecimal
}

import strategicprimer.model.common.map {
    IFixture,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures {
    IResourcePile,
    ResourcePileImpl,
    Quantity
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

object dbResourcePileHandler
        extends AbstractDatabaseWriter<IResourcePile, IUnit|IFortress>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS resource_piles (
               parent INTEGER NOT NULL,
               id INTEGER NOT NULL,
               kind VARCHAR(64) NOT NULL,
               contents VARCHAR(64) NOT NULL,
               quantity VARCHAR(128) NOT NULL
                   CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%'),
               units VARCHAR(32) NOT NULL,
               created INTEGER,
               image VARCHAR(255)
           );"""
    ];

    shared actual void write(Sql db, IResourcePile obj, IUnit|IFortress context) =>
        db.Insert("""INSERT INTO resource_piles (parent, id, kind, contents, quantity,
                         units, created, image)
                     VALUES(?, ?, ?, ?, ?, ?, ?, ?);""")
            .execute(context.id, obj.id, obj.kind, obj.contents,
                        obj.quantity.number.string, obj.quantity.units, obj.created,
                        obj.image);

    void readResourcePile(IMutableMapNG map,
            MutableMultimap<Integer, Object> containees)(Map<String, Object> row,
            Warning warner) {
        assert (is Integer parentId = row["parent"],
            is Integer id = row["id"], is String kind = row["kind"],
            is String contents = row["contents"],
            is String qtyString = row["quantity"], is String units = row ["units"],
            is Integer|SqlNull created = row["created"],
            is String|SqlNull image = row["image"]);
        Number<out Anything> quantity;
        if (is Integer num = Integer.parse(qtyString)) {
            quantity = num;
        } else {
            assert (exists num = parseDecimal(qtyString));
            quantity = num;
        }
        value pile = ResourcePileImpl(id, kind, contents, Quantity(quantity, units));
        if (is String image) {
            pile.image = image;
        }
        if (is Integer created) {
            pile.created = created;
        }
        containees.put(parentId, pile);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, MutableMap<Integer, IFixture> containers,
                MutableMultimap<Integer, Object> containees, Warning warner) =>
            handleQueryResults(db, warner, "resource piles", readResourcePile(map, containees),
                """SELECT * FROM resource_piles""");
}
