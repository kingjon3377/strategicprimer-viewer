import strategicprimer.model.impl.map {
    HasMutableImage,
    IFixture,
    HasKind
}
"A centaur."
shared class Centaur(kind, id) satisfies Immortal&HasMutableImage&HasKind {
    "What kind of centaur."
    shared actual String kind;
    "ID number."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    shared actual Centaur copy(Boolean zero) {
        Centaur retval = Centaur(kind, id);
        retval.image = image;
        return retval;
    }
    shared actual String shortDescription => "``kind`` centaur";
    shared actual String string => shortDescription;
    shared actual String defaultImage = "centaur.png";
    shared actual Boolean equals(Object obj) {
        if (is Centaur obj) {
            return obj.kind == kind && obj.id == id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Centaur fixture) {
            return fixture.kind == kind;
        } else {
            return false;
        }
    }
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is Centaur obj) {
                if (obj.kind == kind) {
                    return true;
                } else {
                    report("Different kinds of centaur for ID #``id``");
                    return false;
                }
            } else {
                report("For ID #``id``, different kinds of members");
                return false;
            }
        } else {
            report("Called with different IDs, #``id`` and ``obj.id``");
            return false;
        }
    }
    shared actual String plural = "Centaurs";
    shared actual Integer dc => 20;
}
