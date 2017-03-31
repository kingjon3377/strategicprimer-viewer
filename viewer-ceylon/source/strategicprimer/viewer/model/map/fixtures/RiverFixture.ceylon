import ceylon.collection {
    MutableSet,
	HashSet
}

import lovelace.util.common {
    todo
}
import strategicprimer.viewer.model.map.fixtures {
	SubsettableFixture
}
import strategicprimer.viewer.model.map {
	River,
	TileFixture,
    IFixture
}
"A Fixture to encapsulate the rivers on a tile, so we can show a chit for rivers."
todo("We'd like to remove this class")
shared class RiverFixture(River* initial) satisfies TileFixture&{River*}&SubsettableFixture {
	"The Set we're using to hold the Rivers."
	MutableSet<River> riversSet = HashSet<River> { *initial };
	shared actual RiverFixture copy(Boolean zero) => RiverFixture(*riversSet);
	shared void addRiver(River river) => riversSet.add(river);
	shared void removeRiver(River river) => riversSet.remove(river);
	shared Set<River> rivers => set { *riversSet };
	shared actual Iterator<River> iterator() => riversSet.iterator();
	shared actual Boolean equals(Object obj) {
		if (is RiverFixture obj) {
			return obj.rivers == riversSet;
		} else {
			return false;
		}
	}
	"Because of Java bug #6579200, this had to return a constant in the Java version, and
	 I doubt Ceylon collections are any different."
	shared actual Integer hash = 0;
	shared actual String string => " ".join(rivers.map(River.description));
	"A fixture is a subset if it is a RiverFixture with no rivers we don't have."
	shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
		if (is RiverFixture obj) {
			Set<River> complement = obj.rivers.complement(riversSet);
			if (complement.empty) {
				return true;
			} else {
				report("Extra rivers:\t``", ".join(complement)``");
				return false;
			}
		} else {
			report("Incompatible type to RiverFixture");
			return false;
		}
	}
	"""We return a constant "ID" because this is really a container for a collection of Rivers.

	   Perhaps rivers should have IDs (and names ..), though."""
	todo("Investigate how FreeCol does it")
	shared actual Integer id = -1;
	shared actual Boolean equalsIgnoringID(IFixture fixture) => equals(fixture);
	shared actual String plural = "Rivers";
	shared actual String shortDescription => "a river";
	"The required Perception check result for an explorer to find the rivers."
	shared actual Integer dc = 5;
}