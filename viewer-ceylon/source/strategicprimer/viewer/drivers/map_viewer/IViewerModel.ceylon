import lovelace.util.common {
    todo
}

import model.listeners {
    SelectionChangeSource
}
import model.map {
    Point
}
import strategicprimer.viewer.model {
    IDriverModel
}
"An interface for a model behind the map viewer, handling the selected tile and visible
 dimensions."
shared interface IViewerModel satisfies IDriverModel&SelectionChangeSource&GraphicalParamsSource {
    "The coordinates of the currently selected tile."
    shared formal variable Point selection;
    "The visible dimensions of the map."
    todo("Rename to visibleDimensions")
    shared formal variable VisibleDimensions dimensions;
    "The current zoom level."
    shared formal Integer zoomLevel;
    "Zoom in."
    shared formal void zoomIn();
    "Zoom out."
    shared formal void zoomOut();
    "Reset the zoom level to the default."
    shared formal void resetZoom();
}