import java.util {
    Formatter
}

import strategicprimer.viewer.model.map {
	HasMutableImage
}
import model.map {
    HasKind,
    IFixture
}
"A centaur."
shared class Centaur(kind, id) satisfies Immortal&HasMutableImage&HasKind {
	"What kind of centaur."
	shared actual String kind;
	"ID number."
	shared actual Integer id;
	"The filename of an image to use as an icon for this instance."
	shared actual variable String image = "";
	shared actual Centaur copy(Boolean zero) {
		Centaur retval = Centaur(kind, id);
		retval.image = image;
		return retval;
	}
	shared actual String shortDescription => "``kind`` centaur";
	shared actual String string => shortDescription;
	shared actual String defaultImage = "centaur.png";
	shared actual Boolean equals(Object obj) {
		if (is Centaur obj) {
			return obj.kind == kind && obj.id == id;
		} else {
			return false;
		}
	}
	shared actual Integer hash => id;
	shared actual Boolean equalsIgnoringID(IFixture fixture) {
		if (is Centaur fixture) {
			return fixture.kind == kind;
		} else {
			return false;
		}
	}
	shared actual Boolean isSubset(IFixture obj, Formatter ostream, String context) {
		if (obj.id == id) {
			if (is Centaur obj) {
				if (obj.kind == kind) {
					return true;
				} else {
					ostream.format("%s\tDifferent kinds of centaur for ID #%d%n",
						context, id);
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
	shared actual String plural = "Centaurs";
	shared actual Integer dc => 20;
}