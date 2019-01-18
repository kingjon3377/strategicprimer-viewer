import strategicprimer.model.common.map {
    HasName
}

"Possible actions a trapper can take; top-level so we can switch on the cases,
 since the other alternative, `static`, isn't feasible where this is used right now."
shared class TrapperCommand of setTrap | check | move | easyReset | quit
        satisfies HasName&Comparable<TrapperCommand> {
    shared actual String name;
    Integer ordinal;
    shared new setTrap { name = "Set or reset a trap"; ordinal = 0; }
    shared new check { name = "Check a trap"; ordinal = 1; }
    shared new move { name = "Move to another trap"; ordinal = 2; }
    shared new easyReset { name = "Reset a foothold trap, e.g."; ordinal = 3; }
    shared new quit { name = "Quit"; ordinal = 4; }
    shared actual Comparison compare(TrapperCommand other) => ordinal <=> other.ordinal;
}
