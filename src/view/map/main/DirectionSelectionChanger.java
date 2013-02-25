package view.map.main;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import model.map.PointFactory;
import model.viewer.IViewerModel;
import model.viewer.TileViewSize;

/**
 * A class for moving the cursor around the single-component map UI, including
 * scrolling using the mouse wheel.
 *
 * @author Jonathan Lovelace
 *
 */
public class DirectionSelectionChanger implements MouseWheelListener {
	/**
	 * The map model we'll be referring to.
	 */
	private final IViewerModel model;

	/**
	 * Constructor.
	 *
	 * @param mapModel the map model we're to use
	 * @param tileSize the object encapsulating the size of a visible tile, to use to zoom.
	 */
	public DirectionSelectionChanger(final IViewerModel mapModel, final TileViewSize tileSize) {
		model = mapModel;
		tvs = tileSize;
	}
	/**
	 * The object encapsulating the size of a visible tile.
	 */
	private final TileViewSize tvs;
	/**
	 * Move the cursor up.
	 */
	public void up() { // NOPMD
		if (model.getSelectedTile().getLocation().row > 0) {
			model.setSelection(PointFactory.point(model.getSelectedTile()
					.getLocation().row - 1, model.getSelectedTile()
					.getLocation().col));
		}
	}

	/**
	 * Move the cursor left.
	 */
	public void left() {
		if (model.getSelectedTile().getLocation().col > 0) {
			model.setSelection(PointFactory.point(model.getSelectedTile()
					.getLocation().row, model.getSelectedTile().getLocation()
					.col - 1));
		}
	}

	/**
	 * Move the cursor down.
	 */
	public void down() {
		if (model.getSelectedTile().getLocation().row < model.getMapDimensions().rows - 1) {
			model.setSelection(PointFactory.point(model.getSelectedTile()
					.getLocation().row + 1, model.getSelectedTile()
					.getLocation().col));
		}
	}

	/**
	 * Move the cursor right.
	 */
	public void right() {
		if (model.getSelectedTile().getLocation().col < model.getMapDimensions().cols - 1) {
			model.setSelection(PointFactory.point(model.getSelectedTile()
					.getLocation().row, model.getSelectedTile().getLocation()
					.col + 1));
		}
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "DirectionSelectionChangerImpl";
	}
	/**
	 * Scroll.
	 * @param horiz whether to scroll horizontally.
	 * @param forward whether to scroll forward (down or right)
	 * @param count how many times to scroll
	 */
	private void scroll(final boolean horiz, final boolean forward, final int count) {
		if (horiz && forward) {
			for (int i = 0; i < count; i++) {
				right();
			}
		} else if (horiz) {
			for (int i = 0; i < count; i++) {
				left();
			}
		} else if (forward) {
			for (int i = 0; i < count; i++) {
				down();
			}
		} else {
			for (int i = 0; i < count; i++) {
				up();
			}
		}
	}
	/**
	 * Scroll when the user scrolls the mouse wheel.
	 * @param evt the event to handle
	 */
	@Override
	public void mouseWheelMoved(final MouseWheelEvent evt) {
		if (evt.isControlDown() || evt.isMetaDown()) {
			final int count = evt.getWheelRotation();
			if (count < 0) {
				// Negative is away from the user, forward, "in"
				tvs.increase(0 - count);
			} else {
				tvs.decrease(count);
			}
		} else if (evt.isShiftDown()) {
			// Scroll sideways on Shift+scroll
			final int count = evt.getWheelRotation();
			if (count < 0) {
				scroll(true, false, 0 - count);
			} else {
				scroll(true, true, count);
			}
		} else {
			// No relevant modifiers, scroll vertically.
			final int count = evt.getWheelRotation();
			if (count < 0) {
				scroll(false, false, 0 - count);
			} else {
				scroll(false, true, count);
			}
		}
	}
}
