import strategicprimer.model.common.map {
    Point,
    River,
    TileFixture,
    TileType
}
import strategicprimer.drivers.common {
    IDriverModel,
    IFixtureEditingModel,
    SelectionChangeSource
}

"An interface for a model behind the map viewer, handling the selected tile and visible
 dimensions."
shared interface IViewerModel
        satisfies IDriverModel&SelectionChangeSource&GraphicalParamsSource&IFixtureEditingModel {
    "The coordinates of the currently selected tile."
    shared formal variable Point selection;

    "The coordinates of the tile currently pointed to by the scroll-bars."
    shared formal variable Point cursor;

    "The coordinates of the tile the user is currently interacting with, if any. This should be set when the user
     right-clicks (or equivalent) on a tile, and unset at the end of the operation handling that click."
    shared formal variable Point? interaction;

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

    "Set whether a tile is mountainous."
    shared formal void setMountainous(Point location, Boolean mountainous);

    "Add a fixture to the map at a point."
    shared formal void addFixture(Point location, TileFixture fixture);

    "Remove a fixture from the map at a point."
    shared formal void removeMatchingFixtures(Point location, Boolean(TileFixture) condition);

    "Add a bookmark at the given location."
    shared formal void addBookmark(Point location);

    "Remove a bookmark at the current location."
    shared formal void removeBookmark(Point location);

    "Add a river at a location."
    shared formal void addRiver(Point location, River river);

    "Remove a river at a location."
    shared formal void removeRiver(Point location, River river);

    "Set the map's terrain type at the given point."
    shared formal void setBaseTerrain(Point location, TileType? terrain);
}
