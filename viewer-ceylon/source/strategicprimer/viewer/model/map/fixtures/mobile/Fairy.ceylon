import java.util {
    Formatter
}

import lovelace.util.common {
    todo
}

import strategicprimer.viewer.model.map {
	HasMutableImage
}
import model.map {
    IFixture,
    HasKind
}
"A fairy."
shared class Fairy(kind, id) satisfies Immortal&HasMutableImage&HasKind {
	"The ID number."
	shared actual Integer id;
	"The kind of fairy."
	shared actual String kind;
	"The filename of an image to use as an icon for this instance."
	shared actual variable String image = "";
	shared actual Fairy copy(Boolean zero) {
		Fairy retval = Fairy(kind, id);
		retval.image = image;
		return retval;
	}
	shared actual String shortDescription => "``kind`` fairy";
	shared actual String string => shortDescription;
	shared actual String defaultImage => "giant.png";
	shared actual Boolean equals(Object obj) {
		if (is Giant obj) {
			return obj.id == id && obj.kind == kind;
		} else {
			return false;
		}
	}
	shared actual Integer hash => id;
	shared actual Boolean equalsIgnoringID(IFixture fixture) {
		if (is Giant fixture) {
			return kind == fixture.kind;
		} else {
			return false;
		}
	}
	shared actual Boolean isSubset(IFixture obj, Formatter ostream, String context) {
		if (obj.id == id) {
			if (is Giant obj) {
				if (kind == obj.kind) {
					return true;
				} else {
					ostream.format("%s\tDiffernt kinds of giant for ID #%d%n", context,
						id);
					return false;
				}
			} else {
				ostream.format("%s\tFor ID #%d, different kinds of members%n", context,
					id);
				return false;
			}
		} else {
			ostream.format("%s\tCalled with different IDs, #%d and #%d%n", context, id,
				obj.id);
			return false;
		}
	}
	shared actual String plural = "Giants";
	"The required Perception check result to find the giant."
	todo("Should vary, either defined in XML or computed from kind")
	shared actual Integer dc => 28;
}