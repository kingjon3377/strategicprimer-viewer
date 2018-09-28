import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
"An interface for objects that want to know when the user selects a Unit from a list or
 tree."
todo("Combine with other similar interfaces?")
shared interface UnitSelectionListener {
    "Respond to the fact that the given unit is the new selected unit."
    shared formal void selectUnit(IUnit? unit);
}
