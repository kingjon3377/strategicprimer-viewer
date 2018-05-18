import strategicprimer.model.map {
    TileFixture,
    TileType,
    MapDimensions,
    Point
}
"An [[EncounterTable]] that always returns the same value."
class ConstantTable(String constant) satisfies EncounterTable {
    shared actual String generateEvent(Point point, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions dimensions) =>
                constant;
    shared actual Set<String> allEvents => set { constant };
    shared actual String string => "ConstantTable: ``constant``";
}
