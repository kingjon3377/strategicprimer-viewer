import ceylon.dbc {
	Sql
}

import strategicprimer.model.idreg {
	DuplicateIDException
}
import strategicprimer.model.map {
	IMutableMapNG,
	IFixture,
	IMapNG
}
import strategicprimer.model.xmlio {
	Warning
}
"An interface for code to read map contents from an SQL database."
interface MapContentsReader {
	"Read map direct contents---that is, anything directly at a location on the map."
	shared formal void readMapContents(Sql db, IMutableMapNG map, Warning warner);
	"Read non-direct contents---that is, unit and fortress members and the like. Because
	 in many cases this doesn't apply, it's by default a noop."
	shared default void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) {}
	"Expand any fixture that is itself a stream of fixtures to its contents plus itself."
	shared default {IFixture+} expand(IFixture fixture) {
		if (is {IFixture*} fixture) {
			return fixture.flatMap(expand).follow(fixture);
		} else {
			return Singleton(fixture);
		}
	}
	"Find a tile fixture or unit or fortress member by ID."
	shared default IFixture findById(IMapNG map, Integer id, Warning warner) {
		{IFixture*} retval = map.locations.flatMap(map.fixtures.get).flatMap(expand)
				.filter((fixture) => fixture.id == id);
		if (exists first = retval.first) {
			if (exists another = retval.rest.first) {
				warner.handle(DuplicateIDException(id));
			}
			return first;
		} else {
			throw AssertionError("ID ``id`` not found");
		}
	}
}