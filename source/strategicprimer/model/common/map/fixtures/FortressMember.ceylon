import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    IFixture,
    Subsettable
}
"A (marker) interface for things that can be in a fortress."
todo("Members?")
shared interface FortressMember satisfies IFixture&Subsettable<IFixture> {
    "Specialization."
    shared actual formal FortressMember copy(Boolean zero);
}
