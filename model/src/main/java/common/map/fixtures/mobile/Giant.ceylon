import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    IFixture,
    HasMutableImage,
    HasKind
}

"A giant."
shared class Giant(kind, id) satisfies Immortal&HasMutableImage&HasKind {
    "The ID number."
    shared actual Integer id;

    "The kind of giant."
    shared actual String kind;

    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";

    shared actual Giant copy(Boolean zero) {
        Giant retval = Giant(kind, id);
        retval.image = image;
        return retval;
    }

    shared actual String shortDescription =>
            (kind.empty) then "giant" else "``kind`` giant";

    shared actual String string => shortDescription;

    shared actual String defaultImage => "giant.png";

    shared actual Boolean equals(Object obj) {
        if (is Giant obj) {
            return obj.id == id && obj.kind == kind;
        } else {
            return false;
        }
    }

    shared actual Integer hash => id;

    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Giant fixture) {
            return kind == fixture.kind;
        } else {
            return false;
        }
    }

    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is Giant obj) {
                if (kind == obj.kind) {
                    return true;
                } else {
                    report("Different kinds of giant for ID #``id``");
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

    shared actual String plural = "Giants";

    "The required Perception check result to find the giant."
    todo("Should this vary with kind?")
    shared actual Integer dc => 28;
}
