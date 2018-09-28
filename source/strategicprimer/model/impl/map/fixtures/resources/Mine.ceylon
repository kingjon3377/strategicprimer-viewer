import lovelace.util.common {
    todo
}

import strategicprimer.model.impl.map {
    IFixture
}
import strategicprimer.model.impl.map.fixtures {
    MineralFixture
}
import strategicprimer.model.impl.map.fixtures.towns {
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
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "The default icon filename."
    shared actual String defaultImage = "mine.png";
    shared actual String plural = "Mines";
    "Clone the object."
    shared actual Mine copy(Boolean zero) {
        Mine retval = Mine(kind, status, id);
        retval.image = image;
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
    shared actual String shortDescription => "``status`` ``kind`` mine";
    "The required Perception check for an explorer to find this fixture."
    todo("Should perhaps be variable and loaded from XML")
    shared actual Integer dc => (TownStatus.active == status) then 15 else 25;
}
