import strategicprimer.model.common.map {
    TileFixture,
    HasPortrait,
    HasName,
    HasOwner
}

"An interface for towns and similar fixtures."
shared interface ITownFixture satisfies TileFixture&HasName&HasOwner&HasPortrait {
    "The status of the town."
    shared formal TownStatus status;

    "The size of the town."
    shared formal TownSize townSize;

    """A description of what kind of "town" this is."""
    shared formal String kind;

    "A summary of the town's contents."
    shared formal CommunityStats? population;
}
