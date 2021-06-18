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

import ceylon.collection {
    MutableMap
}

import com.vasileff.ceylon.structures {
    MutableMultimap
}

"An interface for code to read map contents from an SQL database."
interface MapContentsReader {
    "Read map direct contents---that is, anything directly at a location on the map."
    shared formal void readMapContents("The database to read from"
                                       Sql db,
                                       "The map we're reading"
                                       IMutableMapNG map,
                                       "A map by ID of fixtures that can contain others, to connect later."
                                       MutableMap<Integer, IFixture> containers,
                                       "A multimap by container ID of fixtures that are contained in other fixtures, to be added to their containers later."
                                       MutableMultimap<Integer, Object> containees,
                                       "Warning instance to use"
                                       Warning warner);

    "Read non-direct contents---that is, unit and fortress members and the like. Because
     in many cases this doesn't apply, it's by default a noop."
    deprecated("Everything should go in [[readMapContents]] and be fixed up separately.")
    shared default void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) {}

    "Find a tile fixture or unit or fortress member within a given stream of such objects
     by its ID, if present."
    deprecated("I think we're going to get rid of this")
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
    deprecated("I expect this won't be used anymore?")
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
