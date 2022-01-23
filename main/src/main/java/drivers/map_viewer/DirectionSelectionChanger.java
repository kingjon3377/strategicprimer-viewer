package drivers.map_viewer;

import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;

import lovelace.util.Platform;

import common.map.Point;

/**
 * A class for moving the cursor around the single-component map UI, including
 * scrolling using a mouse wheel. When methods (e.g. [[up]]) are called with
 * the default [[true]], they move the selection; when they are called with
 * [[false]], they scroll the window but do not move the selection.
 *
 * TODO: Add withRow() and withColumn() methods to Point to condense the below slightly?
 */
/* package */ class DirectionSelectionChanger implements MouseWheelListener {
	public DirectionSelectionChanger(IViewerModel model) {
		this.model = model;
	}
	
	private final IViewerModel model;

	/**
	 * Get the base point for the current mode (selection vs cursor).
	 *
	 * TODO: boolean to enum
	 */
	private Point get(boolean selection) {
		return (selection) ? model.getSelection() : model.getCursor();
	}

	/**
	 * Assign the given point correctly for the given mode.
	 */
	private void set(boolean selection, Point point) {
		if (selection) {
			model.setSelection(point);
		} else {
			model.setCursor(point);
		}
	}

	/**
	 * Move the cursor up a row.
	 */
	public void up() {
		up(true);
	}

	/**
	 * Move the cursor up a row.
	 */
	public void up(boolean selection) {
		Point old = get(selection);
		if (old.getRow() > 0) {
			set(selection, new Point(old.getRow() - 1, old.getColumn()));
		}
	}

	/**
	 * Move the cursor left a column.
	 */
	public void left() {
		left(true);
	}

	/**
	 * Move the cursor left a column.
	 */
	public void left(boolean selection) {
		Point old = get(selection);
		if (old.getColumn() > 0) {
			set(selection, new Point(old.getRow(), old.getColumn() - 1));
		}
	}

	/**
	 * Move the cursor down a row.
	 */
	public void down() {
		down(true);
	}

	/**
	 * Move the cursor down a row.
	 */
	public void down(boolean selection) {
		Point old = get(selection);
		if (old.getRow() < model.getMapDimensions().getRows() - 1) {
			set(selection, new Point(old.getRow() + 1, old.getColumn()));
		}
	}

	/**
	 * Move the cursor right a column.
	 */
	public void right() {
		right(true);
	}

	/**
	 * Move the cursor right a column.
	 */
	public void right(boolean selection) {
		Point old = get(selection);
		if (old.getColumn()<model.getMapDimensions().getColumns() - 1) {
			set(selection, new Point(old.getRow(), old.getColumn() + 1));
		}
	}

	/**
	 * Move the cursor all the way to the top.
	 */
	public void jumpUp() {
		Point old = model.getSelection();
		if (old.getRow() > 0) {
			model.setSelection(new Point(0, old.getColumn()));
		}
	}

	/**
	 * Move the cursor all the way to the bottom.
	 */
	public void jumpDown() {
		Point old = model.getSelection();
		if (old.getRow() < model.getMapDimensions().getRows()) {
			model.setSelection(new Point(model.getMapDimensions().getRows() - 1,
				old.getColumn()));
		}
	}

	/**
	 * Move the cursor all the way to the left.
	 */
	public void jumpLeft() {
		Point old = model.getSelection();
		if (old.getColumn() > 0) {
			model.setSelection(new Point(old.getRow(), 0));
		}
	}

	/**
	 * Move the cursor all the way to the right.
	 */
	public void jumpRight() {
		Point old = model.getSelection();
		if (old.getColumn()<model.getMapDimensions().getColumns()) {
			model.setSelection(new Point(old.getRow(),
				model.getMapDimensions().getColumns() - 1));
		}
	}

	@FunctionalInterface
	private static interface BooleanConsumer {
		void accept(boolean bool);
	}

	// FIXME: A multi-tile scroll should only set the selection (or cursor) *once*, not for each tile it passes through ... but we still need to make sure we don't go beyond bounds.
	/**
	 * Scroll.
	 *
	 * @param horizontal Whether to scroll horizontally
	 * @param forward Whether to scroll forward (down/right)
	 * @param count How many times (tiles) to scroll
	 *
	 * TODO: Boolean parameters should be enums instead
	 */
	private void scroll(boolean horizontal, boolean forward, int count) {
		BooleanConsumer func;
		if (horizontal && forward) {
			func = this::right;
		} else if (horizontal) {
			func = this::left;
		} else if (forward) {
			func = this::down;
		} else {
			func = this::up;
		}
		for (int i = 0; i < count; i++) {
			func.accept(false);
		}
	}

	/**
	 * Scroll when the user scrolls the mouse wheel: zooming when Command
	 * (on Mac) or Control (otherwise) pressed, horizontally when Shift
	 * pressed, and vertically otherwise.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		if (Platform.isHotKeyPressed(event)) {
			// Zoom if Command-scroll/Control-scroll
			final int count = event.getWheelRotation(); // TODO: pull count variable to top of method
			if (count < 0) {
				for (int i = 0; i < -count; i++) {
					model.zoomIn();
				}
			} else {
				for (int i = 0; i < count; i++) {
					model.zoomOut();
				}
			}
		} else if (event.isShiftDown()) {
			// Scroll sideways on Shift-scroll
			int count = event.getWheelRotation();
			if (count < 0) {
				scroll(true, false, 0 - count);
			} else {
				scroll(true, true, count);
			}
		} else {
			// Otherwise, no relevant modifiers being pressed, scroll vertically.
			// Control is ignored on Mac because it is rarely used as a modifier, and
			// Control-clicking is the same as right-clicking.
			int count = event.getWheelRotation();
			if (count < 0) {
				scroll(false, false, 0 - count);
			} else {
				scroll(false, true, count);
			}
		}
	}
}
