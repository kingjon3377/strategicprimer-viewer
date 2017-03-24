import lovelace.util.common {
    todo
}

import model.map {
    IFixture
}
import model.map.fixtures {
    MineralFixture
}
import model.map.fixtures.resources {
    HarvestableFixture
}
import strategicprimer.viewer.model.map.fixtures.towns {
    TownStatus
}
"A mine---a source of mineral resources."
shared class Mine(kind, status, id) satisfies HarvestableFixture&MineralFixture {
    "What the mine produces."
    shared actual String kind;
    "The status of the mine."
    shared TownStatus status;
    "The ID number of the fixture."
    shared actual Integer id;
    "The name of an image to use as an icon for this particular instance."
    variable String imageFilename = "";
    "The default icon filename."
    shared actual String defaultImage = "mine.png";
    "The name of an image to use as an icon for this particular instance."
    shared actual String image => imageFilename;
    "Set the per-instance icon filename."
    shared actual void setImage(String image) => imageFilename = image;
    shared actual String plural() => "Mines";
    "Clone the object."
    shared actual Mine copy(Boolean zero) {
        Mine retval = Mine(kind, status, id);
        retval.setImage(image);
        return retval;
    }
    shared actual String string => "``status`` mine of ``kind``";
    shared actual Boolean equals(Object obj) {
        if (is Mine obj) {
            return kind == obj.kind && status == obj.status && id == obj.id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Mine fixture) {
            return kind == fixture.kind && status == fixture.status;
        } else {
            return false;
        }
    }
    shared actual String shortDesc() => "``status`` ``kind`` mine";
    "The required Perception check for an explorer to find this fixture."
    todo("Should perhaps be variable and loaded from XML")
    shared actual Integer dc => (TownStatus.active == status) then 15 else 25;
}