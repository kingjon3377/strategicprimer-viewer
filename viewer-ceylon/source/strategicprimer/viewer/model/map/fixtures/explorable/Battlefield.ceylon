import model.map {
    IEvent,
    IFixture
}
import model.map.fixtures.explorable {
    ExplorableFixture
}
""" "There are the signs of a long-ago battle here" """
shared class Battlefield(dc, id) satisfies IEvent&ExplorableFixture {
	"The required Perception check result to discover the battlefield."
	shared actual Integer dc;
	"A unique ID."
	shared actual Integer id;
	"The filename of an image to use as an icon for this instance."
	variable String imageFilename = "";
	shared actual String image => imageFilename;
	shared actual void setImage(String image) => imageFilename = image;
	"Clone the object."
	shared actual Battlefield copy(Boolean zero) {
		Battlefield retval = Battlefield((zero) then 0 else dc, id);
		retval.setImage(image);
		return retval;
	}
	shared actual String text = "There are the signs of a long-ago battle here.";
	shared actual Boolean equals(Object obj) {
		if (is Battlefield obj) {
			return obj.id == id;
		} else {
			return false;
		}
	}
	shared actual Integer hash => id;
	"If we ignore ID, all Battlefields are equal."
	shared actual Boolean equalsIgnoringID(IFixture fixture) => fixture is Battlefield;
	shared actual String defaultImage = "battlefield.png";
	shared actual String plural() => "Battlefields";
	shared actual String shortDesc() => "signs of a long-ago battle";
	shared actual String string => "An ancient battlefield with DC ``dc``";
}