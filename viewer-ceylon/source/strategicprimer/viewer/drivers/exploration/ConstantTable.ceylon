import strategicprimer.viewer.model.map {
    TileType
}
import model.map {
    Point,
    TileFixture,
    MapDimensions
}
"An [[EncounterTable]] that always returns the same value."
class ConstantTable(String constant) satisfies EncounterTable {
    shared actual String generateEvent(Point point, TileType terrain,
            {TileFixture*} fixtures, MapDimensions dimensions) => constant;
    shared actual Set<String> allEvents => set { constant };
    shared actual String string => "ConstantTable: ``constant``";
}