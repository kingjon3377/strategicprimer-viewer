import model.map {
    TileFixture,
    HasName,
    HasMutableOwner,
    HasPortrait
}
import model.map.fixtures.towns {
    TownStatus,
    TownSize
}
"An interface for towns and similar fixtures."
shared interface ITownFixture satisfies TileFixture&HasName&HasMutableOwner&HasPortrait {
    "The status of the town."
    shared formal TownStatus status;
    "The size of the town."
    shared formal TownSize size;
    """A description of what kind of "town" this is."""
    shared formal String kind;
}