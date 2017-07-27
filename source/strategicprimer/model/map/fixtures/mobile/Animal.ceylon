import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
	HasMutableImage,
	IFixture,
    HasKind
}
import strategicprimer.model.map.fixtures {
    UnitMember
}
import lovelace.util.jvm {
    readFileContents
}
"An animal or group of animals."
shared class Animal(kind, traces, talking, status, id, born = -1, population = 1)
		satisfies MobileFixture&HasMutableImage&HasKind&UnitMember {
	"ID number."
	shared actual Integer id;
	"If true, this is only traces or tracks, not an actual animal."
	shared Boolean traces;
	"Whether this is a talking animal."
	shared Boolean talking;
	"The domestication status of the animal."
	todo("Should this be an enumerated type?")
	shared variable String status;
	"The filename of an image to use as an icon for this instance."
	shared actual variable String image = "";
	"What kind of animal this is"
	shared actual String kind;
	"The turn the animal was born, or -1 if it is an adult (or if this is traces ...)"
	shared variable Integer born;
	"How many individual animals are in the population this represents."
	shared Integer population;
	"A population cannot have fewer than one individual."
	assert (population >= 1);
	shared actual String shortDescription {
		if (traces) {
			return "traces of ``kind``";
		} else if (talking) {
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
	shared actual String defaultImage = (traces) then "tracks.png" else "animal.png";
	"Whether another animal is equal except its ID and population count."
	shared Boolean equalExceptPopulation(Animal other) {
		return kind == other.kind && traces == other.traces && talking == other.talking &&
		status == other.status && born == other.born;
	}
	shared actual Boolean equalsIgnoringID(IFixture fixture) {
		if (is Animal fixture) {
			return equalExceptPopulation(fixture) && population == fixture.population;
		} else {
			return false;
		}
	}
	"An object is equal if it is an animal with equal kind, either both or neither are
	 talking, either both or neither are only traces, and if not traces if the IDs are
	 equal."
	shared actual Boolean equals(Object obj) {
		if (is Animal obj) {
			if (traces) {
				return equalsIgnoringID(obj);
			} else {
				return kind == obj.kind && traces == obj.traces && talking == obj.talking
					&& status == obj.status && id == obj.id && born == obj.born
					&& population == obj.population;
			}
		} else {
			return false;
		}
	}
	shared actual Integer hash => id;
	shared actual String string => shortDescription;
	shared actual String plural = "Animals";
	"Clone the animal."
	shared actual Animal copy(Boolean zero) {
		Animal retval = Animal(kind, traces, talking, status, id,
			(zero) then -1 else born, (zero) then 1 else population);
		retval.image = image;
		return retval;
	}
	"Required Perception check result to find the animal."
	todo("Should be variable, either read from XML or computed from kind using some other
	      read-from-file data.") // FIXME
	shared actual Integer dc => (traces) then 12 else 22;
	shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
		if (obj.id == id) {
			if (is Animal obj) {
				if (kind != obj.kind) {
					report("Different kinds of animal for ID #``id``");
					return false;
				} else if (!talking, obj.talking) {
					report("In animal ID #``id``:\tSubmap's is talking and master's isn't");
					return false;
				} else if (traces, !obj.traces) {
					report("In animal #``id``: Submap has animal and master only tracks");
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
	todo("If Ceylon ever gets a sufficiently nuanced visibility system, restrict access to
	      this method to the package containing the XML I/O tests")
	shared void resetCurrentTurn() => currentTurnLocal = -1;
}
shared object animalPlurals satisfies Correspondence<String, String> {
	assert (exists textContent = readFileContents(`module strategicprimer.model`,
		"animal_plurals.txt"));
	Map<String, String> plurals = map { *textContent.split('\n'.equals)
		.map((String line) => line.split('\t'.equals, true, true, 1))
		.map(({String+} line) => line.first->(line.rest.first else line.first))
	};
	shared actual String get(String key) => plurals[key] else key;
	shared actual Boolean defines(String key) => plurals.defines(key);

}
