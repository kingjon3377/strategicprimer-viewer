"An interface for \"fixtures\" used in the UI that should not actually be added to a map."
shared interface FakeFixture satisfies TileFixture&HasImage {
    """A dummy "ID number""""
    deprecated("A fake fixture should only ever be used in a FixtureListModel, so this
                should never be called.")
    shared actual default Integer id {
        log.warn("A fake fixture was asked for its ID");
        return -1;
    }

    "Whether this equals another fixture if we ignore ID."
    deprecated("A fake fixture should only ever be used in a FixtureListModel, so this
                method should never be called.")
    shared actual default Boolean equalsIgnoringID(IFixture fixture) {
        log.warn("equalsIgnoringID() called on a fake fixture");
        return equals(fixture);
    }

    "We don't allow per-instance icons for these, so always return the empty string."
    shared actual String image => "";

    deprecated("A fake fixture should only ever be used in a FixtureListModel, so this
                method should never be called.")
    shared actual default String plural {
        log.warn("A fake fixture asked for its plural");
        return "You shouldn't see this text; report this.";
    }

    "Compare to another fixture."
    deprecated("A fake fixture should only ever be used in a FixtureListModel, so this
                method should never be called.")
    shared actual default Comparison compare(TileFixture fixture) {
        log.warn("compare() called on a fake fixture");
        return (super of TileFixture).compare(fixture);
    }
}
