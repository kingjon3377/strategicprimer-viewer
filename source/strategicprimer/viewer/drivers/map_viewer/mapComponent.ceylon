import ceylon.math.float {
    halfEven
}

import java.awt {
    Graphics,
    Color,
    Rectangle
}
import java.awt.event {
    ComponentEvent,
    MouseMotionAdapter,
    MouseListener,
    MouseEvent,
    ComponentAdapter
}

import javax.swing {
    JComponent,
    SwingUtilities
}

import lovelace.util.common {
    Comparator
}

import strategicprimer.drivers.common {
    MapChangeListener,
    SelectionChangeListener,
    SelectionChangeSource,
	FixtureMatcher
}

import strategicprimer.model.map {
    Point,
    MapDimensions,
    TileFixture,
    pointFactory
}
"An interface for a UI representing a map."
shared interface MapGUI {
    "The driver model the GUI represents."
    shared formal IViewerModel mapModel;
}
"A component to display the map, even a large one, without the performance problems that
 came from drawing the entire map every time and letting Java manage the scrolling or,
 worse, instantiating a GUITile object for every visible tile every time the map was
 scrolled (or, yet worse again, a GUITile for every tile in the map, and removing them all
 and adding the visible tiles back in every time the map was scrolled)."
JComponent&MapGUI&MapChangeListener&SelectionChangeListener&GraphicalParamsListener
mapComponent(IViewerModel model, Boolean(TileFixture) zof,
        Iterable<FixtureMatcher>&Comparator<TileFixture> matchers) {
    MouseListener&ToolTipSource&SelectionChangeSource cml =
            componentMouseListener(model, zof, matchers.compare);
    DirectionSelectionChanger dsl = DirectionSelectionChanger(model);
    Rectangle boundsCheck(Rectangle? rect) {
        if (exists rect) {
            return rect;
        } else {
            Integer tileSize = scaleZoom(model.zoomLevel,
                model.mapDimensions.version);
            VisibleDimensions dimensions = model.visibleDimensions;
            return Rectangle(0, 0, dimensions.width * tileSize,
                dimensions.height * tileSize);
        }
    }
    void fixVisibility() {
        Point selectedPoint = model.selection;
        Integer selectedRow = largest(selectedPoint.row, 0);
        Integer selectedColumn = largest(selectedPoint.column, 0);
        VisibleDimensions visibleDimensions = model.visibleDimensions;
        variable Integer minimumRow = visibleDimensions.minimumRow;
        variable Integer maximumRow = visibleDimensions.maximumRow;
        variable Integer minimumColumn = visibleDimensions.minimumColumn;
        variable Integer maximumColumn = visibleDimensions.maximumColumn;
        if (selectedRow < minimumRow) {
            Integer difference = minimumRow - selectedRow;
            minimumRow -= difference;
            maximumRow -= difference;
        } else if (selectedRow > maximumRow) {
            Integer difference = selectedRow - maximumRow;
            minimumRow += difference;
            maximumRow += difference;
        }
        if (selectedColumn < minimumColumn) {
            Integer difference = minimumColumn - selectedColumn;
            minimumColumn -= difference;
            maximumColumn -= difference;
        } else if (selectedColumn > maximumColumn) {
            Integer difference = selectedColumn - maximumColumn;
            minimumColumn += difference;
            maximumColumn += difference;
        }
        model.visibleDimensions = VisibleDimensions(minimumRow, maximumRow, minimumColumn,
            maximumColumn);
    }
    object retval extends JComponent() satisfies MapGUI&MapChangeListener&
            SelectionChangeListener&GraphicalParamsListener {
        variable TileDrawHelper helper = tileDrawHelperFactory(
            model.mapDimensions.version, imageUpdate, zof,
            matchers);
        doubleBuffered = true;
        shared actual IViewerModel mapModel = model;
        shared actual String? getToolTipText(MouseEvent event) =>
                cml.getToolTipText(event);
        shared actual void dimensionsChanged(VisibleDimensions oldDim,
                VisibleDimensions newDim) => repaint();
        void paintTile(Graphics pen, Integer tileSize, Point point, Integer row,
                Integer column, Boolean selected) {
            helper.drawTile(pen, model.map, point,
                coordinateFactory(column * tileSize, row * tileSize),
                coordinateFactory(tileSize, tileSize));
            if (selected) {
                Graphics context = pen.create();
                try {
                    context.color = Color.black;
                    context.drawRect((column * tileSize) + 1, (row * tileSize) + 1,
                        tileSize - 2, tileSize - 2);
                } finally {
                    context.dispose();
                }
            }
        }
        shared actual void tileSizeChanged(Integer olSize, Integer newSize) {
            ComponentEvent event = ComponentEvent(this, ComponentEvent.componentResized);
            for (listener in componentListeners) {
                listener.componentResized(event);
            }
            repaint();
        }
        Boolean selectionVisible {
            Point selectedPoint = model.selection;
            Integer selectedRow = largest(selectedPoint.row, 0);
            Integer selectedColumn = largest(selectedPoint.column, 0);
            VisibleDimensions visibleDimensions = model.visibleDimensions;
            if (visibleDimensions.rows.contains(selectedRow),
                visibleDimensions.columns.contains(selectedColumn)) {
                return true;
            } else {
                return false;
            }
        }
        shared actual void selectedPointChanged(Point? old, Point newPoint) {
            SwingUtilities.invokeLater(() => requestFocusInWindow());
            if (!selectionVisible) {
                fixVisibility();
            }
            repaint();
        }
        shared actual void mapChanged() {
            helper = tileDrawHelperFactory(model.mapDimensions.version,
                imageUpdate, zof, matchers);
        }
        shared actual void paint(Graphics pen) {
            Graphics context = pen.create();
            try {
                context.color = Color.white;
                context.fillRect(0, 0, width, height);
                Rectangle bounds = boundsCheck(context.clipBounds);
                MapDimensions mapDimensions = model.mapDimensions;
                Integer tileSize = scaleZoom(model.zoomLevel, mapDimensions.version);
                void drawMapPortion(Integer minX, Integer minY, Integer maxX,
                        Integer maxY) {
                    Integer minRow = model.visibleDimensions.minimumRow;
                    Integer maxRow = model.visibleDimensions.maximumRow;
                    Integer minCol = model.visibleDimensions.minimumColumn;
                    Integer maxCol = model.visibleDimensions.maximumColumn;
                    for (i in minY .. maxY) {
                        if ((i + minRow)>=(maxRow + 1)) {
                            break;
                        }
                        for (j in minX..maxX) {
                            if ((j + minCol) >= (maxCol + 1)) {
                                break;
                            }
                            Point location = pointFactory(i + minRow, j + minCol);
                            paintTile(context, tileSize, location, i, j,
                                model.selection == location);
                        }
                    }
                }
                drawMapPortion(halfEven(bounds.minX / tileSize).plus(0.1).integer,
                    halfEven(bounds.minY / tileSize).plus(0.1).integer,
                    smallest(halfEven(bounds.maxX / tileSize).plus(1.1).integer,
                        mapDimensions.columns),
                    smallest(halfEven(bounds.maxY / tileSize).plus(1.1).integer,
                        mapDimensions.rows));
            } finally {
                context.dispose();
            }
            super.paint(pen);
        }
    }
    cml.addSelectionChangeListener(retval);
    retval.addMouseListener(cml);
    retval.addMouseWheelListener(dsl);
    assert (exists actionMap = retval.actionMap,
        exists inputMap = retval.getInputMap(JComponent.whenAncestorOfFocusedComponent));
    arrowListenerInitializer.setUpArrowListeners(dsl, inputMap, actionMap);
    object mapSizeListener extends ComponentAdapter() {
        shared actual void componentResized(ComponentEvent event) {
            Integer tileSize = scaleZoom(model.zoomLevel, model.mapDimensions.version);
            Integer visibleColumns = retval.width / tileSize;
            Integer visibleRows = retval.height / tileSize;
            variable Integer minimumColumn = model.visibleDimensions.minimumColumn;
            variable Integer maximumColumn = model.visibleDimensions.maximumColumn;
            variable Integer minimumRow = model.visibleDimensions.minimumRow;
            variable Integer maximumRow = model.visibleDimensions.maximumRow;
            MapDimensions mapDimensions = model.mapDimensions;
            if (visibleColumns != (maximumColumn - minimumColumn) ||
            visibleRows != (maximumRow - minimumRow)) {
                Integer totalColumns = mapDimensions.columns;
                if (visibleColumns >= totalColumns) {
                    minimumColumn = 0;
                    maximumColumn = totalColumns - 1;
                } else if (minimumColumn + visibleColumns >= totalColumns) {
                    maximumColumn = totalColumns - 1;
                    minimumColumn = totalColumns - visibleColumns - 2;
                } else {
                    maximumColumn = (minimumColumn + visibleColumns) - 1;
                }
                Integer totalRows = mapDimensions.rows;
                if (visibleRows >= totalRows) {
                    minimumRow = 0;
                    maximumRow = totalRows - 1;
                } else if ((minimumRow + visibleRows) >= totalRows) {
                    maximumRow = totalRows - 1;
                    minimumRow = totalRows - visibleRows - 2;
                } else {
                    maximumRow = minimumRow + visibleRows - 1;
                }
                model.visibleDimensions = VisibleDimensions(minimumRow, maximumRow,
                    minimumColumn, maximumColumn);
            }
        }
        shared actual void componentShown(ComponentEvent event) =>
                componentResized(event);
    }
    retval.addComponentListener(mapSizeListener);
    retval.toolTipText = "";
    object mouseMotionListener extends MouseMotionAdapter() {
        shared actual void mouseMoved(MouseEvent event) => retval.repaint();
    }
    retval.addMouseMotionListener(mouseMotionListener);
    retval.requestFocusEnabled = true;
    return retval;
}
