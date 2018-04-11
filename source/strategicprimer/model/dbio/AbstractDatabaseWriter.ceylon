import ceylon.collection {
	MutableSet,
	HashSet
}
import ceylon.dbc {
	Sql
}
abstract class AbstractDatabaseWriter<in Item, in Context>()
		satisfies DatabaseWriter<Item, Context> given Item satisfies Object given Context satisfies Object {
	"SQL to run to initialize the needed tables."
	shared formal {String+} initializers;
	"Database connections that we've been initialized for."
	MutableSet<Sql> connections = HashSet<Sql>();
	shared actual void initialize(Sql sql) {
		if (!connections.contains(sql)) {
			sql.transaction(() {
				for (initializer in initializers) {
					sql.Statement(initializer).execute();
				}
				connections.add(sql);
				return true;
			});
		}
	}
}