import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    HasMutableImage,
    IFixture,
    HasKind
}
import strategicprimer.model.map.fixtures {
    UnitMember,
    FortressMember
}
"A piece of equipment."
todo("More members?")
shared class Implement(kind, id, count = 1)
        satisfies UnitMember&FortressMember&HasKind&HasMutableImage {
    """The "kind" of the implement."""
    shared actual String kind;
    "The ID number."
    shared actual Integer id;
    "How many of this kind of equipment are in this stack."
    shared Integer count;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "If we ignore ID, a fixture is equal iff itis an Implement of the same kind."
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Implement fixture) {
            return fixture.kind == kind && fixture.count == count;
        } else {
            return false;
        }
    }
    "A fixture is a subset iff it is equal."
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is Implement obj) {
                if (obj.kind == kind) {
                    if (obj.count <= count) {
                        return true;
                    } else {
                        report("In Implement ID #``id``:\tHas higher count than we do");
                        return false;
                    }
                } else {
                    report("In Implement ID #``id``:\tKinds differ");
                    return false;
                }
            } else {
                report("Different fixture types given for ID #``id``");
                return false;
            }
        } else {
            report("IDs differ");
            return false;
        }
    }
    shared actual Implement copy(Boolean zero) => Implement(kind, id, count);
    shared actual String defaultImage = "implement.png";
    shared actual Boolean equals(Object obj) {
        if (is Implement obj) {
            return obj.id == id && obj.kind == kind && obj.count == count;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual String string {
        if (count == 1) {
            return "An implement of kind ``kind``";
        } else {
            return "A group of ``count`` implements of kind ``kind``";
        }
    }
}
