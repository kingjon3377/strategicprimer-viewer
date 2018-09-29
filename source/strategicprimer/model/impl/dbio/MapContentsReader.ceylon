import ceylon.dbc {
    Sql
}

import strategicprimer.model.common.map {
    IFixture,
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.common.xmlio {
    Warning
}
"An interface for code to read map contents from an SQL database."
interface MapContentsReader {
    "Read map direct contents---that is, anything directly at a location on the map."
    shared formal void readMapContents(Sql db, IMutableMapNG map, Warning warner);
    "Read non-direct contents---that is, unit and fortress members and the like. Because
     in many cases this doesn't apply, it's by default a noop."
    shared default void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) {}
    "Find a tile fixture or unit or fortress member within a given stream of such objects
     by its ID, if present."
    shared default IFixture? findByIdImpl({IFixture*} stream, Integer id) {
        for (fixture in stream) {
            if (fixture.id == id) {
                return fixture;
            } else if (is {IFixture*} fixture,
                    exists retval = findByIdImpl(fixture, id)) {
                return retval;
            }
        }
        return null;
    }
    "Find a tile fixture or unit or fortress member by ID."
    shared default IFixture findById(IMapNG map, Integer id, Warning warner) =>
            dbMemoizer.findById(map, id, this, warner);
    "Run the given method on each row returned by the given query."
    shared default void handleQueryResults(Sql db, Warning warner, String description,
            Anything(Map<String, Object>, Warning) handler, String query, Object* args) {
        log.trace("About to read ``description``");
        variable Integer count = 0;
        for (row in db.Select(query).Results(*args)) {
            handler(row, warner);
            count++;
            if (50.divides(count)) {
                log.trace("Finished reading 50 ``description``");
            }
        }
        log.trace("Finished reading ``description``");
    }
}