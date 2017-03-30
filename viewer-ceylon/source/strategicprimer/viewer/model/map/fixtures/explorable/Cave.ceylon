import strategicprimer.viewer.model.map.fixtures {
	IEvent
}
import model.map {
    IFixture
}
""""There are extensive caves beneath this tile"."""
shared class Cave(dc, id) satisfies IEvent&ExplorableFixture {
	"The required Perception check result to discover the caves."
	shared actual Integer dc;
	"A unique ID."
	shared actual Integer id;
	"The filename of an image to use as an icon for this instance."
	shared actual variable String image = "";
	"Clone the object."
	shared actual Cave copy(Boolean zero) {
		Cave retval = Cave((zero) then 0 else dc, id);
		retval.image = image;
		return retval;
	}
	shared actual String text = "There are extensive caves beneath this tile.";
	shared actual Boolean equals(Object obj) {
		if (is Cave obj) {
			return id == obj.id;
		} else {
			return false;
		}
	}
	shared actual Integer hash => id;
	shared actual String string => "Caves with DC ``dc``";
	"If we ignore ID (and DC), all caves are equal."
	shared actual Boolean equalsIgnoringID(IFixture fixture) => fixture is Cave;
	by("MrBeast", "http://opengameart.org/content/cave-tileset-0")
	shared actual String defaultImage = "cave.png";
	shared actual String plural = "Caves";
	shared actual String shortDescription => "caves underground";
}