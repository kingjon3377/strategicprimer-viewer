import ceylon.numeric.float {
    halfEven
}

import java.awt {
    Component,
    Graphics,
    Color,
    Rectangle
}
import java.awt.event {
    ActionEvent,
    KeyEvent,
    ComponentEvent,
    MouseMotionAdapter,
    MouseEvent,
    ComponentAdapter
}

import javax.swing {
    AbstractAction,
    KeyStroke,
    JComponent,
    SwingUtilities
}

import lovelace.util.common {
    as,
    Comparator
}

import strategicprimer.drivers.common {
    MapChangeListener,
    SelectionChangeListener,
    FixtureMatcher
}

import strategicprimer.model.common.map {
    Point,
    TileFixture,
    MapDimensions
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import java.awt.image {
    BufferedImage
}

"A component to display the map, even a large one, without the performance problems that
 came from drawing the entire map every time and letting Java manage the scrolling or,
 worse, instantiating a GUITile object for every visible tile every time the map was
 scrolled (or, yet worse again, a GUITile for every tile in the map, and removing them all
 and adding the visible tiles back in every time the map was scrolled)."
class MapComponent extends JComponent satisfies MapGUI&MapChangeListener&
        SelectionChangeListener&GraphicalParamsListener {
    shared actual IViewerModel mapModel;
    ComponentMouseListener cml;
    DirectionSelectionChanger dsl;
    variable TileDrawHelper helper;

    shared variable BufferedImage? backgroundImage = null;

    shared new (IViewerModel model, Boolean(TileFixture) zof,
            {FixtureMatcher*}&Comparator<TileFixture> matchers) extends JComponent() {
        mapModel = model;
        cml = ComponentMouseListener(model, zof, matchers.compare);
        dsl = DirectionSelectionChanger(model);
        helper = Ver2TileDrawHelper(imageUpdate, zof, matchers);
        doubleBuffered = true;
    }

    // TODO: cache this?
    Integer tileSize => scaleZoom(mapModel.zoomLevel, mapModel.mapDimensions.version);

    Rectangle boundsCheck(Rectangle? rect) {
        if (exists rect) {
            return rect;
        } else {
            VisibleDimensions dimensions = mapModel.visibleDimensions;
            return Rectangle(0, 0, dimensions.width * tileSize,
                dimensions.height * tileSize);
        }
    }

    void fixVisibility() {
        Point selectedPoint = mapModel.selection;
        Integer selectedRow = largest(selectedPoint.row, 0);
        Integer selectedColumn = largest(selectedPoint.column, 0);
        VisibleDimensions visibleDimensions = mapModel.visibleDimensions;
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
        mapModel.visibleDimensions = VisibleDimensions(minimumRow, maximumRow,
            minimumColumn, maximumColumn);
    }

    shared actual String? getToolTipText(MouseEvent event) =>
            cml.getToolTipText(event);

    shared actual void dimensionsChanged(VisibleDimensions oldDim,
            VisibleDimensions newDim) => repaint();

    void paintTile(Graphics pen, Integer tileSize, Point point, Integer row,
            Integer column, Boolean selected) {
        if (!point in mapModel.map.dimensions) {
            return;
        }
        helper.drawTile(pen, mapModel.map, point,
            Coordinate(column * tileSize, row * tileSize),
            Coordinate(tileSize, tileSize));
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

    Boolean selectionVisible {
        Point selectedPoint = mapModel.selection;
        Integer selectedRow = largest(selectedPoint.row, 0);
        Integer selectedColumn = largest(selectedPoint.column, 0);
        VisibleDimensions visibleDimensions = mapModel.visibleDimensions;
        return visibleDimensions.rows.contains(selectedRow) &&
        visibleDimensions.columns.contains(selectedColumn);
    }

    // Can't take method reference to requestFocusInWindow() because it's overloaded
    void requestFocusNarrowly() => requestFocusInWindow();

    "If the point is currently visible, call the overloading of [[repaint]] that takes
     coordinates, passing the coordinates describing the point."
    void repaintPoint(Point point) {
        value visibleDimensions = mapModel.visibleDimensions;
        Integer row = largest(point.row, 0);
        Integer column = largest(point.column, 0);
        if (row in visibleDimensions.rows, column in visibleDimensions.columns) {
            repaint((column - visibleDimensions.minimumColumn) * tileSize,
                (row - visibleDimensions.minimumRow) * tileSize, tileSize, tileSize);
        }
    }

    shared actual void selectedPointChanged(Point? old, Point newPoint) {
        SwingUtilities.invokeLater(requestFocusNarrowly);
        if (selectionVisible) {
            if (exists old, old != newPoint) {
                repaintPoint(old);
            }
            repaintPoint(newPoint);
        } else {
            fixVisibility();
            repaint();
        }
    }

    shared actual void cursorPointChanged(Point? old, Point newCursor) {} // TODO: check visibility of cursor point here?

    shared actual void mapChanged() {}

    void drawBackgroundImage(Graphics context, Integer tileSize) {
        if (exists temp = backgroundImage) {
            value visibleDimensions = mapModel.visibleDimensions;
            value mapDimensions = mapModel.mapDimensions;
            Float horizontalScaling = temp.width.float / mapDimensions.columns;
            Float verticalScaling = temp.height.float / mapDimensions.rows;
            Integer x = (visibleDimensions.minimumColumn * horizontalScaling).integer;
            Integer y = (visibleDimensions.minimumRow * verticalScaling).integer;
            Integer sliceWidth = (visibleDimensions.width * horizontalScaling).integer;
            Integer sliceHeight = (visibleDimensions.height * verticalScaling).integer;
            // FIXME: Cache this, regenerating when visible dimensions change, somehow
            value sliced = temp.getSubimage(Integer.largest(0, x), Integer.largest(0, y),
                Integer.smallest(temp.width - x - 1, sliceWidth), Integer.smallest(temp.height - y - 1, sliceHeight));
            context.drawImage(sliced, 0, 0, visibleDimensions.width * tileSize, visibleDimensions.height * tileSize,
                null); // TODO: supply observer
        } else {
            context.color = Color.white;
            context.fillRect(0, 0, width, height);
        }
    }

    // FIXME: Are these map or screen coordinates? If screen coordinates, what are they relative to?
    void drawMapPortion(Graphics context, Integer tileSize, Integer minX,
            Integer minY, Integer maxX, Integer maxY) {
        Integer minRow = mapModel.visibleDimensions.minimumRow;
        Integer maxRow = mapModel.visibleDimensions.maximumRow;
        Integer minCol = mapModel.visibleDimensions.minimumColumn;
        Integer maxCol = mapModel.visibleDimensions.maximumColumn;
        for (i in minY .. maxY) {
            if ((i + minRow)>=(maxRow + 1)) {
                break;
            }
            for (j in minX..maxX) {
                if ((j + minCol) >= (maxCol + 1)) {
                    break;
                }
                Point location = Point(i + minRow, j + minCol);
                paintTile(context, tileSize, location, i, j,
                    mapModel.selection == location);
            }
        }
    }

    shared actual void paint(Graphics pen) {
        super.paint(pen);
        Graphics context = pen.create();
        try {
            context.color = Color.white;
            context.fillRect(0, 0, width, height);
            Rectangle bounds = boundsCheck(context.clipBounds);
            MapDimensions mapDimensions = mapModel.mapDimensions;
            drawBackgroundImage(context, tileSize);
            drawMapPortion(context, tileSize, halfEven(bounds.minX / tileSize)
                .plus(0.1).integer,
                halfEven(bounds.minY / tileSize).plus(0.1).integer,
                smallest(halfEven(bounds.maxX / tileSize).plus(1.1).integer,
                    mapDimensions.columns),
                smallest(halfEven(bounds.maxY / tileSize).plus(1.1).integer,
                    mapDimensions.rows));
        } finally {
            context.dispose();
        }
    }

    shared actual void interactionPointChanged() {}
    shared actual void selectedUnitChanged(IUnit? old, IUnit? newUnit) {}

    addMouseListener(cml);
    addMouseWheelListener(dsl);

    assert (exists localActionMap = actionMap, exists localInputMap =
            getInputMap(JComponent.whenAncestorOfFocusedComponent));
    arrowListenerInitializer.setUpArrowListeners(dsl, localInputMap, localActionMap);
    localInputMap.put(KeyStroke.getKeyStroke(KeyEvent.vkT, 0), "show-terrain-menu");
    localActionMap.put("show-terrain-menu", object extends AbstractAction() {
        shared actual void actionPerformed(ActionEvent event) =>
            cml.showMenuAtSelection(as<Component>(event.source));
    });

    object mapSizeListener extends ComponentAdapter() {
        // TODO: Split the difference instead of only expanding/contracting on  'max' side
        Integer[2] constrain(Integer total, Integer visible, Integer oldMinimum) {
            if (visible >= total) {
                return [0, total - 1];
            } else if (oldMinimum + visible >= total) {
                return [total - visible - 2, total - 1];
            } else {
                return [oldMinimum, oldMinimum + visible - 1];
            }
        }
        Integer[4] concat(Integer[2] one, Integer[2] two) =>
            [one.first, one.rest.first, two.first, two.rest.first];
        shared actual void componentResized(ComponentEvent event) {
            Integer visibleColumns = outer.width / tileSize;
            Integer visibleRows = outer.height / tileSize;
            VisibleDimensions oldDimensions = mapModel.visibleDimensions;
            MapDimensions mapDimensions = mapModel.mapDimensions;
            if (visibleColumns != oldDimensions.width ||
                    visibleRows != oldDimensions.height) {
                mapModel.visibleDimensions =
                    VisibleDimensions(*concat(constrain(mapDimensions.rows, visibleRows,
                            oldDimensions.minimumRow),
                        constrain(mapDimensions.columns, visibleColumns,
                            oldDimensions.minimumColumn)));
            }
        }

        shared actual void componentShown(ComponentEvent event) =>
                componentResized(event);
    }
    addComponentListener(mapSizeListener);

    toolTipText = "";
    addMouseMotionListener(object extends MouseMotionAdapter() {
        shared actual void mouseMoved(MouseEvent event) => outer.repaint();
    });

    requestFocusEnabled = true;

    shared actual void tileSizeChanged(Integer olSize, Integer newSize) {
        ComponentEvent event = ComponentEvent(this, ComponentEvent.componentResized);
        for (listener in componentListeners) {
            listener.componentResized(event);
        }
        repaint();
    }

    shared actual void mapMetadataChanged() {}
}
