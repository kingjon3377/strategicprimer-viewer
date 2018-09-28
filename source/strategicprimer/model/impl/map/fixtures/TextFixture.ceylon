import strategicprimer.model.impl.map {
    TileFixture,
    HasMutableImage,
    IFixture
}
"A Fixture to encapsulate arbitrary text associated with a tile, so we can improve the
 interface, have more than one set of text per tile, and be clear on *which turn*
 encounters happened."
shared class TextFixture(text, turn) satisfies TileFixture&HasMutableImage {
    "The text."
    shared String text;
    "The turn it's associated with."
    shared Integer turn;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "Clone the object."
    shared actual TextFixture copy(Boolean zero) {
        TextFixture retval = TextFixture(text, turn);
        retval.image = image;
        return retval;
    }
    shared actual String shortDescription =>
            (turn == -1) then text else "``text`` (turn ``turn``)";
    shared actual String string => shortDescription;
    shared actual String defaultImage = "text.png";
    shared actual Boolean equals(Object obj) {
        if (is TextFixture obj) {
            return obj.text == text && obj.turn == turn;
        } else {
            return false;
        }
    }
    shared actual Integer hash = text.hash.leftLogicalShift(turn);
    "TextFixtures deliberately don't have a UID, unlike fixtures that used to not have
     them because there were *so many* in the world map."
    shared actual Integer id = -1;
    "Since text fixtures don't have an ID, this can simplyd elegate to equals()"
    shared actual Boolean equalsIgnoringID(IFixture fixture) => equals(fixture);
    shared actual String plural = "Arbitrary-text notes";
    "The required Perception check result for an explorer to find the note."
    shared actual Integer dc = 5;
}
