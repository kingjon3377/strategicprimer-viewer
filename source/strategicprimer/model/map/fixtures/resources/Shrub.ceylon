import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    IFixture,
	HasPopulation
}
"A [[strategicprimer.model.map::TileFixture]] to represent shrubs, or their aquatic
 equivalents, on a tile."
shared class Shrub(kind, id, population = -1) satisfies HarvestableFixture&HasPopulation/*<Shrub>*/ {
    "What kind of shrub this is"
    shared actual String kind;
    "The ID number."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "How many individual plants are in this planting of this shrub, or on this tile."
    shared actual Integer population;
    shared actual Shrub copy(Boolean zero) {
        Shrub retval = Shrub(kind, id, (zero) then -1 else population);
        retval.image = image;
        return retval;
    }
    shared actual Shrub reduced(Integer newPopulation) => Shrub(kind, id, newPopulation);
    shared actual Shrub combined(HasPopulation addend) {
        assert (is Shrub addend);
        return Shrub(kind, id, Integer.largest(0, population) + Integer.largest(0, addend.population));
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
    shared actual Boolean isSubset(IFixture other, Anything(String) report) {
        if (other.id != id) {
            report("Different IDs");
            return false;
        } else if (is Shrub other) {
            if (other.kind != kind) {
                report("In shrub with ID #``id``:\tKinds differ");
                return false;
            } else if (other.population > population) {
                report("In shrub ``kind`` (#``id``):\tHas higher count than we do");
                return false;
            } else {
                return true;
            }
        } else {
            report("Different types for ID #``id``");
            return false;
        }
    }
    "The required Perception check for an explorer to find the fixture."
    todo("Should this vary, either loading from XML or by kind?")
    shared actual Integer dc = 15;
}
