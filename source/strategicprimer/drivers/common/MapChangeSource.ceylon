"An interface for things that can fire notifications of a new map being loaded."
shared interface MapChangeSource {
    "Notify the given listener of any newly loaded maps."
    shared formal void addMapChangeListener(MapChangeListener listener);

    "Stop notifying the given listener."
    shared formal void removeMapChangeListener(MapChangeListener listener);
}
