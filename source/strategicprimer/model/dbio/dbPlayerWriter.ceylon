import strategicprimer.model.map {
	Player,
	IMapNG
}
import ceylon.dbc {
	Sql
}
object dbPlayerWriter satisfies DatabaseWriter<Player, IMapNG> {
	shared actual void write(Sql db, Player obj, IMapNG context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS players (
			                id INTEGER,
			                codename VARCHAR(64) NOT NULL,
			                current BOOLEAN NOT NULL
		                )""").execute();
		db.Insert("""INSERT INTO players (id, codename, current) VALUES(?, ?, ?)""")
				.execute(obj.playerId, obj.name, obj.current);
	}
}