"An interface for the map viewer driver."
shared interface ViewerDriver satisfies GUIDriver {
    "Center the view on the currently selected tile."
    shared formal void center();
    "Zoom in."
    shared formal void zoomIn();
    "Zoom out."
    shared formal void zoomOut();
    "Reset the zoom level."
    shared formal void resetZoom();
}