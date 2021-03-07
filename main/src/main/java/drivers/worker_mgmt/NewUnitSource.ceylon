import strategicprimer.drivers.worker.common {
    NewUnitListener
}

"An interface for [[NewUnitListener]]s to listen to."
shared interface NewUnitSource {
    "Add a listener."
    shared formal void addNewUnitListener(NewUnitListener listener);

    "Remove a listener."
    shared formal void removeNewUnitListener(NewUnitListener listener);
}
