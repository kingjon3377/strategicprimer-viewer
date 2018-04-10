import strategicprimer.model.map.fixtures.mobile {
	Animal,
	IUnit,
	maturityModel
}
import ceylon.dbc {
	Sql,
	SqlNull
}
import strategicprimer.model.map {
	Point
}
import strategicprimer.model.map.fixtures.towns {
	Fortress
}
import java.sql {
	Types
}
object dbAnimalWriter extends AbstractDatabaseWriter<Animal, Point|IUnit|Fortress>() {
	Integer|SqlNull born(Animal animal) {
		if (exists maturityAge = maturityModel.maturityAges[animal.kind],
				maturityAge <= (currentTurn - animal.born)) {
			return SqlNull(Types.integer);
		} else {
			return animal.born;
		}
	}
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS animals (
			   row INTEGER,
			   column INTEGER
				   CHECK ((row NOT NULL AND column NOT NULL) OR
					   (row IS NULL AND column IS NULL)),
			   parent INTEGER
				   CHECK ((row NOT NULL AND parent IS NULL) OR
					   (row IS NULL AND parent NOT NULL)),
			   kind VARCHAR(32) NOT NULL,
			   talking BOOLEAN NOT NULL,
			   status VARCHAR(32) NOT NULL,
			   born INTEGER,
			   count INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   image VARCHAR(255)
		   )""",
		// We assume that animal tracks can't occur inside a unit or fortress, and ignore their 'domestication status',
		// 'talking', 'born', and 'count'. We also follow the XML I/O framework in discarding their IDs.
		"""CREATE TABLE IF NOT EXISTS tracks (
			   row INTEGER NOT NULL,
			   column INTEGER NOT NULL,
			   kind VARCHAR(32) NOT NULL,
			   image VARCHAR(255)
		   )"""
	];
	shared actual void write(Sql db, Animal obj, Point|IUnit|Fortress context) {
		if (obj.traces) {
			"We assume that animal tracks can't occur inside a unit or fortress."
			assert (is Point context);
			db.Insert("""INSERT INTO tracks (row, column, kind, image) VALUES(?, ?, ?, ?)""")
					.execute(context.row, context.column, obj.kind, obj.image);
		} else {
			value insertion = db.Insert(
				"""INSERT INTO animals (row, column, parent, kind, talking, status, born, count, id, image)
				   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""");
			if (is Point context) {
				insertion.execute(context.row, context.column, SqlNull(Types.integer),
					obj.kind, obj.talking, obj.status, born(obj), obj.population, obj.id, obj.image);
			} else {
				insertion.execute(SqlNull(Types.integer), SqlNull(Types.integer), context.id,
					obj.kind, obj.talking, obj.status, born(obj), obj.population, obj.id, obj.image);
			}
		}
	}
}