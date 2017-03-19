import model.map {
    TileFixture
}
"An interface for a filter to tell whether a given fixture should be displayed."
shared interface ZOrderFilter {
    "Whether the fixture should be displayed."
    shared formal Boolean shouldDisplay(TileFixture fixture);
}