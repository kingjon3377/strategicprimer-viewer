import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    IFixture
}
import strategicprimer.model.map.fixtures {
    IEvent,
    MineralFixture
}
"A deposit (always exposed for now) of stone."
todo("Support non-exposed deposits")
shared class StoneDeposit(stone, dc, id)
        satisfies IEvent&HarvestableFixture&MineralFixture {
    "What kind of stone this deposit is."
    shared StoneKind stone;
    "The DC to discover the deposit."
    todo("Reasonable defaults")
    shared actual Integer dc;
    "ID number."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "Clone the object."
    shared actual StoneDeposit copy(Boolean zero) {
        StoneDeposit retval = StoneDeposit(stone, (zero) then 0 else dc, id);
        retval.image = image;
        return retval;
    }
    shared actual String text => "There is an exposed ``stone`` deposit here.";
    shared actual Boolean equals(Object obj) {
        if (is StoneDeposit obj) {
            return obj.stone == stone && obj.id == id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual String string => "A ``stone`` deposit, of DC ``dc``";
    "The default icon filename."
    shared actual String defaultImage = "stone.png";
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is StoneDeposit fixture) {
            return fixture.stone == stone;
        } else {
            return false;
        }
    }
    shared actual String kind => stone.string;
    shared actual String plural = "Stone deposits";
    shared actual String shortDescription => "an exposed ``stone`` deposit";
}
