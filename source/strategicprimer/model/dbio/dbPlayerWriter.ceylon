import strategicprimer.model.map {
	Player,
	IMapNG
}
import ceylon.dbc {
	Sql
}
object dbPlayerWriter extends AbstractDatabaseWriter<Player, IMapNG>() {
	shared actual {String+} initializers = [ // TODO: id should be NOT NULL, but that gave problems for some reason
		"""CREATE TABLE IF NOT EXISTS players (
			   id INTEGER,
			   codename VARCHAR(64) NOT NULL,
			   current BOOLEAN NOT NULL
		   )"""
	];
	shared actual void write(Sql db, Player obj, IMapNG context) {
		db.Insert("""INSERT INTO players (id, codename, current) VALUES(?, ?, ?)""")
				.execute(obj.playerId, obj.name, obj.current);
	}
}