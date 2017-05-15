"An interface for objects that tell listeners when the visible dimensions or the tile
 size/zoom level changed."
shared interface GraphicalParamsSource {
    "Add a listener."
    shared formal void addGraphicalParamsListener(GraphicalParamsListener listener);
    "Remove a listener."
    shared formal void removeGraphicalParamsListener(GraphicalParamsListener listener);
}