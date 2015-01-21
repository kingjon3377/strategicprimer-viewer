package view.map.main;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JScrollBar;

import model.listeners.GraphicalParamsListener;
import model.listeners.MapChangeListener;
import model.listeners.SelectionChangeListener;
import model.map.ITile;
import model.map.MapDimensions;
import model.map.Point;
import model.viewer.IViewerModel;
import model.viewer.VisibleDimensions;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to change the visible area of the map based on the user's use of the
 * scrollbars.
 *
 * TODO: Merge much of this functionality into MapScrollPanel? Or at least make
 * it accept the necessary events (from the model if not its scroll-bars) and
 * pass them to this object. Keep track of VisibleDimensions and selected point
 * directly, rather than querying the model, so we can drop the reference to the
 * model.
 *
 * @author Jonathan Lovelace
 *
 */
public class ScrollListener implements AdjustmentListener, MapChangeListener,
		SelectionChangeListener, GraphicalParamsListener {
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
	 * @param map the map model to work with
	 * @param horizBar the horizontal scroll bar to work with
	 * @param vertBar the vertical scroll bar to work with
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
		hbar.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(@Nullable final JComponent input) {
				return input instanceof JScrollBar
						&& isInRange(0, ((JScrollBar) input).getValue(),
								mapDim.cols - map.getDimensions().getWidth());
			}
		});
		vbar = vertBar;
		vbar.getModel().setRangeProperties(Math.max(selPoint.row, 0), 1, 0,
				mapDim.rows - map.getDimensions().getHeight(), false);
		vbar.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(@Nullable final JComponent input) {
				return input instanceof JScrollBar
						&& isInRange(0, ((JScrollBar) input).getValue(),
								mapDim.rows - map.getDimensions().getHeight());
			}
		});
		hbar.addAdjustmentListener(this);
		vbar.addAdjustmentListener(this);
	}

	/**
	 * Alternate constructor to reduce complexity in the calling class. The
	 * uncheckable precondition is that the component is using a BorderLayout,
	 * and doesn't already have members at south or east.
	 *
	 * @param map the map model to work with
	 * @param component the component to attach the scrollbars to.
	 */
	public ScrollListener(final IViewerModel map, final JComponent component) {
		this(map, new JScrollBar(Adjustable.HORIZONTAL), new JScrollBar(
				Adjustable.VERTICAL));
		component.add(hbar, BorderLayout.SOUTH);
		component.add(vbar, BorderLayout.EAST);
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
	 * The property-change based version this replaces went to the model for the
	 * selected point rather than looking at the reported new value; since it's
	 * typesafe here, and probably faster, this switched to using the new value
	 * it was passed.
	 *
	 * @param old th previously selected point
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
	 * @param old the previously selected tile
	 * @param newTile the newly selected tile
	 */
	@Override
	public void selectedTileChanged(@Nullable final ITile old, final ITile newTile) {
		// We only care about the point.
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
	 * @param min the start of a range
	 * @param value a value
	 * @param max the end of te range
	 * @return whether the value is in the range
	 */
	protected static boolean isInRange(final int min, final int value,
			final int max) {
		return value >= min && value <= max;
	}

	/**
	 * Handle scroll-bar events.
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void adjustmentValueChanged(@Nullable final AdjustmentEvent evt) {
		if (evt != null && (hbar == evt.getSource() || vbar == evt.getSource())) {
			model.setDimensions(new VisibleDimensions(vbar.getValue(), vbar
					.getValue() + dimensions.getHeight(), hbar.getValue(), hbar
					.getValue() + dimensions.getWidth()));
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ScrollListener";
	}
}
