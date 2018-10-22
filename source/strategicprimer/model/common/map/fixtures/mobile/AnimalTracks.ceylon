import lovelace.util.common {
    todo
}
import strategicprimer.model.common.map.fixtures.mobile {
    MobileFixture
}
import strategicprimer.model.common.map {
    IFixture,
    HasMutableImage,
    HasKind
}

"Animal tracks or other traces."
todo("We'd prefer this to not be MobileFixture, but changing that would require
      serious refactoring of XML I/O code.")
shared class AnimalTracks(kind)
        satisfies HasMutableImage&HasKind&MobileFixture&AnimalOrTracks {
    "The kind of animal of which this is tracks or traces."
    shared actual String kind;

    shared actual String shortDescription = "traces of ``kind``";

    todo("Should perhaps depend on the kind of animal")
    shared actual String defaultImage = "tracks.png";

    shared actual Integer hash => kind.hash;

    shared actual String string => shortDescription;

    shared actual String plural => "Animal tracks";

    shared actual Boolean equals(Object other) {
        if (is AnimalTracks other) {
            return other.kind == kind;
        } else {
            return false;
        }
    }

    shared actual Boolean equalsIgnoringID(IFixture other) => equals(other);

    todo("Allow user to customize via XML?")
    shared actual Integer dc = 12;

    shared actual Integer id = -1;

    shared actual AnimalTracks copy(Boolean zero) => AnimalTracks(kind);

    shared actual variable String image = "";

    shared actual Boolean isSubset(IFixture fixture, Anything(String) report) {
        if (is AnimalTracks fixture) {
            if (fixture.kind == kind) {
                return true;
            } else {
                report("Comparing tracks from different kinds of animals: ``
                fixture.kind`` and ``kind``"); // TODO: Fix indentation
                return false;
            }
        } else if (is Animal fixture, fixture.kind == kind) {
            report("Has full ``kind`` animal where we have only tracks");
            return false;
        } else {
            report("Different kind of fixture");
            return false;
        }
    }
}
