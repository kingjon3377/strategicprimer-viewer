import lovelace.util.common {
    todo
}

import model.map {
    SubsettableFixture
}
"A (marker) interface for things that can be in a fortress."
todo("Members?")
shared interface FortressMember satisfies SubsettableFixture {
	"Specialization."
	shared actual formal FortressMember copy(Boolean zero);
}