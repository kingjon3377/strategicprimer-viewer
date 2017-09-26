import strategicprimer.model.map {
    TileFixture
}
"""An interface for fixtures representing things that were on the original table of
   numeric "events" used for version-0 maps."""
shared interface IEvent satisfies TileFixture {
    "Exploration-result text describing the event."
    shared formal String text;
}
