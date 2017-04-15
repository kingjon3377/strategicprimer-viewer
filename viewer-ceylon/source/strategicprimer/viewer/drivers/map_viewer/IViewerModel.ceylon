
import strategicprimer.model.map {
    Point
}
import strategicprimer.drivers.common {
    IDriverModel,
    SelectionChangeSource
}
"An interface for a model behind the map viewer, handling the selected tile and visible
 dimensions."
shared interface IViewerModel satisfies IDriverModel&SelectionChangeSource&GraphicalParamsSource {
    "The coordinates of the currently selected tile."
    shared formal variable Point selection;
    "The visible dimensions of the map."
    shared formal variable VisibleDimensions visibleDimensions;
    "The current zoom level."
    shared formal Integer zoomLevel;
    "Zoom in."
    shared formal void zoomIn();
    "Zoom out."
    shared formal void zoomOut();
    "Reset the zoom level to the default."
    shared formal void resetZoom();
}