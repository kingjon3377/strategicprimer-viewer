import strategicprimer.model.common.map {
    TileFixture
}
"An interface for things that want to accept a new user-created tile fixture."
shared interface NewFixtureListener {
    "Add the new fixture."
    shared formal void addNewFixture(TileFixture fixture);
}
