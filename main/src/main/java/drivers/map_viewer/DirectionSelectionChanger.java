package drivers.map_viewer;

import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.util.function.Consumer;

import lovelace.util.Platform;

import legacy.map.Point;

/**
 * A class for moving the cursor around the single-component map UI, including
 * scrolling using a mouse wheel. When methods (e.g. {@link #up} are called with
 * the default true, they move the selection; when they are called with
 * false, they scroll the window but do not move the selection.
 *
 * TODO: Add withRow() and withColumn() methods to Point to condense the below slightly?
 */
/* package */ final class DirectionSelectionChanger implements MouseWheelListener {
	public DirectionSelectionChanger(final IViewerModel model) {
		this.model = model;
	}

	private final IViewerModel model;

	public enum SelectionMode {
		Selection, Cursor
	}
	/**
	 * Get the base point for the current mode (selection vs cursor).
	 *
	 * TODO: boolean to enum
	 */
	private Point get(final SelectionMode mode) {
		switch (mode) {
			case Selection -> {
				return model.getSelection();
			}
			case Cursor -> {
				return model.getCursor();
			}
			default -> throw new IllegalStateException("Exhaustive switch wasn't");
		}
	}

	/**
	 * Assign the given point correctly for the given mode.
	 */
	private void set(final SelectionMode mode, final Point point) {
		switch (mode) {
			case Selection -> model.setSelection(point);
			case Cursor -> model.setCursor(point);
			default -> throw new IllegalStateException("Exhaustive switch wasn't");
		}
	}

	/**
	 * Move the cursor up a row.
	 */
	public void up() {
		up(SelectionMode.Selection);
	}

	/**
	 * Move the cursor up a row.
	 */
	public void up(final SelectionMode mode) {
		final Point old = get(mode);
		if (old.row() > 0) {
			set(mode, new Point(old.row() - 1, old.column()));
		}
	}

	/**
	 * Move the cursor left a column.
	 */
	public void left() {
		left(SelectionMode.Selection);
	}

	/**
	 * Move the cursor left a column.
	 */
	public void left(final SelectionMode mode) {
		final Point old = get(mode);
		if (old.column() > 0) {
			set(mode, new Point(old.row(), old.column() - 1));
		}
	}

	/**
	 * Move the cursor down a row.
	 */
	public void down() {
		down(SelectionMode.Selection);
	}

	/**
	 * Move the cursor down a row.
	 */
	public void down(final SelectionMode mode) {
		final Point old = get(mode);
		if (old.row() < model.getMapDimensions().rows() - 1) {
			set(mode, new Point(old.row() + 1, old.column()));
		}
	}

	/**
	 * Move the cursor right a column.
	 */
	public void right() {
		right(SelectionMode.Selection);
	}

	/**
	 * Move the cursor right a column.
	 */
	public void right(final SelectionMode mode) {
		final Point old = get(mode);
		if (old.column() < model.getMapDimensions().columns() - 1) {
			set(mode, new Point(old.row(), old.column() + 1));
		}
	}

	/**
	 * Move the cursor all the way to the top.
	 */
	public void jumpUp() {
		final Point old = model.getSelection();
		if (old.row() > 0) {
			model.setSelection(new Point(0, old.column()));
		}
	}

	/**
	 * Move the cursor all the way to the bottom.
	 */
	public void jumpDown() {
		final Point old = model.getSelection();
		if (old.row() < model.getMapDimensions().rows()) {
			model.setSelection(new Point(model.getMapDimensions().rows() - 1,
					old.column()));
		}
	}

	/**
	 * Move the cursor all the way to the left.
	 */
	public void jumpLeft() {
		final Point old = model.getSelection();
		if (old.column() > 0) {
			model.setSelection(new Point(old.row(), 0));
		}
	}

	/**
	 * Move the cursor all the way to the right.
	 */
	public void jumpRight() {
		final Point old = model.getSelection();
		if (old.column() < model.getMapDimensions().columns()) {
			model.setSelection(new Point(old.row(),
					model.getMapDimensions().columns() - 1));
		}
	}

	@FunctionalInterface
	private interface BooleanConsumer {
		void accept(boolean bool);
	}

	// FIXME: A multi-tile scroll should only set the selection (or cursor) *once*, not for each tile it passes
	//  through ... but we still need to make sure we don't go beyond bounds.

	/**
	 * Scroll.
	 *
	 * @param horizontal Whether to scroll horizontally
	 * @param forward    Whether to scroll forward (down/right)
	 * @param count      How many times (tiles) to scroll
	 *
	 * TODO: Boolean parameters should be enums instead
	 */
	private void scroll(final boolean horizontal, final boolean forward, final int count) {
		final Consumer<SelectionMode> func;
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
			func.accept(SelectionMode.Cursor);
		}
	}

	/**
	 * Scroll when the user scrolls the mouse wheel: zooming when Command
	 * (on Mac) or Control (otherwise) pressed, horizontally when Shift
	 * pressed, and vertically otherwise.
	 */
	@Override
	public void mouseWheelMoved(final MouseWheelEvent event) {
		final int count = event.getWheelRotation();
		if (Platform.isHotKeyPressed(event)) {
			// Zoom if Command-scroll/Control-scroll
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
			if (count < 0) {
				scroll(true, false, -count);
			} else {
				scroll(true, true, count);
			}
		} else {
			// Otherwise, no relevant modifiers being pressed, scroll vertically.
			// Control is ignored on Mac because it is rarely used as a modifier, and
			// Control-clicking is the same as right-clicking.
			if (count < 0) {
				scroll(false, false, -count);
			} else {
				scroll(false, true, count);
			}
		}
	}
}
