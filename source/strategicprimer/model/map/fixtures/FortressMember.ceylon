import lovelace.util.common {
    todo
}

import strategicprimer.model.map.fixtures {
    SubsettableFixture
}
"A (marker) interface for things that can be in a fortress."
todo("Members?")
shared interface FortressMember satisfies SubsettableFixture {
	"Specialization."
	shared actual formal FortressMember copy(Boolean zero);
}