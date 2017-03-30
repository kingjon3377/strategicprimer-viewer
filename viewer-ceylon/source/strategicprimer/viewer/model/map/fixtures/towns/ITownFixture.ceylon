import strategicprimer.viewer.model.map {
    TileFixture,
    HasMutableOwner
}
import model.map {
    HasName,
    HasPortrait
}
"An interface for towns and similar fixtures."
shared interface ITownFixture satisfies TileFixture&HasName&HasMutableOwner&HasPortrait {
    "The status of the town."
    shared formal TownStatus status;
    "The size of the town."
    shared formal TownSize townSize;
    """A description of what kind of "town" this is."""
    shared formal String kind;
}