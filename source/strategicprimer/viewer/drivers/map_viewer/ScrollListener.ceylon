import javax.swing.event {
    ChangeEvent,
    ChangeListener
}
import java.awt {
    BorderLayout,
    Adjustable
}
import strategicprimer.model.common.map {
    MapDimensions,
    Point
}
import lovelace.util.common {
    defer
}
import lovelace.util.jvm {
    BorderedPanel
}
import strategicprimer.drivers.common {
    MapChangeListener,
    SelectionChangeListener
}
import javax.swing {
    JScrollBar,
    JComponent,
    BoundedRangeModel,
    InputVerifier
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

"A class, formerly static within [[ScrollListener]], to verify that scroll
 inputs are within the valid range."
class ScrollInputVerifier extends InputVerifier {
    Integer() mapDimension;
    String dimension;
    shared new horizontal(MapDimensions() mapDimsSource) extends InputVerifier() {
        mapDimension = compose(MapDimensions.columns, mapDimsSource);
        dimension = "horizontal";
    }

    shared new vertical(MapDimensions() mapDimsSource) extends InputVerifier() {
        mapDimension = compose(MapDimensions.rows, mapDimsSource);
        dimension = "vertical";
    }

    "A scrollbar is valid if its value is between 0 and the size of the map."
    shared actual Boolean verify(JComponent input) {
        if (is JScrollBar input) {
            if ((0:(mapDimension())).contains(input.\ivalue)) {
                log.trace("``input.\ivalue`` is a valid ``dimension`` coordinate");
                return true;
            } else {
                log.trace("``input.\ivalue`` is not a valid ``dimension`` coordinate");
                return false;
            }
        } else {
            log.trace("ScrollInputVerifier called on non-scroll-bar input");
            return false;
        }
    }
}

class ScrollAdjustmentListener(IViewerModel model, BoundedRangeModel horizontalBarModel,
        BoundedRangeModel verticalBarModel) satisfies ChangeListener&GraphicalParamsListener {
    shared variable VisibleDimensions visibleDimensions = model.visibleDimensions;
    variable Boolean mutex = true;
    shared actual void stateChanged(ChangeEvent event) {
        log.trace("ScrollAdjustment listener starting to respond to scroll-bar change");
        if (mutex) {
            mutex = false;
            VisibleDimensions oldDimensions = model.visibleDimensions;
            Integer newColumn = horizontalBarModel.\ivalue;
            Integer newRow = verticalBarModel.\ivalue;
            Integer newMinColumn;
            Integer newMaxColumn;
            log.trace("Columns were from ``oldDimensions.minimumColumn`` to ``oldDimensions.maximumColumn``; new column is ``newColumn``");
            if (newColumn.negative) {
                log.trace("'New column' is negative, skipping horizontal scrolling.");
                newMaxColumn = oldDimensions.maximumColumn;
                newMinColumn = oldDimensions.minimumColumn;
            } else if (newColumn >= model.mapDimensions.columns) {
                log.trace("'New column' is above max column, skipping horizontal scrolling.");
                newMaxColumn = oldDimensions.maximumColumn;
                newMinColumn = oldDimensions.minimumColumn;
            } else if (oldDimensions.minimumColumn > newColumn) {
                log.trace("User scrolled left");
                newMinColumn = newColumn;
                newMaxColumn = newColumn + visibleDimensions.width - 1;
            } else if (oldDimensions.maximumColumn < newColumn) {
                log.trace("User scrolled right");
                newMaxColumn = newColumn;
                newMinColumn = newColumn - visibleDimensions.width + 1;
            } else {
                log.trace("User didn't scroll horizontally");
                newMaxColumn = oldDimensions.maximumColumn;
                newMinColumn = oldDimensions.minimumColumn;
            }
            Integer newMinRow;
            Integer newMaxRow;
            log.trace("Rows were from ``oldDimensions.minimumRow`` to ``oldDimensions.maximumRow``; new column is ``newRow``");
            if (newRow.negative) {
                log.trace("'New row' is negative, skipping vertical scrolling.");
                newMaxRow = oldDimensions.maximumRow;
                newMinRow = oldDimensions.minimumRow;
            } else if (newRow >= model.mapDimensions.rows) {
                log.trace("'New row' is above max row, skipping vertical scrolling.");
                newMaxRow = oldDimensions.maximumRow;
                newMinRow = oldDimensions.minimumRow;
            } else if (oldDimensions.minimumRow > newRow) {
                log.trace("User scrolled up");
                newMinRow = newRow;
                newMaxRow = newRow + visibleDimensions.height - 1;
            } else if (oldDimensions.maximumRow < newRow) {
                log.trace("user scrolled down");
                newMaxRow = newRow;
                newMinRow = newRow - visibleDimensions.height + 1;
            } else {
                log.trace("User didn't scroll vertically");
                newMaxRow = oldDimensions.maximumRow;
                newMinRow = oldDimensions.minimumRow;
            }
            VisibleDimensions newDimensions = VisibleDimensions(newMinRow,
                newMaxRow, newMinColumn, newMaxColumn);
            if (oldDimensions != newDimensions) {
                log.trace("Replacing old viewport dimensions with new one.");
                model.visibleDimensions = newDimensions;
            } else {
                log.trace("Viewport dimensions did not change");
            }
            mutex = true;
        } else {
            log.trace("Detected reentrant handling: skipping");
        }
    }
    "Handle a change in visible dimensions."
    shared actual void dimensionsChanged(VisibleDimensions oldDimensions,
            VisibleDimensions newDimensions) => visibleDimensions = newDimensions;

    "Ignored; other listeners will adjust the dimensions, causing [[dimensionsChanged]] to
     be called."
    shared actual void tileSizeChanged(Integer oldSize, Integer newSize) { }
}

"A class to change the visible area of the map based on the user's use of the scrollbars."
class ScrollListener satisfies MapChangeListener&SelectionChangeListener&
        GraphicalParamsListener {
    static Integer constrainToRange(Integer val, Integer min, Integer max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    IViewerModel model;
    BoundedRangeModel horizontalBarModel;
    BoundedRangeModel verticalBarModel;
    variable MapDimensions mapDimensions;
    variable VisibleDimensions visibleDimensions;
    // Can't take scroll-bar models instead of scroll bars because we have to set up input verifiers as well.
    shared new (IViewerModel mapModel, JScrollBar horizontal, JScrollBar vertical) {
        model = mapModel;
        visibleDimensions = mapModel.visibleDimensions;
        mapDimensions = mapModel.mapDimensions;
        Point cursor = mapModel.cursor;
        horizontalBarModel = horizontal.model;
        horizontalBarModel.setRangeProperties(constrainToRange(cursor.column,
                0, mapDimensions.columns - 1),
            smallest(mapDimensions.columns, visibleDimensions.width), 0,
            mapDimensions.columns, false);
        horizontal.inputVerifier = ScrollInputVerifier.horizontal(
            defer(IViewerModel.mapDimensions, [mapModel]));
        verticalBarModel = vertical.model;
        verticalBarModel.setRangeProperties(constrainToRange(cursor.row, 0,
                mapDimensions.rows - 1),
            smallest(mapDimensions.rows, visibleDimensions.height), 0,
            mapDimensions.rows, false);
        vertical.inputVerifier = ScrollInputVerifier.vertical(
            defer(IViewerModel.mapDimensions, [mapModel]));

        value adjustmentListener = ScrollAdjustmentListener(model, horizontalBarModel, verticalBarModel);
        mapModel.addGraphicalParamsListener(adjustmentListener);

        horizontalBarModel.addChangeListener(adjustmentListener);
        verticalBarModel.addChangeListener(adjustmentListener);
    }

    "Alternate constructor that adds (new) scroll-bars to an existing component. This only
     works if that component is laid out using a [[BorderLayout]] and doesn't already have
      members at page-end and line-end."
    new addScrollBars(IViewerModel mapModel, BorderedPanel component, JScrollBar horizontalBar,
                JScrollBar verticalBar)
            extends ScrollListener(mapModel, horizontalBar, verticalBar) {
        "We don't want to replace existing components with scrollbars"
        assert (!component.pageEnd exists, !component.lineEnd exists);
        component.pageEnd = horizontalBar;
        component.lineEnd = verticalBar;
    }

    shared new createScrollBars(IViewerModel mapModel, BorderedPanel component)
            extends addScrollBars(mapModel, component, JScrollBar(Adjustable.horizontal),
                JScrollBar(Adjustable.vertical)) {}

    Integer countChanges(BoundedRangeModel model, Integer val, Integer extent,
            Integer minimum, Integer maximum) {
        variable Integer retval = 0;
        if (model.\ivalue != val) {
            retval++;
        }
        if (model.minimum != minimum) {
            retval++;
        }
        if (model.extent != extent) {
            retval++;
        }
        if (model.maximum != maximum) {
            retval++;
        }
        return retval;
    }

    void setRangeProperties(BoundedRangeModel model, Integer val, Integer extent,
            Integer minimum, Integer maximum) {
        Integer differences = countChanges(model, val, minimum, extent, maximum);
        if (!differences.positive) {
            return;
        } else if (differences < 3) {
            model.valueIsAdjusting = true;
            if (model.\ivalue != val) {
                model.\ivalue = val;
            }
            if (model.minimum != minimum) {
                model.minimum = minimum;
            }
            if (model.maximum != maximum) {
                model.maximum = maximum;
            }
            if (model.extent != extent) {
                model.extent = extent;
            }
            model.valueIsAdjusting = false;
        } else {
            model.setRangeProperties(val, extent, minimum, maximum, false);
        }
    }

    variable Boolean mutex = true;
    "Handle a change in visible dimensions."
    shared actual void dimensionsChanged(VisibleDimensions oldDimensions,
            VisibleDimensions newDimensions) {
        if (mutex) {
            mutex = false;
            visibleDimensions = newDimensions;
            setRangeProperties(horizontalBarModel, largest(model.cursor.column, 0),
                smallest(newDimensions.width, mapDimensions.columns),
                0, mapDimensions.columns);
            setRangeProperties(verticalBarModel, largest(model.cursor.row, 0),
                smallest(newDimensions.height, mapDimensions.rows), 0,
                mapDimensions.rows);
            mutex = true;
        }
    }

    "Ignored; other listeners will adjust the dimensions, causing [[dimensionsChanged]] to
     be called."
    shared actual void tileSizeChanged(Integer oldSize, Integer newSize) { }

    "Handle a change to the cursor location."
    shared actual void cursorPointChanged(Point? previous, Point newCursor) {
        VisibleDimensions temp = model.visibleDimensions;
        if (!temp.columns.contains(newCursor.column),
                horizontalBarModel.\ivalue != largest(newCursor.column, 0)) {
            horizontalBarModel.\ivalue = largest(newCursor.column, 0);
        }
        if (!temp.rows.contains(newCursor.row),
                verticalBarModel.\ivalue != largest(newCursor.row, 0)) {
            verticalBarModel.\ivalue = largest(newCursor.row, 0);
        }
    }

    "Scrolling deals only with the cursor location, not with the selection."
    shared actual void selectedPointChanged(Point? previousSelection, Point newSelection) {}

    "Handle notification that a new map was loaded."
    shared actual void mapChanged() {
        mapDimensions = model.mapDimensions;
        visibleDimensions = model.visibleDimensions;
        horizontalBarModel.setRangeProperties(0,
            smallest(visibleDimensions.width, mapDimensions.columns), 0,
            mapDimensions.columns, false);
        verticalBarModel.setRangeProperties(0,
            smallest(visibleDimensions.height, mapDimensions.rows), 0,
            mapDimensions.rows, false);
    }

    shared actual void mapMetadataChanged() {}
    shared actual void selectedUnitChanged(IUnit? oldSelection, IUnit? newSelection) {}
    shared actual void interactionPointChanged() {}
}
