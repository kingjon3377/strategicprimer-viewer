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
import java.util {
    JOptional=Optional
}

import lovelace.util.common {
    todo
}

import model.listeners {
    GraphicalParamsListener,
    SelectionChangeSupport,
    SelectionChangeListener
}
import model.map {
    Point,
    IMutableMapNG,
    PointFactory
}
import model.misc {
    SimpleDriverModel,
    IDriverModel
}
import model.viewer {
    IViewerModel,
    VisibleDimensions
}

import util {
    Pair
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
    shared actual Point selectedPoint => selPoint;
    assign selectedPoint {
        Point oldSel = selPoint;
        selPoint = selectedPoint;
        scs.fireChanges(oldSel, selPoint);
    }
    shared actual void setSelection(Point point) => selectedPoint = point;
    "Clear the selection."
    shared void clearSelection() => selectedPoint = PointFactory.invalidPoint;
    "The visible dimensions of the map."
    variable VisibleDimensions visDimensions;
    shared actual String string =>
            mapFile.map((path) => "ViewerModel for ``path``")
                .orElse("ViewerModel for an unsaved map");
    "Set the map and its filename, and also clear the selection and reset the visible
     dimensions and the zoom level."
    shared actual void setMap(IMutableMapNG newMap, JOptional<JPath> origin) {
        super.setMap(newMap, origin);
        clearSelection();
        visDimensions = VisibleDimensions(0, newMap.dimensions().rows - 1,
            0, newMap.dimensions().columns - 1);
        resetZoom();
    }
    shared new ("The initial map" IMutableMapNG theMap,
            "The file it was loaded from or should be saved to" JOptional<JPath> file)
            extends SimpleDriverModel() {
        visDimensions = VisibleDimensions(0, theMap.dimensions().rows - 1,
            0, theMap.dimensions().columns - 1);
        setMap(theMap, file);
    }
    shared new fromPair(
            "A pair of the initial map and its filename"
            Pair<IMutableMapNG, JOptional<JPath>> pair)
            extends ViewerModel(pair.first(), pair.second()) {}
    shared new copyConstructor(IDriverModel model) extends SimpleDriverModel() {
        if (is IViewerModel model) {
            visDimensions = model.dimensions;
            selPoint = model.selectedPoint;
        } else {
            visDimensions = VisibleDimensions(0, model.mapDimensions.rows - 1,
                0, model.mapDimensions.columns - 1);
        }
        setMap(model.map, model.mapFile);
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
}