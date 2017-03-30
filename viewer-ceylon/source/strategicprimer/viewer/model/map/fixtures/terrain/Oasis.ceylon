import strategicprimer.viewer.model.map.fixtures {
    TerrainFixture
}
import model.map {
    HasMutableImage,
    IFixture
}
"An oasis on the map."
shared class Oasis(id) satisfies TerrainFixture&HasMutableImage {
    "The ID number of this fixture."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    variable String imageFilename = "";
    "The filename of an image to use as an icon for this instance."
    shared actual String image => imageFilename;
    "Set the per-instance icon filename."
    shared actual void setImage(String image) => imageFilename = image;
    shared actual String string = "Oasis";
    shared actual String defaultImage = "oasis.png";
    shared actual Boolean equals(Object obj) {
        if (is Oasis obj) {
            return obj.id == id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) => fixture is Oasis;
    shared actual String plural() => "Oases";
    shared actual String shortDesc() => "an oasis";
    shared actual Oasis copy(Boolean zero) {
        Oasis retval = Oasis(id);
        retval.setImage(image);
        return retval;
    }
    shared actual Integer dc = 15;
}