import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures.towns {
	Village
}
import ceylon.dbc {
	Sql,
	SqlNull
}
import java.sql {
	Types
}
object dbVillageWriter extends AbstractDatabaseWriter<Village, Point>() {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS villages (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   status VARCHAR(9) NOT NULL
				   CHECK(status IN ('abandoned', 'active', 'burned', 'ruined')),
			   name VARCHAR(128) NOT NULL,
			   id INTEGER NOT NULL,
			   owner INTEGER NOT NULL,
			   race VARCHAR(32) NOT NULL,
			   image VARCHAR(255),
			   portrait VARCHAR(255),
			   population INTEGER
		   )"""
	];
	shared actual void write(Sql db, Village obj, Point context) {
		db.Insert("""INSERT INTO villages (row, column, status, name, id, owner, race, image, portrait, population)
		             VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.status.string, obj.name, obj.id, obj.owner.playerId,
					obj.race, obj.image, obj.portrait, obj.population?.population else SqlNull(Types.integer));
		if (exists stats = obj.population) {
			dbCommunityStatsWriter.initialize(db);
			dbCommunityStatsWriter.write(db, stats, obj);
		}
	}
}