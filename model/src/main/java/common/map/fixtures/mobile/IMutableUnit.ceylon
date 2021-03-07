import strategicprimer.model.common.map.fixtures {
    UnitMember
}

import strategicprimer.model.common.map {
    HasMutableImage,
    HasMutableKind,
    HasMutableName,
    HasMutableOwner,
    HasMutablePortrait
}

"An interface for mutator methods on units."
shared interface IMutableUnit
        satisfies IUnit&HasMutableKind&HasMutableName&HasMutableImage&HasMutableOwner&HasMutablePortrait {
    "Set the unit's orders for a turn."
    shared formal void setOrders(Integer turn, String newOrders);

    "Set the unit's results for a turn."
    shared formal void setResults(Integer turn, String newResults);

    "Add a member."
    shared formal void addMember(UnitMember member);

    "Remove a member"
    shared formal void removeMember(UnitMember member);

    "Change the internal order of members to be sorted. Sort order is implementation-defined."
    shared formal void sortMembers();

    "Clone the unit."
    shared formal actual IMutableUnit copy(Boolean zero);
}
