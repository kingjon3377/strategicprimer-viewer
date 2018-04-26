import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    HasMutableImage,
    IFixture,
    HasKind,
	HasPopulation,
	HasImage
}
import strategicprimer.model.map.fixtures {
    UnitMember
}
import lovelace.util.jvm {
    readFileContents
}
"An interface to cover animals and animal tracks, to try to work around eclipse/ceylon#7372."
todo("Remove once that fixed and the fix released")
shared interface AnimalOrTracks of Animal|AnimalTracks satisfies IFixture&UnitMember {}
"Animal tracks or other traces."
shared class AnimalTracks(kind) satisfies HasMutableImage&HasKind&MobileFixture&AnimalOrTracks { // TODO: We'd prefer this to not be MobileFixture, but changing that would require serious refactoring of XML I/O code.
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
				report("Comparing tracks from different kinds of animals: ``fixture.kind`` and ``kind``");
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
"An animal or group of animals."
shared interface Animal
		satisfies AnimalOrTracks&Identifiable&MobileFixture&HasImage&HasKind&UnitMember&HasPopulation<Animal> {
	"Whether this is a talking animal."
	shared formal Boolean talking;
	"The domestication status of the animal."
	todo("Should this be an enumerated type?")
	shared formal String status;
	"The turn the animal was born, or -1 if it is an adult (or if this is traces ...)"
	shared formal Integer born;
	shared actual default String shortDescription {
		if (talking) {
			return "talking ``kind``";
		} else {
			String popString;
			if (population == 1) {
				popString = "";
			} else {
				popString = "``population`` ";
			}
			if (born >= 0) {
				return "``popString````kind`` (born ``born``)";
			} else {
				return "``popString````status`` ``kind``";
			}
		}
	}
	"Default image filename"
	todo("Should depend on the kind of animal")
	shared actual default String defaultImage => "animal.png";
	shared actual Integer hash => id;
	shared actual String string => shortDescription;
	shared actual String plural => "Animals";
	"Whether another animal is equal except its ID and population count."
	shared default Boolean equalExceptPopulation(Animal other) {
		return kind == other.kind && talking == other.talking &&
				status == other.status && born == other.born;
	}
	shared actual default Boolean equalsIgnoringID(IFixture fixture) {
		if (is Animal fixture) {
			return equalExceptPopulation(fixture) && population == fixture.population;
		} else {
			return false;
		}
	}
	"An object is equal if it is an animal with equal kind, either both or neither are
	 talking, either both or neither are only traces, and if not traces if the IDs are
	 equal."
	shared actual default Boolean equals(Object obj) {
		if (is Animal obj) {
			return obj.id == id && equalsIgnoringID(obj);
		} else {
			return false;
		}
	}
	shared actual formal Animal reduced(Integer newPopulation, Integer newId);
	shared actual formal Animal copy(Boolean zero);
	shared actual formal Animal combined(Animal addend);
	"Required Perception check result to find the animal."
	todo("Should be variable, either read from XML or computed from kind using some other
	      read-from-file data.") // FIXME
	shared actual default Integer dc => 22;
	shared actual default Boolean isSubset(IFixture obj, Anything(String) report) {
		if (obj.id == id) {
			if (is Animal obj) {
				if (kind != obj.kind) {
					report("Different kinds of animal for ID #``id``");
					return false;
				} else if (!talking, obj.talking) {
					report("In animal ID #``id``:\tSubmap's is talking and master's isn't");
					return false;
				} else if (status != obj.status) {
					report("Animal domestication status differs at ID #``id``");
					return false;
				} else if (obj.population > population) {
					report(
						"In animal #``id``: Submap has greater population than master");
					return false;
				} else if (obj.born < born) {
					report("In animal #``id``: Submap has greater age than master");
					return false;
				} else {
					return true;
				}
			} else if (is AnimalTracks obj, kind == obj.kind) {
				return true;
			} else {
				report("For ID #``id``, different kinds of members");
				return false;
			}
		} else {
			report("Called with different IDs, #``id`` and #``obj.id``");
			return false;
		}
	}
}
shared class AnimalImpl(kind, talking, status, id, born = -1, population = 1)
        satisfies Animal&HasMutableImage {
    "ID number."
    shared actual Integer id;
    "Whether this is a talking animal."
    shared actual Boolean talking;
    "The domestication status of the animal."
    shared actual variable String status;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "What kind of animal this is"
    shared actual String kind;
    "The turn the animal was born, or -1 if it is an adult (or if this is traces ...)"
    shared actual variable Integer born;
    "How many individual animals are in the population this represents."
    shared actual Integer population;
    "Clone the animal."
    shared actual Animal copy(Boolean zero) {
        AnimalImpl retval = AnimalImpl(kind, talking, status, id,
            (zero) then -1 else born, (zero) then 1 else population); // TODO: change, here and elsewhere, so that "unknown" is -1 population
        retval.image = image;
        return retval;
    }
    shared actual Animal reduced(Integer newPopulation, Integer newId) =>
            AnimalImpl(kind, talking, status, newId, born, newPopulation);
    shared actual Animal combined(Animal addend) => AnimalImpl(kind, talking, status, id, born,
            Integer.largest(0, population) + Integer.largest(0, addend.population));
}
shared object maturityModel {
    assert (exists textContent = readFileContents(`module strategicprimer.model`,
        "maturity.txt"));
    shared Map<String, Integer> maturityAges = map {
        *textContent.split('\n'.equals)
            .map((String line) => line.split('\t'.equals, true, true, 1))
            .map(({String+} line) => line.first->Integer.parse(line.rest.first else ""))
            .narrow<String->Integer>()
    };
    variable Integer currentTurnLocal = -1;
    shared Integer currentTurn => currentTurnLocal;
    assign currentTurn {
        if (currentTurnLocal < 0) {
            currentTurnLocal = currentTurn;
        }
    }
    "Clear the stored current turn"
    restricted shared void resetCurrentTurn() => currentTurnLocal = -1;
}
shared object animalPlurals satisfies Correspondence<String, String> {
    assert (exists textContent = readFileContents(`module strategicprimer.model`,
        "animal_plurals.txt"));
    Map<String, String> plurals = map(textContent.split('\n'.equals)
        .map((String line) => line.split('\t'.equals, true, true, 1))
        .map(({String+} line) => line.first->(line.rest.first else line.first)));
    shared actual String get(String key) => plurals[key] else key;
    shared actual Boolean defines(String key) => plurals.defines(key);

}
