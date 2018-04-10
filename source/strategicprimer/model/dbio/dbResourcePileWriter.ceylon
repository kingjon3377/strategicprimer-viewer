import strategicprimer.model.map.fixtures {
	ResourcePile
}
import strategicprimer.model.map.fixtures.mobile {
	IUnit
}
import strategicprimer.model.map.fixtures.towns {
	Fortress
}
import ceylon.dbc {
	Sql
}
object dbResourcePileWriter extends AbstractDatabaseWriter<ResourcePile, IUnit|Fortress>() {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS resource_piles (
			   parent INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   kind VARCHAR(64) NOT NULL,
			   contents VARCHAR(64) NOT NULL,
			   quantity VARCHAR(128) NOT NULL
				   CHECK (quantity NOT LIKE '%[^0-9.]%' AND quantity NOT LIKE '%.%.%'),
			   units VARCHAR(32) NOT NULL,
			   created INTEGER
		   )"""
	];
	shared actual void write(Sql db, ResourcePile obj, IUnit|Fortress context) {
		db.Insert("""INSERT INTO resource_piles (parent, id, kind, contents, quantity, units, created)
		             VALUES(?, ?, ?, ?, ?, ?, ?)""").execute(context.id, obj.id, obj.kind, obj.contents,
						obj.quantity.number.string, obj.quantity.units, obj.created);
	}
}