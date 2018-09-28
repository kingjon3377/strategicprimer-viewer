import strategicprimer.model.impl.map {
    HasMutableImage,
    IFixture
}
"A TileFixture to represent the basic rock beneath the tile, possibly exposed."
shared class Ground(id, kind, exposed) satisfies MineralFixture&HasMutableImage {
    "The kind of ground."
    shared actual String kind;
    "Whether the ground is exposed."
    shared variable Boolean exposed;
    "The ID number."
    shared actual variable Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "Clone the object."
    shared actual Ground copy(Boolean zero) {
        Ground retval = Ground(id, kind, exposed);
        retval.image = image;
        return retval;
    }
    "Default image depends on whether the ground is exposed or not."
    shared actual String defaultImage => (exposed) then "expground.png" else "blank.png";
    "An object is equal if it is Ground of the same kind, either both or neither are
     exposed, and it has the same ID."
    shared actual Boolean equals(Object obj) {
        if (is Ground obj) {
            return kind == obj.kind && exposed == obj.exposed && id == obj.id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual String shortDescription =>
            "``(exposed) then "Exposed" else "Unexposed"`` ground of kind ``kind``";
    shared actual String string => "``shortDescription``, ID #``id``";
    "If we ignore ID, a fixture is equal if if it is a Ground with equal kind and either
     both or neither are exposed."
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Ground fixture) {
            return kind == fixture.kind && exposed == fixture.exposed;
        } else {
            return false;
        }
    }
    "This works as the plural for our purposes, since it functions as a collective noun."
    shared actual String plural = "Ground";
    "The required Perception check result for an explorer to find the fixture. This does
     not cover digging to deliberately uncover it."
    shared actual Integer dc => (exposed) then 10 else 40;
}
