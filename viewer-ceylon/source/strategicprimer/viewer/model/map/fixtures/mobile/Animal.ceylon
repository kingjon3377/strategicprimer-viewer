import lovelace.util.common {
    todo
}
import strategicprimer.viewer.model.map {
	HasMutableImage,
	IFixture
}
import model.map {
    HasKind
}
import strategicprimer.viewer.model.map.fixtures {
    UnitMember
}
"An animal or group of animals."
todo("Add more features (population, to start with)")
shared class Animal(kind, traces, talking, status, id)
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
					&& status == obj.status && id == obj.id;
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