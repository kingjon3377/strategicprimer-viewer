import strategicprimer.model.map {
    TileFixture,
    HasMutableOwner,
    HasPortrait,
    HasName
}
"An interface for towns and similar fixtures."
shared interface ITownFixture satisfies TileFixture&HasName&HasMutableOwner&HasPortrait {
    "The status of the town."
    shared formal TownStatus status;
    "The size of the town."
    shared formal TownSize townSize;
    """A description of what kind of "town" this is."""
    shared formal String kind;
    "A summary of the town's contents."
    shared formal CommunityStats? population;
}