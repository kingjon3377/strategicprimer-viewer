import java.util {
    EventListener
}
"An interface for objects that want to know when the current map version changes."
shared interface VersionChangeListener satisfies EventListener {
    "Handle a change in map version."
    shared formal void changeVersion(Integer previous, Integer newVersion);
}