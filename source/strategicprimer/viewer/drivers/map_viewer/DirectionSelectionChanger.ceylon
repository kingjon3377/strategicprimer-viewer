import java.awt.event {
    MouseWheelListener,
    MouseWheelEvent
}

import lovelace.util.jvm {
    platform
}

import strategicprimer.model.common.map {
    Point
}

import lovelace.util.common {
    silentListener,
    todo
}

"A class for moving the cursor around the single-component map UI, including scrolling
 using a mouse wheel. When methods (e.g. [[up]]) are called with the default [[true]], they
 move the selection; when they are called with [[false]], they scroll the window but do not
 move the selection."
todo("Add withRow() and withColumn() methods to Point to condense the below slightly")
class DirectionSelectionChanger(IViewerModel model) satisfies MouseWheelListener {
    "Get the base point for the current mode (selection vs cursor)."
    Point get(Boolean selection) => (selection) then model.selection else model.cursor;

    "Assign the given point correctly for the given mode."
    void set(Boolean selection, Point point) {
        if (selection) {
            model.selection = point;
        } else {
            model.cursor = point;
        }
    }

    "Move the cursor up a row."
    shared void up(Boolean selection = true) {
        Point old = get(selection);
        if (old.row > 0) {
            set(selection, Point(old.row - 1, old.column));
        }
    }

    "Move the cursor left a column."
    shared void left(Boolean selection = true) {
        Point old = get(selection);
        if (old.column > 0) {
            set(selection, Point(old.row, old.column - 1));
        }
    }

    "Move the cursor down a row."
    shared void down(Boolean selection = true) {
        Point old = get(selection);
        if (old.row < model.mapDimensions.rows - 1) {
            set(selection, Point(old.row + 1, old.column));
        }
    }

    "Move the cursor right a column."
    shared void right(Boolean selection = true) {
        Point old = get(selection);
        if (old.column<model.mapDimensions.columns - 1) {
            set(selection, Point(old.row, old.column + 1));
        }
    }

    "Move the cursor all the way to the top."
    shared void jumpUp() {
        Point old = model.selection;
        if (old.row > 0) {
            model.selection = Point(0, old.column);
        }
    }

    "Move the cursor all the way to the bottom."
    shared void jumpDown() {
        Point old = model.selection;
        if (old.row < model.mapDimensions.rows) {
            model.selection = Point(model.mapDimensions.rows - 1, old.column);
        }
    }

    "Move the cursor all the way to the left."
    shared void jumpLeft() {
        Point old = model.selection;
        if (old.column > 0) {
            model.selection = Point(old.row, 0);
        }
    }

    "Move the cursor all the way to the right."
    shared void jumpRight() {
        Point old = model.selection;
        if (old.column<model.mapDimensions.columns) {
            model.selection = Point(old.row, model.mapDimensions.columns - 1);
        }
    }

    // FIXME: A multi-tile scroll should only set the selection (or cursor) *once*, not for each tile it passes through ... but we still need to make sure we don't go beyond bounds.
    "Scroll."
    void scroll("Whether to scroll horizontally" Boolean horizontal,
            "Whether to scroll forward (down/right)" Boolean forward,
            "How many times to scroll" Integer count) {
        Anything(Boolean) func;
        if (horizontal, forward) {
            func = right;
        } else if (horizontal) {
            func = left;
        } else if (forward) {
            func = down;
        } else {
            func = up;
        }
        (0:count).each((num) => func(false));
    }

    "Scroll when the user scrolls the mouse wheel: zooming when Command (on
     Mac) or Control (otherwise) pressed, horizontally when Shift pressed, and
     vertically  otherwise."
    shared actual void mouseWheelMoved(MouseWheelEvent event) {
        if (platform.hotKeyPressed(event)) {
            // Zoom if Command-scroll/Control-scroll
            Integer count = event.wheelRotation;
            if (count < 0) {
                (0:(count.magnitude)).each(silentListener(model.zoomIn));
            } else {
                (0:count).each(silentListener(model.zoomOut));
            }
        } else if (event.shiftDown) {
            // Scroll sideways on Shift-scroll
            Integer count = event.wheelRotation;
            if (count < 0) {
                scroll(true, false, 0 - count);
            } else {
                scroll(true, true, count);
            }
        } else {
            // Otherwise, no relevant modifiers being pressed, scroll vertically.
            // Control is ignored on Mac because it is rarely used as a modifier, and
            // Control-clicking is the same as right-clicking.
            Integer count = event.wheelRotation;
            if (count < 0) {
                scroll(false, false, 0 - count);
            } else {
                scroll(false, true, count);
            }
        }
    }
}
