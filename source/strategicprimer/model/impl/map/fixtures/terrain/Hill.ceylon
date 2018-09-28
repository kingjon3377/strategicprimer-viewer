import lovelace.util.common {
    todo
}
import strategicprimer.model.impl.map.fixtures {
    TerrainFixture
}
import strategicprimer.model.impl.map {
    HasMutableImage,
    IFixture
}
"A hill on the map. Should increase unit's effective vision by a small fraction when the
 unit is on it, if not in forest."
todo("Implement that")
shared class Hill(id) satisfies TerrainFixture&HasMutableImage {
    "The ID number of this fixture."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    shared actual String string = "Hill";
    shared actual String defaultImage = "hill.png";
    shared actual Boolean equals(Object obj) {
        if (is Hill obj) {
            return obj.id == id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) => fixture is Hill;
    shared actual String plural = "Hills";
    shared actual String shortDescription => "a hill";
    shared actual Hill copy(Boolean zero) {
        Hill retval = Hill(id);
        retval.image = image;
        return retval;
    }
    shared actual Integer dc = 10;
}
