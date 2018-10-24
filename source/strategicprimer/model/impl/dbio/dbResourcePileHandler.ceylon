import ceylon.dbc {
    Sql,
    SqlNull
}
import ceylon.decimal {
    parseDecimal
}

import strategicprimer.model.common.map {
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile,
    Quantity
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

object dbResourcePileHandler
        extends AbstractDatabaseWriter<ResourcePile, IUnit|Fortress>()
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

    shared actual void write(Sql db, ResourcePile obj, IUnit|Fortress context) =>
        db.Insert("""INSERT INTO resource_piles (parent, id, kind, contents, quantity,
                         units, created, image)
                     VALUES(?, ?, ?, ?, ?, ?, ?, ?);""")
            .execute(context.id, obj.id, obj.kind, obj.contents,
                        obj.quantity.number.string, obj.quantity.units, obj.created,
                        obj.image);

    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {}

    void readResourcePile(IMutableMapNG map, Map<String, Object> row, Warning warner) {
        assert (is Integer parentId = row["parent"],
            is IUnit|Fortress parent = findById(map, parentId, warner),
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
        value pile = ResourcePile(id, kind, contents, Quantity(quantity, units));
        if (is String image) {
            pile.image = image;
        }
        if (is Integer created) {
            pile.created = created;
        }
        if (is IUnit parent) {
            parent.addMember(pile);
        } else {
            parent.addMember(pile);
        }
    }

    shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "resource piles", curry(readResourcePile)(map),
                """SELECT * FROM resource_piles""");
}
