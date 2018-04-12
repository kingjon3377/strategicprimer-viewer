import ceylon.dbc {
	Sql
}
import ceylon.decimal {
	parseDecimal
}

import strategicprimer.model.map {
	IMutableMapNG
}
import strategicprimer.model.map.fixtures {
	ResourcePile,
	Quantity
}
import strategicprimer.model.map.fixtures.mobile {
	IUnit
}
import strategicprimer.model.map.fixtures.towns {
	Fortress
}
import strategicprimer.model.xmlio {
	Warning
}
object dbResourcePileHandler extends AbstractDatabaseWriter<ResourcePile, IUnit|Fortress>() satisfies MapContentsReader {
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
		   )"""
	];
	shared actual void write(Sql db, ResourcePile obj, IUnit|Fortress context) {
		db.Insert("""INSERT INTO resource_piles (parent, id, kind, contents, quantity, units, created, image)
		             VALUES(?, ?, ?, ?, ?, ?, ?, ?)""").execute(context.id, obj.id, obj.kind, obj.contents,
						obj.quantity.number.string, obj.quantity.units, obj.created, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {}
	shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) {
		for (row in db.Select("""SELECT * FROM resource_piles""").Results()) {
			assert (is Integer parentId = row["parent"], is IUnit|Fortress parent = findById(map, parentId, warner),
				is Integer id = row["id"], is String kind = row["kind"], is String contents = row["contents"],
				is String qtyString = row["quantity"], is String units = row ["units"],
				is Integer? created = row["created"], is String? image = row["image"]);
			Number<out Anything> quantity;
			if (is Integer num = Integer.parse(qtyString)) {
				quantity = num;
			} else {
				assert (exists num = parseDecimal(qtyString));
				quantity = num;
			}
			value pile = ResourcePile(id, kind, contents, Quantity(quantity, units));
			if (exists image) {
				pile.image = image;
			}
			if (is IUnit parent) {
				parent.addMember(pile);
			} else {
				parent.addMember(pile);
			}
		}
	}
}