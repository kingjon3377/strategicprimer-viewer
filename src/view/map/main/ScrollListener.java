package view.map.main;

import java.awt.*;
import java.awt.event.AdjustmentListener;
import javax.swing.*;
import model.listeners.GraphicalParamsListener;
import model.listeners.MapChangeListener;
import model.listeners.SelectionChangeListener;
import model.map.MapDimensions;
import model.map.Point;
import model.viewer.IViewerModel;
import model.viewer.VisibleDimensions;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to change the visible area of the map based on the user's use of the
 * scrollbars.
 *
 * TODO: Merge much of this functionality into MapScrollPanel? Or at least make it accept
 * the necessary events (from the model if not its scroll-bars) and pass them to this
 * object. Keep track of VisibleDimensions and selected point directly, rather than
 * querying the model, so we can drop the reference to the model.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public final class ScrollListener implements MapChangeListener,
		                                             SelectionChangeListener,
		                                             GraphicalParamsListener {
	/**
	 * The map model we're working with.
	 */
	private final IViewerModel model;
	/**
	 * The dimensions of the map.
	 */
	private MapDimensions mapDimensions;
	/**
	 * The current visible dimensions of the map.
	 */
	private VisibleDimensions dimensions;
	/**
	 * The horizontal scroll-bar we deal with.
	 */
	private final JScrollBar hbar;
	/**
	 * The vertical scroll-bar we deal with.
	 */
	private final JScrollBar vbar;

	/**
	 * Constructor.
	 *
	 * @param map      the map model to work with
	 * @param horizBar the horizontal scroll bar to work with
	 * @param vertBar  the vertical scroll bar to work with
	 */
	public ScrollListener(final IViewerModel map, final JScrollBar horizBar,
	                      final JScrollBar vertBar) {
		model = map;
		dimensions = map.getDimensions();
		final MapDimensions mapDim = map.getMapDimensions();
		mapDimensions = mapDim;
		hbar = horizBar;
		final Point selPoint = map.getSelectedPoint();
		hbar.getModel().setRangeProperties(Math.max(selPoint.col, 0), 1, 0,
				mapDim.cols - map.getDimensions().getWidth(), false);
		hbar.setInputVerifier(new LocalInputVerifier(mapDim, map, true));
		vbar = vertBar;
		vbar.getModel().setRangeProperties(Math.max(selPoint.row, 0), 1, 0,
				mapDim.rows - map.getDimensions().getHeight(), false);
		vbar.setInputVerifier(new LocalInputVerifier(mapDim, map, false));
		final AdjustmentListener adjList =
				evt -> model.setDimensions(new VisibleDimensions(vbar.getValue(), vbar
						                                                                  .getValue() +
						                                                                  dimensions
								                                                                  .getHeight(),

						                                                hbar.getValue(),
						                                                hbar
								                                                .getValue() +
								                                                dimensions
										                                                .getWidth()));
		hbar.addAdjustmentListener(adjList);
		vbar.addAdjustmentListener(adjList);
	}

	/**
	 * Alternate constructor to reduce complexity in the calling class. The uncheckable
	 * precondition is that the component is using a BorderLayout, and doesn't already
	 * have members at south or east.
	 *
	 * @param map       the map model to work with
	 * @param component the component to attach the scrollbars to.
	 */
	public ScrollListener(final IViewerModel map, final JComponent component) {
		this(map, new JScrollBar(Adjustable.HORIZONTAL), new JScrollBar(
				                                                               Adjustable.VERTICAL));
		component.add(hbar, BorderLayout.PAGE_END);
		component.add(vbar, BorderLayout.LINE_END);
	}

	/**
	 * @param oldDim the old visible dimensions
	 * @param newDim the new visible dimensions
	 */
	@Override
	public void dimensionsChanged(final VisibleDimensions oldDim,
	                              final VisibleDimensions newDim) {
		dimensions = newDim;
		hbar.getModel().setRangeProperties(
				Math.max(model.getSelectedPoint().col, 0), 1, 0,
				mapDimensions.cols - newDim.getWidth(), false);
		vbar.getModel().setRangeProperties(
				Math.max(model.getSelectedPoint().row, 0), 1, 0,
				mapDimensions.rows - newDim.getHeight(), false);
	}

	/**
	 * @param oldSize the old zoom level
	 * @param newSize the new zoom level
	 */
	@Override
	public void tsizeChanged(final int oldSize, final int newSize) {
		// We don't do anything with this.
	}

	/**
	 * The property-change based version this replaces went to the model for the selected
	 * point rather than looking at the reported new value; since it's typesafe here, and
	 * probably faster, this switched to using the new value it was passed.
	 *
	 * @param old      th previously selected point
	 * @param newPoint the newly selected point
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old,
	                                 final Point newPoint) {
		final VisibleDimensions vdim = model.getDimensions();
		if (!isInRange(vdim.getMinimumCol(), newPoint.col, vdim.getMaximumCol())) {
			hbar.getModel().setValue(Math.max(newPoint.col, 0));
		}
		if (!isInRange(vdim.getMinimumRow(), newPoint.row, vdim.getMaximumRow())) {
			vbar.getModel().setValue(Math.max(newPoint.row, 0));
		}
	}

	/**
	 * Handle notification that a new map was loaded.
	 */
	@Override
	public void mapChanged() {
		mapDimensions = model.getMapDimensions();
		hbar.getModel().setRangeProperties(0, 1, 0,
				mapDimensions.cols - model.getDimensions().getWidth(), false);
		vbar.getModel().setRangeProperties(0, 1, 0,
				mapDimensions.rows - model.getDimensions().getHeight(), false);
		dimensions = model.getDimensions();
	}

	/**
	 * @param min   the start of a range
	 * @param value a value
	 * @param max   the end of te range
	 * @return whether the value is in the range
	 */
	protected static boolean isInRange(final int min, final int value,
	                                   final int max) {
		return (value >= min) && (value <= max);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ScrollListener";
	}

	private static class LocalInputVerifier extends InputVerifier {
		private final MapDimensions dimensions;
		private final IViewerModel map;
		private final boolean horiz;

		protected LocalInputVerifier(final MapDimensions mapDim,
		                             final IViewerModel mapModel,
		                             final boolean horizontal) {
			dimensions = mapDim;
			map = mapModel;
			horiz = horizontal;
		}

		private int dimension() {
			if (horiz) {
				return dimensions.cols;
			} else {
				return dimensions.rows;
			}
		}

		private int visibleDimension() {
			if (horiz) {
				return map.getDimensions().getWidth();
			} else {
				return map.getDimensions().getHeight();
			}
		}

		@Override
		public boolean verify(@Nullable final JComponent input) {
			return (input instanceof JScrollBar)
					       && isInRange(0, ((JScrollBar) input).getValue(),
					dimension() - visibleDimension());
		}
	}
}
