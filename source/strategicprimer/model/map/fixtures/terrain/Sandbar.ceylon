import strategicprimer.model.map.fixtures {
    TerrainFixture
}
import strategicprimer.model.map {
    HasMutableImage,
    IFixture
}
"A sandbar on the map."
shared class Sandbar(id) satisfies TerrainFixture&HasMutableImage {
    "The ID number of this fixture."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    shared actual String string = "Sandbar";
    shared actual String defaultImage = "sandbar.png";
    shared actual Boolean equals(Object obj) {
        if (is Sandbar obj) {
            return obj.id == id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) => fixture is Sandbar;
    shared actual String plural = "Sandbars";
    shared actual String shortDescription => "a sandbar";
    shared actual Sandbar copy(Boolean zero) {
        Sandbar retval = Sandbar(id);
        retval.image = image;
        return retval;
    }
    shared actual Integer dc = 18;
}
