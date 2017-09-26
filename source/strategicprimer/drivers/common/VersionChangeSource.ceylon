"An interface for things that will be able to tell when the map version changes."
shared interface VersionChangeSource {
    "Add a listener."
    shared formal void addVersionChangeListener(VersionChangeListener listener);
    "Remove a listener."
    shared formal void removeVersionChangeListener(VersionChangeListener listener);
}