import lovelace.util.common {
    todo
}

import model.map {
    HasKind,
    HasMutableImage,
    IFixture
}
import model.map.fixtures {
    UnitMember
}
import strategicprimer.viewer.model.map.fixtures {
    FortressMember
}
import java.util {
    Formatter
}
"A piece of equipment."
todo("More members?")
shared class Implement(kind, id)
		satisfies UnitMember&FortressMember&HasKind&HasMutableImage {
	"""The "kind" of the implement."""
	shared actual String kind;
	"The ID number."
	shared actual Integer id;
	"The filename of an image to use as an icon for this instance."
	variable String imageFilename = "";
	shared actual String image => imageFilename;
	shared actual void setImage(String image) => imageFilename = image;
	"If we ignore ID, a fixture is equal iff itis an Implement of the same kind."
	shared actual Boolean equalsIgnoringID(IFixture fixture) {
		if (is Implement fixture) {
			return fixture.kind == kind;
		} else {
			return false;
		}
	}
	"A fixture is a subset iff it is equal."
	shared actual Boolean isSubset(IFixture obj, Formatter ostream, String context) {
		if (obj.id == id) {
			if (is Implement obj) {
				if (obj.kind == kind) {
					return true;
				} else {
					ostream.format("%s\tIn Implement ID #%d%n: Kinds differ%n", context,
						id);
					return false;
				}
			} else {
				ostream.format("%s\tDifferent fixture types given for ID #%d%n", context,
					id);
				return false;
			}
		} else {
			ostream.format("%s\tIDs differ%n", context);
			return false;
		}
	}
	shared actual Implement copy(Boolean zero) => Implement(kind, id);
	shared actual String defaultImage = "implement.png";
	shared actual Boolean equals(Object obj) {
		if (is Implement obj) {
			return obj.id == id && obj.kind == kind;
		} else {
			return false;
		}
	}
	shared actual Integer hash => id;
	shared actual String string => "An implement of kind ``kind``";
}