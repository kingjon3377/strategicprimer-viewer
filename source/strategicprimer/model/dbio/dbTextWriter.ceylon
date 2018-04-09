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
import strategicprimer.model.map.fixtures {
	TextFixture
}
object dbTextWriter satisfies DatabaseWriter<TextFixture, Point> {
	shared actual void write(Sql db, TextFixture obj, Point context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS text_notes (
			                row INTEGER NOT NULL,
			                column INTEGER NOT NULL,
			                turn INTEGER,
			                text VARCHAR(1024) NOT NULL,
			                image VARCHAR(255)
		                )""").execute();
		Integer|SqlNull turn;
		if (obj.turn >= 0) {
			turn = obj.turn;
		} else {
			turn = SqlNull(Types.integer);
		}
		db.Insert("""INSERT INTO text_notes (row, column, turn, text, image) VALUES(?, ?, ?, ?, ?)""")
				.execute(context.row, context.column, turn, obj.text, obj.image);
	}
}