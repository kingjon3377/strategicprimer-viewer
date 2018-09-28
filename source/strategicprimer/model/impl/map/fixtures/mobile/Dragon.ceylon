import strategicprimer.model.impl.map {
    HasMutableImage,
    IFixture,
    HasKind
}
"A dragon."
shared class Dragon(kind, id) satisfies Immortal&HasMutableImage&HasKind {
    "What kind of dragon."
    shared actual String kind;
    "ID number."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    shared actual Dragon copy(Boolean zero) {
        Dragon retval = Dragon(kind, id);
        retval.image = image;
        return retval;
    }
    shared actual String shortDescription =>
            (kind.empty) then "dragon" else "``kind`` dragon";
    shared actual String string => shortDescription;
    by("https://openclipart.org/detail/166560/fire-dragon-by-olku")
    shared actual String defaultImage = "dragon.png";
    shared actual Boolean equals(Object obj) {
        if (is Dragon obj) {
            return obj.kind == kind && obj.id == id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Dragon fixture) {
            return fixture.kind == kind;
        } else {
            return false;
        }
    }
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is Dragon obj) {
                if (obj.kind == kind) {
                    return true;
                } else {
                    report("\tDifferent kinds of dragon for ID #``id``");
                    return false;
                }
            } else {
                report("\tFor ID #``id``, different kinds of members");
                return false;
            }
        } else {
            report("\tCalled with different IDs, #``id`` and #``obj.id``");
            return false;
        }
    }
    shared actual String plural = "Dragons";
    shared actual Integer dc => 20;
}
