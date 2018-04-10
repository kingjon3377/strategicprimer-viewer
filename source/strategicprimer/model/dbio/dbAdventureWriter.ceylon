import strategicprimer.model.map.fixtures.explorable {
	AdventureFixture
}
import ceylon.dbc {
	Sql
}
import strategicprimer.model.map {
	Point
}
object dbAdventureWriter extends AbstractDatabaseWriter<AdventureFixture, Point>() {
	shared actual {String+} initializers =  // TODO: Increase 'full' buffer? Make 'owner' NOT NULL?
			["""CREATE TABLE IF NOT EXISTS adventures (
				    row INTEGER NOT NULL,
				    column INTEGER NOT NULL,
				    id INTEGER NOT NULL,
				    brief VARCHAR(255),
				    full VARCHAR(255),
				    owner INTEGER,
				    image VARCHAR(255)
			    )"""];
	shared actual void write(Sql db, AdventureFixture obj, Point context) {
			db.Insert("""INSERT INTO adventures (row, column, id, brief, full, owner, image)
			             VALUES(?, ?, ?, ?, ?, ?, ?)""")
					.execute(context.row, context.column, obj.id, obj.briefDescription,
						obj.fullDescription, obj.owner.playerId, obj.image);
	}
}