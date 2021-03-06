import strategicprimer.model.common.map {
    TileType,
    Point,
    MapDimensions,
    TileFixture
}

"An [[EncounterTable]] that always returns the same value."
class ConstantTable(String constant) satisfies EncounterTable {
    shared actual String generateEvent(Point point, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions dimensions) =>
                constant;

    shared actual Set<String> allEvents => set(Singleton(constant));

    shared actual String string => "ConstantTable: ``constant``";
}
