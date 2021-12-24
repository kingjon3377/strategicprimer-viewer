import ceylon.collection {
    Queue,
    LinkedList
}

import strategicprimer.model.common.map {
    Point,
    TileType,
    TileFixture,
    MapDimensions
}

"A mock [[EncounterTable]] for the apparatus to test the ExplorationRunner, to
 produce the events the tests want in the order they want, and guarantee that
 the runner never calls [[allEvents]]."
class MockTable(String* values) satisfies EncounterTable {
    Queue<String> queue = LinkedList<String>(values);

    shared actual String generateEvent(Point point, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions mapDimensions) {
        assert (exists retval = queue.accept());
        return retval;
    }

    suppressWarnings("expressionTypeNothing")
    shared actual Set<String> allEvents => nothing;
}
