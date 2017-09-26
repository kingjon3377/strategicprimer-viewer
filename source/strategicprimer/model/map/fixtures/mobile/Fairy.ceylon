import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    HasMutableImage,
    IFixture,
    HasKind
}
"A fairy."
shared class Fairy(kind, id) satisfies Immortal&HasMutableImage&HasKind {
    "The ID number."
    shared actual Integer id;
    "The kind of fairy."
    shared actual String kind;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    shared actual Fairy copy(Boolean zero) {
        Fairy retval = Fairy(kind, id);
        retval.image = image;
        return retval;
    }
    shared actual String shortDescription => "``kind`` fairy";
    shared actual String string => shortDescription;
    shared actual String defaultImage => "fairy.png";
    shared actual Boolean equals(Object obj) {
        if (is Fairy obj) {
            return obj.id == id && obj.kind == kind;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Fairy fixture) {
            return kind == fixture.kind;
        } else {
            return false;
        }
    }
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is Fairy obj) {
                if (kind == obj.kind) {
                    return true;
                } else {
                    report("Different kinds of fairy for ID #``id``");
                    return false;
                }
            } else {
                report("For ID #``id``, different kinds of members");
                return false;
            }
        } else {
            report("Called with different IDs, #``id`` and #``obj.id``");
            return false;
        }
    }
    shared actual String plural = "Fairies";
    "The required Perception check result to find the fairy."
    todo("Should vary, either defined in XML or computed from kind")
    shared actual Integer dc => 28;
}
