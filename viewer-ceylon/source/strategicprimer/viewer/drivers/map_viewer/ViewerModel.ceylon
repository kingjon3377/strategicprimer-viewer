import ceylon.collection {
    MutableList,
    ArrayList
}

import java.lang {
    JInteger=Integer
}
import java.nio.file {
    JPath=Path
}

import lovelace.util.common {
    todo
}

import model.listeners {
    SelectionChangeSupport,
    SelectionChangeListener
}
import strategicprimer.viewer.model.map {
    IMutableMapNG,
    IMapNG
}
import model.map {
    Point,
    PointFactory
}

import strategicprimer.viewer.model {
    SimpleDriverModel,
    IDriverModel
}
"A class to encapsulate the various model-type things views need to do with maps."
todo("Tests")
shared class ViewerModel extends SimpleDriverModel satisfies IViewerModel {
    "The starting zoom level."
    shared static Integer defaultZoomLevel = 8;
    "The maximum zoom level, to make sure that the tile size never overflows."
    static Integer maxZoomLevel = JInteger.maxValue / 4;
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
            _zoomLevel++;
            for (listener in gpListeners) {
                listener.tileSizeChanged(_zoomLevel - 1, _zoomLevel);
            }
        }
    }
    "Zoom out, decreasing the zoom level."
    shared actual void zoomOut() {
        if (_zoomLevel > 1) {
            _zoomLevel--;
            for (listener in gpListeners) {
                listener.tileSizeChanged(_zoomLevel + 1, _zoomLevel);
            }
        }
    }
    "Reset the zoom level to the default."
    shared actual void resetZoom() {
        Integer old = _zoomLevel;
        _zoomLevel = defaultZoomLevel;
        for (listener in gpListeners) {
            listener.tileSizeChanged(old, _zoomLevel);
        }
    }
    "The currently selected point in the main map."
    variable Point selPoint = PointFactory.invalidPoint;
    "The currently selected point in the map."
    todo("Rename to `selection`")
    shared actual Point selection => selPoint;
    assign selection {
        Point oldSel = selPoint;
        selPoint = selection;
        scs.fireChanges(oldSel, selPoint);
    }
    "Clear the selection."
    shared void clearSelection() => selection = PointFactory.invalidPoint;
    "The visible dimensions of the map."
    variable VisibleDimensions visDimensions;
    shared new ("The initial map" IMutableMapNG theMap,
            "The file it was loaded from or should be saved to" JPath? file)
            extends SimpleDriverModel(theMap, file) {
        visDimensions = VisibleDimensions(0, theMap.dimensions.rows - 1, 0,
            theMap.dimensions.columns - 1);
    }
    shared new fromPair(
            "A pair of the initial map and its filename"
            [IMutableMapNG, JPath?] pair)
            extends ViewerModel(pair.first, pair.rest.first) {}
    void postSetMap(IMapNG newMap) {
        clearSelection();
        visDimensions = VisibleDimensions(0, newMap.dimensions.rows - 1, 0,
            newMap.dimensions.columns - 1);
        resetZoom();
    }
    shared new copyConstructor(IDriverModel model)
            extends SimpleDriverModel(model.map, model.mapFile) {
        if (is IViewerModel model) {
            visDimensions = model.dimensions;
            selPoint = model.selection;
        } else {
            visDimensions = VisibleDimensions(0, model.mapDimensions.rows - 1,
                0, model.mapDimensions.columns - 1);
        }
        resetZoom();
    }
    "The visible dimensions of the map."
    shared actual VisibleDimensions dimensions => visDimensions;
    assign dimensions {
        // TODO: should we notify listeners before or after the change? Do they use the
        // params we pass them, or get them from us?
        for (listener in gpListeners) {
            listener.dimensionsChanged(visDimensions, dimensions);
        }
        visDimensions = dimensions;
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
        if (exists path = mapFile) {
            return "ViewerModel for ``path``";
        } else {
            return "ViewerModel for an unsaved map";
        }
    }
    "Set the map and its filename, and also clear the selection and reset the visible
     dimensions and the zoom level."
    shared actual void setMap(IMutableMapNG newMap, JPath? origin) {
        super.setMap(newMap, origin);
        postSetMap(newMap);
    }
}