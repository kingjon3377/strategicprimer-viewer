import lovelace.util.common {
    todo
}
import strategicprimer.model.map.fixtures {
    TerrainFixture,
	SPNumber,
	numberComparator
}
import strategicprimer.model.map {
    HasMutableImage,
    IFixture,
    HasKind
}
"A forest on a tile."
shared class Forest(kind, rows, id, acres = -1) satisfies TerrainFixture&HasMutableImage&HasKind {
    "What kind of trees dominate this forest."
    shared actual String kind;
    """Whether this is "rows of" trees."""
    shared Boolean rows;
    "Unique identifying number for this instance."
    shared actual variable Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "The size of the forest, in acres. (Or a negative number if unknown.)"
    shared SPNumber acres; // FIXME: Make Subsettable so we can compare this properly
    "Clone the forest"
    shared actual Forest copy(Boolean zero) {
        Forest retval = Forest(kind, rows, id, (zero) then -1 else acres);
        retval.image = image;
        return retval;
    }
    "The filename of an image to represent forests by default."
    todo("Should differ based on kind of tree.")
    shared actual String defaultImage = "trees.png";
    shared actual Boolean equals(Object obj) {
        if (is Forest obj) {
            return obj.id == id && kind == obj.kind && rows == obj.rows && acres == obj.acres;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Forest fixture) {
            return fixture.kind == kind && fixture.rows == rows && fixture.acres == acres;
        } else {
            return false;
        }
    }
    shared actual String plural = "Forests";
    shared actual String shortDescription {
        if (numberComparator.compare(acres, 0) == smaller) {
            if (rows) {
                return "Rows of ``kind`` trees";
            } else {
                return "A ``kind`` forest";
            }
        } else {
            if (rows) {
                return "Rows of ``kind`` trees for ``acres`` acres";
            } else {
                return "A ``acres``-acre ``kind`` forest";
            }
        }
    }
    shared actual String string => shortDescription;
    shared actual Integer dc = 5;
}
