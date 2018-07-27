import ceylon.dbc {
	Sql,
	SqlNull
}

import strategicprimer.model.map {
	Point,
	IMutableMapNG,
	pointFactory
}
import strategicprimer.model.map.fixtures.resources {
	CacheFixture
}
import strategicprimer.model.xmlio {
	Warning
}
object dbCacheHandler extends AbstractDatabaseWriter<CacheFixture, Point>()
		satisfies MapContentsReader {
	shared actual {String+} initializers =
			["""CREATE TABLE IF NOT EXISTS caches (
				    row INTEGER NOT NULL,
				    column INTEGER NOT NULL,
				    id INTEGER NOT NULL,
				    kind VARCHAR(32) NOT NULL,
				    contents VARCHAR(512) NOT NULL,
				    image VARCHAR(256)
			    );"""];
	shared actual void write(Sql db, CacheFixture obj, Point context) {
		db.Insert(
			"""INSERT INTO caches (row, column, id, kind, contents, image)
			   VALUES(?, ?, ?, ?, ?, ?);""")
				.execute(context.row, context.column, obj.id, obj.kind, obj.contents, obj.image);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		log.trace("About to read caches");
		variable Integer count = 0;
		for (dbRow in db.Select("""SELECT * FROM caches""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
				is Integer id = dbRow["id"], is String kind = dbRow["kind"],
				is String contents = dbRow["contents"], is String|SqlNull image = dbRow["image"]);
			value cache = CacheFixture(kind, contents, id);
			if (is String image) {
				cache.image = image;
			}
			map.addFixture(pointFactory(row, column), cache);
			count++;
			if (50.divides(count)) {
				log.trace("Finished reading ``count`` caches");
			}
		}
		log.trace("Finished reading caches");
	}
}