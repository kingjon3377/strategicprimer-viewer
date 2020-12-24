import ceylon.collection {
    MutableList,
    ArrayList
}

import lovelace.util.common {
    todo,
    PathWrapper
}

import strategicprimer.drivers.common {
    SimpleDriverModel,
    IDriverModel,
    SelectionChangeListener
}
import strategicprimer.model.common.map {
    Point,
    IMutableMapNG,
    River,
    TileFixture,
    TileType
}

"A class to encapsulate the various model-type things views need to do with maps."
todo("Tests")
shared class ViewerModel extends SimpleDriverModel satisfies IViewerModel {
    "The starting zoom level."
    shared static Integer defaultZoomLevel = 8;

    "The maximum zoom level, to make sure that the tile size never overflows."
    static Integer maxZoomLevel = runtime.maxArraySize / 2;

    "The distance a 'jump' will take the cursor."
    static Integer jumpInterval = 5;

    "The list of graphical-parameter listeners."
    MutableList<GraphicalParamsListener> gpListeners =
            ArrayList<GraphicalParamsListener>();

    "The object to handle notifying selection-change listeners."
    SelectionChangeSupport scs = SelectionChangeSupport();

    "The current zoom level."
    variable Integer _zoomLevel = defaultZoomLevel;

    "The current zoom level."
    shared actual Integer zoomLevel => _zoomLevel;

    "Zoom in, increasing the zoom level."
    shared actual void zoomIn() {
        if (_zoomLevel < maxZoomLevel) {
            Integer oldZoom = _zoomLevel;
            _zoomLevel++;
            Integer newZoom = _zoomLevel;
            for (listener in gpListeners) {
                listener.tileSizeChanged(oldZoom, newZoom);
            }
        }
    }

    "Zoom out, decreasing the zoom level."
    shared actual void zoomOut() {
        if (_zoomLevel > 1) {
            Integer oldZoom = _zoomLevel;
            _zoomLevel--;
            Integer newZoom = _zoomLevel;
            for (listener in gpListeners) {
                listener.tileSizeChanged(oldZoom, newZoom);
            }
        }
    }

    "The currently selected point in the main map."
    variable Point selPoint = Point.invalidPoint;

    "The point currently pointed to by the scroll-bars."
    variable Point cursorPoint = Point(0, 0);

    "The point in the map the user has just right-clicked on, if any."
    variable Point? interactionPoint = null;

    "The visible dimensions of the map."
    variable VisibleDimensions visDimensions;

    shared new ("The initial map" IMutableMapNG theMap)
            extends SimpleDriverModel(theMap) {
        visDimensions = VisibleDimensions(0, theMap.dimensions.rows - 1, 0,
            theMap.dimensions.columns - 1);
    }

    deprecated("Filename now stored in the map")
    shared new fromEntry(
        "An [[Entry]] of the initial map and its filename"
        IMutableMapNG->[PathWrapper?, Boolean] entry)
            extends SimpleDriverModel(entry.key) {
        visDimensions = VisibleDimensions(0, entry.key.dimensions.rows - 1, 0,
            entry.key.dimensions.columns - 1);
    }

    deprecated("Filename now stored in the map")
    shared new fromPair(
        "A pair of the initial map and its filename"
        [IMutableMapNG, PathWrapper?] pair)
            extends SimpleDriverModel(pair.first) {
        visDimensions = VisibleDimensions(0, pair.first.dimensions.rows - 1, 0,
            pair.first.dimensions.columns - 1);
    }

    shared new copyConstructor(IDriverModel model)
            extends SimpleDriverModel(model.restrictedMap) {
        if (is IViewerModel model) {
            visDimensions = model.visibleDimensions;
            selPoint = model.selection;
            _zoomLevel = model.zoomLevel;
        } else {
            visDimensions = VisibleDimensions(0, model.mapDimensions.rows - 1,
                0, model.mapDimensions.columns - 1);
            _zoomLevel = defaultZoomLevel;
        }
    }

    "The visible dimensions of the map."
    shared actual VisibleDimensions visibleDimensions => visDimensions;
    assign visibleDimensions {
        if (visDimensions != visibleDimensions) {
            VisibleDimensions oldDimensions = visDimensions;
            visDimensions = visibleDimensions;
            // We notify listeners after the change, since one object's
            // dimensionsChanged() delegates to repaint(). (The other uses the parameter
            // we provide for robustness.)
            for (listener in gpListeners) {
                listener.dimensionsChanged(oldDimensions, visibleDimensions);
            }
        }
    }

    void fixVisibility() {
        Point currSelection = selPoint;
        VisibleDimensions currDims = visDimensions;
        Integer row;
        if (currSelection.row < 0) {
            row = 0;
        } else if (currSelection.row >= map.dimensions.rows) {
            row = map.dimensions.rows - 1;
        } else {
            row = currSelection.row;
        }
        Integer column;
        if (currSelection.column < 0) {
            column = 0;
        } else if (currSelection.column >= map.dimensions.columns) {
            column = map.dimensions.columns - 1;
        } else {
            column = currSelection.column;
        }
        Integer minRow;
        Integer maxRow;
        if (currDims.rows.contains(row)) {
            minRow = currDims.minimumRow;
            maxRow = currDims.maximumRow;
        } else if (currDims.minimumRow > row, currDims.minimumRow - row <= jumpInterval) {
            minRow = row;
            maxRow = currDims.maximumRow - (currDims.minimumRow - row);
        } else if (currDims.maximumRow < row, row - currDims.maximumRow <= jumpInterval) {
            minRow = currDims.minimumRow + (row - currDims.maximumRow);
            maxRow = row;
        } else if ((0:currDims.height).contains(row)) {
            minRow = 0;
            maxRow = currDims.height - 1;
        } else if (((map.dimensions.rows - currDims.height)..(map.dimensions.rows - 1))
                .contains(row)) {
            minRow = map.dimensions.rows - currDims.height;
            maxRow = map.dimensions.rows - 1;
        } else {
            minRow = row - currDims.height / 2;
            maxRow = minRow + currDims.height - 1;
        }
        Integer minColumn;
        Integer maxColumn;
        if (currDims.columns.contains(column)) {
            minColumn = currDims.minimumColumn;
            maxColumn = currDims.maximumColumn;
        } else if (currDims.minimumColumn > column,
                currDims.minimumColumn - column <= jumpInterval) {
            minColumn = column;
            maxColumn = currDims.maximumColumn - (currDims.minimumColumn - column);
        } else if (currDims.maximumColumn < column,
                column - currDims.maximumColumn <= jumpInterval) {
            minColumn = currDims.minimumColumn + (column - currDims.maximumColumn);
            maxColumn = column;
        } else if ((0:currDims.width).contains(column)) {
            minColumn = 0;
            maxColumn = currDims.width - 1;
        } else if (column in
                (map.dimensions.columns - currDims.width)..(map.dimensions.columns - 1)) {
            minColumn = map.dimensions.columns - currDims.width;
            maxColumn = map.dimensions.columns - 1;
        } else {
            minColumn = column - currDims.width / 2;
            maxColumn = minColumn + currDims.width - 1;
        }
        visibleDimensions = VisibleDimensions(minRow, maxRow, minColumn, maxColumn);
    }

    "Reset the zoom level to the default."
    shared actual void resetZoom() {
        Integer old = _zoomLevel;
        _zoomLevel = defaultZoomLevel;
        for (listener in gpListeners) {
            listener.tileSizeChanged(old, _zoomLevel);
        }
        fixVisibility();
    }

    "The point currently pointed to by the scroll-bars."
    shared actual Point cursor => cursorPoint;
    assign cursor {
        Point oldCursor = cursorPoint;
        cursorPoint = cursor;
        scs.fireCursorChanges(oldCursor, cursor);
    }

   "The currently selected point in the map."
    shared actual Point selection => selPoint;
    assign selection {
        Point oldSel = selPoint;
        selPoint = selection;
        if (selection.valid) {
            cursor = selection;
        } else {
            cursor = Point(largest(selection.row, 0), largest(selection.column, 0));
        }
        scs.fireChanges(oldSel, selPoint);
        fixVisibility();
    }

    "The point in the map the user has just right-clicked on, if any."
    shared actual Point? interaction => interactionPoint;
    assign interaction {
        interactionPoint = interaction;
        scs.fireInteraction();
    }

    "Clear the selection."
    shared void clearSelection() {
        selection = Point.invalidPoint;
        interaction = null;
    }

    shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
            scs.addSelectionChangeListener(listener);
    shared actual void removeSelectionChangeListener(SelectionChangeListener listener) =>
            scs.removeSelectionChangeListener(listener);
    shared actual void addGraphicalParamsListener(GraphicalParamsListener listener) =>
            gpListeners.add(listener);
    shared actual void removeGraphicalParamsListener(GraphicalParamsListener listener) =>
            gpListeners.remove(listener);

    shared actual String string {
        if (exists path = map.filename) {
            return "ViewerModel for ``path``";
        } else {
            return "ViewerModel for an unsaved map";
        }
    }

    "Set the map and its filename, and also clear the selection and reset the visible
     dimensions and the zoom level."
    shared actual void setMap(IMutableMapNG newMap) {
        super.setMap(newMap);
        clearSelection();
        visDimensions = VisibleDimensions(0, newMap.dimensions.rows - 1, 0,
            newMap.dimensions.columns - 1);
        resetZoom();
    }

    "Set whether a tile is mountainous."
    shared actual void setMountainous(Point location, Boolean mountainous) {
        restrictedMap.mountainous[location] = mountainous;
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Add a fixture to the map at a point."
    shared actual void addFixture(Point location, TileFixture fixture) {
        restrictedMap.addFixture(location, fixture);
        mapModified = true; // TODO: If addFixture() returns Boolean, only set this flag if this was a change?
    }

    "Remove a fixture from the map at a point."
    shared actual void removeMatchingFixtures(Point location, Boolean(TileFixture) condition) {
        for (fixture in map.fixtures.get(location).filter(condition)) {
            restrictedMap.removeFixture(location, fixture);
        }
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Add a bookmark at the given location."
    shared actual void addBookmark(Point location) {
        restrictedMap.addBookmark(location);
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Remove a bookmark at the current location."
    shared actual void removeBookmark(Point location) {
        restrictedMap.removeBookmark(location);
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Add a river at a location."
    shared actual void addRiver(Point location, River river) {
        restrictedMap.addRivers(location, river);
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Remove a river at a location."
    shared actual void removeRiver(Point location, River river) {
        restrictedMap.removeRivers(location, river);
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Set the map's terrain type at the given point."
    shared actual void setBaseTerrain(Point location, TileType? terrain) {
        restrictedMap.baseTerrain[location] = terrain;
        mapModified = true; // TODO: Only set the flag if this was a change?
    }
}
