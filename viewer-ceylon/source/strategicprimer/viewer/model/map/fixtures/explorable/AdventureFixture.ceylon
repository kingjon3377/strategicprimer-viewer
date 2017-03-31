import lovelace.util.common {
    todo
}

import strategicprimer.viewer.model.map {
    HasMutableOwner,
	IFixture
}

import model.map {
    Player
}
"A Fixture representing an adventure hook."
todo("Should be Subsettable, since players shouldn't know when another player completes an
      adventure on the far side of the world")
shared class AdventureFixture(owner, briefDescription, fullDescription, id)
		satisfies ExplorableFixture&HasMutableOwner {
	"A brief description of the adventure."
	shared String briefDescription;
	"A longer description of the adventure."
	shared String fullDescription;
	"A unique ID number."
	shared actual Integer id;
	"The filename of an image to use as an icon for this instance."
	shared actual variable String image = "";
	"The player that has undertaken the adventure."
	shared actual variable Player owner;
	"Clone the fixture."
	shared actual AdventureFixture copy(Boolean zero) {
		AdventureFixture retval = AdventureFixture(owner, briefDescription,
			fullDescription, id);
		retval.image = image;
		return retval;
	}
	shared actual String string {
		if (fullDescription.empty) {
			if (briefDescription.empty) {
				return "Adventure hook";
			} else {
				return briefDescription;
			}
		} else {
			return fullDescription;
		}
	}
	shared actual String defaultImage= "adventure.png";
	shared actual Boolean equalsIgnoringID(IFixture fixture) {
		if (is AdventureFixture obj = fixture) {
			return ((owner.independent && obj.owner.independent) ||
					(owner.playerId == obj.owner.playerId)) &&
				briefDescription == obj.briefDescription &&
				fullDescription == obj.fullDescription;
		} else {
			return false;
		}
	}
	shared actual Boolean equals(Object obj) {
		if (is AdventureFixture obj) {
			return id == obj.id && equalsIgnoringID(obj);
		} else {
			return false;
		}
	}
	shared actual Integer hash => id;
	shared actual String plural => "Adventures";
	shared actual String shortDescription => briefDescription;
	"The required Perception check result for an explorer to find the adventure hook."
	todo("Should probably be variable, i.e. read from XML")
	shared actual Integer dc => 30;
}