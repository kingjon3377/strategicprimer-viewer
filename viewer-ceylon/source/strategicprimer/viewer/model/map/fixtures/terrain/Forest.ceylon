import lovelace.util.common {
    todo
}

import model.map {
    TerrainFixture,
    HasMutableImage,
    HasKind,
    IFixture
}
"A forest on a tile."
shared class Forest(kind, rows, id) satisfies TerrainFixture&HasMutableImage&HasKind {
    "What kind of trees dominate this forest."
    shared actual String kind;
    """Whether this is "rows of" trees."""
    shared Boolean rows;
    "Unique identifying number for this instance."
    shared actual variable Integer id;
    "The filename of an image to use as an icon for this instance."
    variable String imageFilename = "";
    "The filename of an image to use as an icon for this instance."
    shared actual String image => imageFilename;
    shared actual void setImage(String image) => imageFilename = image;
    "Clone the forest"
    shared actual Forest copy(Boolean zero) {
        Forest retval = Forest(kind, rows, id);
        retval.setImage(image);
        return retval;
    }
    "The filename of an image to represent forests by default."
    todo("Should differ based on kind of tree.")
    shared actual String defaultImage = "trees.png";
    shared actual Boolean equals(Object obj) {
        if (is Forest obj) {
            return obj.id == id && kind == obj.kind && rows == obj.rows;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Forest fixture) {
            return fixture.kind == kind && fixture.rows == rows;
        } else {
            return false;
        }
    }
    shared actual String plural() => "Forests";
    shared actual String shortDesc() =>
            (rows) then "Rows of ``kind`` trees" else "A ``kind`` forest";
    shared actual String string => shortDesc();
    shared actual Integer dc = 5;
}