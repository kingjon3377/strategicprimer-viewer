import ceylon.dbc {
	Sql,
	SqlNull
}

import java.sql {
	Types
}

import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures.towns {
	AbstractTown
}
object dbTownWriter satisfies DatabaseWriter<AbstractTown, Point> {
	shared actual void write(Sql db, AbstractTown obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS towns (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                id INTEGER NOT NULL,
			                status VARCHAR(9) NOT NULL CHECK(status IN ('abandoned', 'active', 'burned', 'ruined')),
			                size VARCHAR(6) NOT NULL CHECK(size IN ('small', 'medium', 'large')),
			                dc INTEGER,
			                name VARCHAR(128) NOT NULL,
			                owner INTEGER NOT NULL,
			                image VARCHAR(255),
			                portrait VARCHAR(255),
			                population INTEGER
		                )""").execute();
		db.Insert("""INSERT INTO towns (row, column, id, status, size, dc, name, owner, image, portrait, population)
		             VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, obj.status.string, obj.townSize.string,
					obj.dc, obj.name, obj.owner.playerId, obj.image, obj.portrait,
					obj.population?.population else SqlNull(Types.integer));
		if (exists stats = obj.population) {
			dbCommunityStatsWriter.write(db, stats, obj);
		}
	}
}