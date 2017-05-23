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
"An animal or group of animals."
todo("Add more features (population, to start with)")
shared class Animal(kind, traces, talking, status, id, born = -1)
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
	shared actual String shortDescription =>
			"``(traces) then "traces of " else ""````(talking) then "talking " else ""````kind``";
	"Default image filename"
	todo("Should depend on the kind of animal")
	shared actual String defaultImage = (traces) then "tracks.png" else "animal.png";
	shared actual Boolean equalsIgnoringID(IFixture fixture) {
		if (is Animal fixture) {
			return kind == fixture.kind && traces == fixture.traces &&
				talking == fixture.talking && status == fixture.status;
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
					&& status == obj.status && id == obj.id && born == obj.born;
			}
		} else {
			return false;
		}
	}
	shared actual Integer hash => id;
	shared actual String string => shortDescription;
	shared actual String plural = "Animals";
	"Clone the animal."
	todo("Should we zero out any information?")
	shared actual Animal copy(Boolean zero) {
		Animal retval = Animal(kind, traces, talking, status, id);
		retval.image = image;
		return retval;
	}
	"Required Perception check result to find the animal."
	todo("Should be variable, either read from XML or computed from kind using some other
	      read-from-file data.") // FIXME
	shared actual Integer dc => (traces) then 12 else 22;
	todo("Check turn of birth?")
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
	assert (exists file = `module strategicprimer.model`
		.resourceByPath("maturity.txt"));
	value textContent = file.textContent();
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