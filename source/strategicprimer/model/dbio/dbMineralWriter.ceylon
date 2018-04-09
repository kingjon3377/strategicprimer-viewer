import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures.resources {
	MineralVein,
	StoneDeposit
}
import ceylon.dbc {
	Sql
}
object dbMineralWriter satisfies DatabaseWriter<MineralVein|StoneDeposit, Point> {
	shared actual void write(Sql db, MineralVein|StoneDeposit obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS minerals (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                type VARCHAR(7) NOT NULL CHECK(type IN('stone', 'mineral')),
			                id INTEGER NOT NULL,
			                kind VARCHAR(64) NOT NULL,
			                exposed BOOLEAN NOT NULL CHECK(exposed OR type IN('mineral')),
			                dc INTEGER NOT NULL,
			                image VARCHAR(255)
		                )""").execute();
		String type;
		Boolean exposed;
		switch (obj)
		case (is MineralVein) {
			type = "mineral";
			exposed = obj.exposed;
		}
		case (is StoneDeposit) {
			type = "stone";
			exposed = true;
		}
		db.Insert(
			"""INSERT INTO minerals (row, column, type, id, kind, exposed, dc, image)
			   VALUES(?, ?, ?, ?, ?, ?, ?, ?)""").execute(context.row, context.column, type,
					obj.id, obj.kind, exposed, obj.dc, obj.image);
	}
}