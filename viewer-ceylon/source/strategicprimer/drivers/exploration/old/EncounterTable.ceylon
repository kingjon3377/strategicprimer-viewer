import strategicprimer.model.map {
    TileFixture,
    TileType,
    MapDimensions,
    Point
}
"An interface for encounter tables, for the now-nearly-defunct second model of generating
 results. This class's methods produce data for the Judge's use; to produce results for a
 player we would need to know the explorer's Perception modifier and perhaps other data."
shared interface EncounterTable {
    """Generates an appropriate event, an "encounter." For QuadrantTAbles this is always
       the same for each tile, for random-event tables the result is randomly selected
       from that table, and so on."""
    shared formal String generateEvent(
            "The location of the tile in question."
            Point point,
            "The terrain there."
            TileType terrain,
            "The fixtures on the tile, if any."
            {TileFixture*} fixtures,
            "The dimensions of the map."
            MapDimensions mapDimensions);
    "For table-debugging purposes, return the set of all events the table can return."
    shared formal Set<String> allEvents;
}