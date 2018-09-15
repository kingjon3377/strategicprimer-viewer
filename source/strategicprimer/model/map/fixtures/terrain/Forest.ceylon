import lovelace.util.common {
    todo
}
import strategicprimer.model.map.fixtures {
    TerrainFixture,
	numberComparator
}
import strategicprimer.model.map {
    HasMutableImage,
    IFixture,
    HasKind,
	HasExtent
}
"A forest on a tile."
shared class Forest(kind, rows, id, acres = -1)
		satisfies TerrainFixture&HasMutableImage&HasKind&HasExtent {
    "What kind of trees dominate this forest."
    shared actual String kind;
    """Whether this is "rows of" trees."""
    shared Boolean rows;
    "Unique identifying number for this instance."
    shared actual variable Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "The size of the forest, in acres. (Or a negative number if unknown.)"
    shared actual Number<out Anything> acres;
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
            return obj.id == id && kind == obj.kind && rows == obj.rows &&
//                acres == obj.acres; // TODO: switch back once fix for eclipse/ceylon-sdk#711 released
                numberComparator.compare(acres, obj.acres) == equal;
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
        if (!acres.positive) {
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
    shared actual Boolean isSubset(IFixture other, Anything(String) report) {
        if (id != other.id) {
            report("Different IDs");
            return false;
        } else if (is Forest other) {
            if (other.kind != kind) {
                report("In forest with ID #``id``: Kinds differ");
                return false;
            }
            variable Boolean retval = true;
            void localReport(String str) =>
                    report("In ``kind`` forest (ID #``id``):\t``str``");
            if (other.rows, !rows) {
                localReport("In rows when we aren't");
                retval = false;
            }
            if (numberComparator.compare(other.acres, acres) == larger) {
                localReport("Has larger extent than we do");
                retval = false;
            }
            return retval;
        } else {
            report("Different types for ID #``id``");
            return false;
        }
    }
    shared actual Integer dc = 5;
}
