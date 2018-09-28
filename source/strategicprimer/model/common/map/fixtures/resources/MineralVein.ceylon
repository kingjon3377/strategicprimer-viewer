import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    IFixture
}
import strategicprimer.model.common.map.fixtures {
    IEvent,
    MineralFixture
}
"A vein of a mineral."
shared class MineralVein(kind, exposed, dc, id)
        satisfies IEvent&HarvestableFixture&MineralFixture {
    "What kind of mineral this is a vein of"
    shared actual String kind;
    "Whether the vein is exposed or not."
    shared variable Boolean exposed;
    "The DC to discover the vein."
    todo("Provide good default")
    shared actual Integer dc;
    "The ID number."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    shared actual MineralVein copy(Boolean zero) {
        MineralVein retval = MineralVein(kind, exposed, (zero) then 0 else dc, id);
        retval.image = image;
        return retval;
    }
    shared actual String text =>
            (exposed) then "There is an exposed vein of ``kind`` here."
                else "There is a vein of ``kind`` here, but it's not exposed.";
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is MineralVein fixture) {
            return kind == fixture.kind && exposed == fixture.exposed;
        } else {
            return false;
        }
    }
    shared actual Boolean equals(Object obj) {
        if (is MineralVein obj, equalsIgnoringID(obj)) {
            return obj.id == id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual String string =>
            (exposed) then "A ``kind`` deposit, exposed, DC ``dc``"
                else "A ``kind`` deposit, not exposed, DC ``dc``";
    shared actual String defaultImage = "mineral.png";
    shared actual String plural = "Mineral veins";
    shared actual String shortDescription =>
            (exposed) then "exposed ``kind``" else "unexposed ``kind``";
}
