import javax.swing.event {
    ChangeEvent
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

class ScrollAdjustmentListener(IViewerModel model) { // FIXME: Listen to some events so we can reset on map or selected point change
    // TODO: Do we really need to track these, or can we just rely on [[model.cursor]]?
    variable Integer? oldRow = null;
    variable Integer? oldColumn = null;

    // TODO: Should probably track horizonal and vertical scrolling separately
    shared variable Boolean adjusting = false;

    shared void horizontalScroll(ChangeEvent event) {
        assert (is BoundedRangeModel source = event.source);
        if (adjusting) {
            log.trace("Waiting for scrollbar to stop adjusting before handling horizontal scroll");
            return;
        }
        log.trace("Starting to respond to horizontal scroll");
        VisibleDimensions oldDimensions = model.visibleDimensions;
        Integer newValue = source.\ivalue;
        VisibleDimensions newDimensions;
        if (newValue.negative) {
            log.warn("Tried to scroll to negative column, skipping ...");
            return;
        } else if (newValue > (model.mapDimensions.columns + oldDimensions.columns.size)) {
            log.warn("Tried to scroll too far to the right, skipping ...");
            return;
        } else if (exists oldValue = oldColumn) {
            if (oldValue == newValue) {
                log.trace("Horizontal scroll to same value, possibly reentrant. Skipping ...");
                return;
            }
            Integer offset = newValue - oldValue;
            log.trace("User scrolled horizontally by ``offset`` tiles.");
            oldColumn = newValue;
            newDimensions = VisibleDimensions(oldDimensions.minimumRow,
                oldDimensions.maximumRow, oldDimensions.minimumColumn + offset,
                oldDimensions.maximumColumn + offset);
        } else {
            Integer newMinColumn;
            Integer newMaxColumn;
            if (oldDimensions.minimumColumn > newValue) {
                log.trace("User scrolled left");
                newMinColumn = newValue;
                newMaxColumn = newValue + oldDimensions.width - 1;
            } else if (oldDimensions.maximumColumn < newValue) {
                log.trace("User scrolled right");
                newMaxColumn = newValue;
                newMinColumn = newValue - oldDimensions.width + 1;
            } else {
                log.trace("No cached horizontal coordinate and new value within previous visible area, skipping ...");
                oldColumn = newValue;
                return;
            }
            oldColumn = newValue;
            newDimensions = VisibleDimensions(oldDimensions.minimumRow,
                oldDimensions.maximumRow, newMinColumn, newMaxColumn);
        }
        Boolean oldAdjusting = adjusting;
        adjusting = true;
        model.cursor = Point(model.cursor.row, newValue);
        model.visibleDimensions = newDimensions;
        adjusting = oldAdjusting;
    }

    shared void verticalScroll(ChangeEvent event) {
        assert (is BoundedRangeModel source = event.source);
        if (adjusting) {
            // TODO: We'd like to do *some* handling, in case the user is dragging the tongue. Mutex flag again?
            log.trace("Waiting for scrollbar to stop adjusting before handling vertical scroll");
            return;
        }
        log.trace("Starting to respond to vertical scroll");
        Integer newValue = source.\ivalue;
        VisibleDimensions oldDimensions = model.visibleDimensions;
        VisibleDimensions newDimensions;
        if (newValue.negative) {
            log.warn("Tried to scroll to negative row, skipping ...");
            return;
        } else if (newValue > (model.mapDimensions.rows + model.visibleDimensions.rows.size)) {
            log.warn("Tried to scroll too far to the right, skipping ...");
            return;
        } else if (exists oldValue = oldRow) {
            if (oldValue == newValue) {
                log.trace("Vertical scroll to same value, possibly reentrant. Skipping ...");
                return;
            }
            Integer offset = newValue - oldValue;
            log.trace("User scrolled vertically by ``offset`` tiles.");
            oldRow = newValue;
            newDimensions = VisibleDimensions(oldDimensions.minimumRow + offset,
                oldDimensions.maximumRow + offset, oldDimensions.minimumColumn,
                oldDimensions.maximumColumn);
        } else {
            Integer newMinRow;
            Integer newMaxRow;
            if (oldDimensions.minimumRow > newValue) {
                log.trace("User scrolled down");
                newMinRow = newValue;
                newMaxRow = newValue + model.visibleDimensions.height - 1;
            } else if (oldDimensions.maximumRow < newValue) {
                log.trace("User scrolled up");
                newMaxRow = newValue;
                newMinRow = newValue - model.visibleDimensions.height + 1;
            } else {
                log.trace("No cached vertical coordinate and new value within previous visible area, skipping ...");
                oldRow = newValue;
                return;
            }
            oldRow = newValue;
            newDimensions = VisibleDimensions(newMinRow, newMaxRow,
                oldDimensions.minimumColumn, oldDimensions.maximumColumn);
        }
        Boolean oldAdjusting = adjusting;
        adjusting = true;
        model.cursor = Point(newValue, model.cursor.column);
        model.visibleDimensions = newDimensions;
        adjusting = oldAdjusting;
    }
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

        value adjustmentListener = ScrollAdjustmentListener(model);

        horizontalBarModel.addChangeListener(adjustmentListener.horizontalScroll);
        verticalBarModel.addChangeListener(adjustmentListener.verticalScroll);
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
