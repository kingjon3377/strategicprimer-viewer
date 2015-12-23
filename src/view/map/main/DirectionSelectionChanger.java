package view.map.main;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import model.map.PointFactory;
import model.viewer.IViewerModel;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class for moving the cursor around the single-component map UI, including scrolling
 * using the mouse wheel.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class DirectionSelectionChanger implements MouseWheelListener {
	/**
	 * The map model we'll be referring to.
	 */
	private final IViewerModel model;

	/**
	 * Constructor.
	 *
	 * @param mapModel the map model we're to use
	 */
	public DirectionSelectionChanger(final IViewerModel mapModel) {
		model = mapModel;
	}

	/**
	 * Move the cursor up.
	 */
	public void up() { // NOPMD
		if (model.getSelectedPoint().row > 0) {
			model.setSelection(PointFactory.point(
					model.getSelectedPoint().row - 1,
					model.getSelectedPoint().col));
		}
	}

	/**
	 * Move the cursor left.
	 */
	public void left() {
		if (model.getSelectedPoint().col > 0) {
			model.setSelection(PointFactory.point(model.getSelectedPoint().row,
					model.getSelectedPoint().col - 1));
		}
	}

	/**
	 * Move the cursor down.
	 */
	public void down() {
		if (model.getSelectedPoint().row < (model.getMapDimensions().rows - 1)) {
			model.setSelection(PointFactory.point(
					model.getSelectedPoint().row + 1,
					model.getSelectedPoint().col));
		}
	}

	/**
	 * Move the cursor right.
	 */
	public void right() {
		if (model.getSelectedPoint().col < (model.getMapDimensions().cols - 1)) {
			model.setSelection(PointFactory.point(model.getSelectedPoint().row,
					model.getSelectedPoint().col + 1));
		}
	}

	/**
	 * Move the cursor all the way to the top.
	 */
	public void jumpUp() {
		if (model.getSelectedPoint().row > 0) {
			model.setSelection(
					PointFactory.point(0, model.getSelectedPoint().col));
		}
	}

	/**
	 * Move the cursor all the way to the bottom.
	 */
	public void jumpDown() {
		if (model.getSelectedPoint().row < (model.getMapDimensions().rows - 1)) {
			model.setSelection(
					PointFactory.point(model.getMapDimensions().rows - 1,
							model.getSelectedPoint().col));
		}
	}

	/**
	 * Move the cursor all the way to the left.
	 */
	public void jumpLeft() {
		if (model.getSelectedPoint().col > 0) {
			model.setSelection(
					PointFactory.point(model.getSelectedPoint().row, 0));
		}
	}

	/**
	 * Move the cursor all the way to the right.
	 */
	public void jumpRight() {
		if (model.getSelectedPoint().col < (model.getMapDimensions().cols - 1)) {
			model.setSelection(PointFactory.point(model.getSelectedPoint().row,
					model.getMapDimensions().rows - 1));
		}
	}

	/**
	 * @return a String representation of the object.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "DirectionSelectionChangerImpl";
	}

	/**
	 * Scroll.
	 *
	 * @param horiz   whether to scroll horizontally.
	 * @param forward whether to scroll forward (down or right)
	 * @param count   how many times to scroll
	 */
	private void scroll(final boolean horiz, final boolean forward,
	                    final int count) {
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
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void mouseWheelMoved(@Nullable final MouseWheelEvent evt) {
		if (evt == null) {
			return;
		} else if (evt.isControlDown() || evt.isMetaDown()) {
			final int count = evt.getWheelRotation();
			if (count < 0) {
				for (int i = 0; i > count; i--) {
					model.zoomIn();
				}
			} else {
				for (int i = 0; i < count; i++) {
					model.zoomOut();
				}
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
