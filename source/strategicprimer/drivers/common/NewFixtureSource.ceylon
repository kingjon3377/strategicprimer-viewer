"An interface for [[NewFixtureSource]]s to listen to."
shared interface NewFixtureSource {
    "Add a listener."
    shared formal void addNewFixtureListener(NewFixtureListener listener);

    "Remove a listener."
    shared formal void removeNewFixtureListener(NewFixtureListener listener);
}
