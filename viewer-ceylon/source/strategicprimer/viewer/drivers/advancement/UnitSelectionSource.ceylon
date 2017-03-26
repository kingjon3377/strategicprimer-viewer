"An interface for objects that handle the user's selection of a unit."
shared interface UnitSelectionSource {
    "Add a listener."
    shared formal void addUnitSelectionListener(UnitSelectionListener listener);
    "Remove a listener."
    shared formal void removeUnitSelectionListener(UnitSelectionListener listener);
}