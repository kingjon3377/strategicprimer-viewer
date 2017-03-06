import model.map {
    PointFactory,
    Point
}
import java.awt.event {
    MouseWheelListener,
    MouseWheelEvent
}
import model.viewer {
    IViewerModel
}
import lovelace.util.jvm {
    platform
}
"A class for moving the cursor around the single-component map UI, including scrolling
 using a mouse wheel."
class DirectionSelectionChanger(IViewerModel model) satisfies MouseWheelListener {
    "Move the cursor up a row."
    shared void up() {
        Point old = model.selectedPoint;
        if (old.row > 0) {
            model.setSelection(PointFactory.point(old.row - 1, old.col));
        }
    }
    "Move the cursor left a column."
    shared void left() {
        Point old = model.selectedPoint;
        if (old.col > 0) {
            model.setSelection(PointFactory.point(old.row, old.col - 1));
        }
    }
    "Move the cursor down a row."
    shared void down() {
        Point old = model.selectedPoint;
        if (old.row < model.mapDimensions.rows) {
            model.setSelection(PointFactory.point(old.row + 1, old.col));
        }
    }
    "Move the cursor right a column."
    shared void right() {
        Point old = model.selectedPoint;
        if (old.col<model.mapDimensions.columns) {
            model.setSelection(PointFactory.point(old.row, old.col + 1));
        }
    }
    "Move the cursor all the way to the top."
    shared void jumpUp() {
        Point old = model.selectedPoint;
        if (old.row > 0) {
            model.setSelection(PointFactory.point(0, old.col));
        }
    }
    "Move the cursor all the way to the bottom."
    shared void jumpDown() {
        Point old = model.selectedPoint;
        if (old.row < model.mapDimensions.rows) {
            model.setSelection(PointFactory.point(model.mapDimensions.rows - 1, old.col));
        }
    }
    "Move the cursor all the way to the left."
    shared void jumpLeft() {
        Point old = model.selectedPoint;
        if (old.col > 0) {
            model.setSelection(PointFactory.point(old.row, 0));
        }
    }
    "Move the cursor all the way to the right."
    shared void jumpRight() {
        Point old = model.selectedPoint;
        if (old.col<model.mapDimensions.columns) {
            model.setSelection(PointFactory.point(old.row, model.mapDimensions.columns - 1));
        }
    }
    "Scroll."
    void scroll("Whether to scroll horizontally" Boolean horizontal,
            "Whether to scroll forward (down/right)" Boolean forward,
            "How many times to scroll" Integer count) {
        Anything() func;
        if (horizontal, forward) {
            func = right;
        } else if (horizontal) {
            func = left;
        } else if (forward) {
            func = down;
        } else {
            func = up;
        }
        for (i in 0..count) {
            func();
        }
    }
    "Scroll when the user scrolls the mouse wheel."
    shared actual void mouseWheelMoved(MouseWheelEvent event) {
        if (platform.hotKeyPressed(event)) {
            // Zoom if Command-scroll/Control-scroll
            Integer count = event.wheelRotation;
            if (count < 0) {
                for (i in 0..count) {
                    model.zoomIn();
                }
            } else {
                for (i in 0..count) {
                    model.zoomOut();
                }
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
            // Otherwise, no relevant modifiers being pressed, scroll vertically
            // TODO: should Control have meaning on Mac?
            Integer count = event.wheelRotation;
            if (count < 0) {
                scroll(false, false, 0 - count);
            } else {
                scroll(false, true, count);
            }
        }
    }
}
