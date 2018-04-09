import strategicprimer.model.map.fixtures.resources {
	CacheFixture
}
import strategicprimer.model.map {
	Point
}
import ceylon.dbc {
	Sql
}
object dbCacheWriter satisfies DatabaseWriter<CacheFixture, Point> {
	shared actual void write(Sql db, CacheFixture obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS caches (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                id INTEGER NOT NULL,
			                kind VARCHAR(32) NOT NULL,
			                contents VARCHAR(512) NOT NULL,
			                image VARCHAR(256) NOT NULL
		                )""").execute();
		db.Insert(
			"""INSERT INTO caches (row, column, id, kind, contents, image) VALUES(?, ?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, obj.id, obj.kind, obj.contents, obj.image);
	}
}