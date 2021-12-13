"An interface for objects that want to know when the current map version changes."
shared interface VersionChangeListener {
    "Handle a change in map version."
    shared formal void changeVersion(Integer previous, Integer newVersion);
}