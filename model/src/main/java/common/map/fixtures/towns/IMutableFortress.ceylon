import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    HasMutableName,
    HasMutableImage
}

import strategicprimer.model.common.map.fixtures {
    FortressMember
}

"A fortress on the map. A player can only have one fortress per tile, but multiple players
  may have fortresses on the same tile."
todo("FIXME: We need something about buildings yet")
shared interface IMutableFortress
        satisfies IFortress&HasMutableImage&IMutableTownFixture&HasMutableName {
    "Add a member to the fortress."
    shared formal void addMember(FortressMember member);

    "Remove a member from the fortress."
    shared formal void removeMember(FortressMember member);

    "Clone the fortress."
    shared actual formal IMutableFortress copy(Boolean zero);
}
