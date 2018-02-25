import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    IFixture
}
"A [[strategicprimer.model.map::TileFixture]] to represent shrubs, or their aquatic
 equivalents, on a tile."
shared class Shrub(kind, id, population = -1) satisfies HarvestableFixture {
    "What kind of shrub this is"
    shared actual String kind;
    "The ID number."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "How many individual plants are in this planting of this shrub, or on this tile."
    shared Integer population; // FIXME: Make Subsettable so we can compare this properly
    shared actual Shrub copy(Boolean zero) {
        Shrub retval = Shrub(kind, id, (zero) then -1 else population);
        retval.image = image;
        return retval;
    }
    shared actual String defaultImage = "shrub.png";
    shared actual String string => kind;
    shared actual Boolean equals(Object obj) {
        if (is Shrub obj) {
            return obj.id == id && obj.kind == kind && population == obj.population;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Shrub fixture) {
            return kind == fixture.kind && population == fixture.population;
        } else {
            return false;
        }
    }
    shared actual String plural = "Shrubs";
    shared actual String shortDescription {
        if (population < 1) {
            return kind;
        } else {
            return "``population`` ``kind``";
        }
    }
    "The required Perception check for an explorer to find the fixture."
    todo("Should this vary, either loading from XML or by kind?")
    shared actual Integer dc = 15;
}
