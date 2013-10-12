package view.map.main;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JScrollBar;

import org.eclipse.jdt.annotation.Nullable;

import model.map.MapDimensions;
import model.viewer.IViewerModel;
import model.viewer.VisibleDimensions;

/**
 * A class to change the visible area of the map based on the user's use of the
 * scrollbars.
 *
 * @author Jonathan Lovelace
 *
 */
public class ScrollListener implements AdjustmentListener,
		PropertyChangeListener {
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
		final MapDimensions mapDim = model.getMapDimensions();
		mapDimensions = mapDim;
		hbar = horizBar;
		hbar.getModel().setRangeProperties(
				Math.max(model.getSelectedPoint().col, 0), 1, 0,
				mapDim.cols - model.getDimensions().getWidth(), false);
		hbar.setInputVerifier(new InputVerifier() {
			/**
			 * Verify input
			 *
			 * @param input the input event to verify
			 * @return whether to let it proceed
			 */
			@Override
			public boolean verify(@Nullable final JComponent input) {
				return input instanceof JScrollBar && isInRange(0,
						((JScrollBar) input).getValue(), mapDim.cols
								- map.getDimensions().getWidth());
			}
		});
		vbar = vertBar;
		vbar.getModel().setRangeProperties(
				Math.max(model.getSelectedPoint().row, 0), 1, 0,
				mapDim.rows - model.getDimensions().getHeight(), false);
		vbar.setInputVerifier(new InputVerifier() {
			/**
			 * Verify input
			 *
			 * @param input the input event to verify
			 * @return whether to let it proceed
			 */
			@Override
			public boolean verify(@Nullable final JComponent input) {
				return input instanceof JScrollBar && isInRange(0,
						((JScrollBar) input).getValue(), mapDim.rows
								- map.getDimensions().getHeight());
			}
		});
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
		this(map, new JScrollBar(Adjustable.HORIZONTAL), new JScrollBar(Adjustable.VERTICAL));
		component.add(hbar, BorderLayout.SOUTH);
		component.add(vbar, BorderLayout.EAST);
	}

	/**
	 * Set up listeners. This is now done in a method rather than the
	 * constructor so we don't get "dead store" warnings.
	 */
	public void setUpListeners() {
		model.addPropertyChangeListener(this);
		hbar.addAdjustmentListener(this);
		vbar.addAdjustmentListener(this);
	}

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
	 *
	 * @param evt the event to handle
	 *
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(@Nullable final PropertyChangeEvent evt) {
		if (evt == null) {
			return;
		} else if ("dimensions".equals(evt.getPropertyName())
				&& evt.getNewValue() instanceof VisibleDimensions
				&& !((VisibleDimensions) evt.getNewValue())
						.isSameSize(dimensions)) {
			dimensions = model.getDimensions();
			hbar.getModel().setRangeProperties(
					Math.max(model.getSelectedPoint().col, 0),
					1, 0,
					mapDimensions.cols - model.getDimensions().getWidth(),
					false);
			vbar.getModel().setRangeProperties(
					Math.max(model.getSelectedPoint().row, 0),
					1, 0,
					mapDimensions.rows - model.getDimensions().getHeight(),
					false);
		} else if ("tile".equals(evt.getPropertyName())) {
			if (!isInRange(model.getDimensions().getMinimumCol(), model
					.getSelectedPoint().col, model
					.getDimensions().getMaximumCol())) {
				hbar.getModel()
						.setValue(
								Math.max(model.getSelectedPoint()
										.col, 0));
			}
			if (!isInRange(model.getDimensions().getMinimumRow(), model
					.getSelectedPoint().row, model
					.getDimensions().getMaximumRow())) {
				vbar.getModel()
						.setValue(
								Math.max(model.getSelectedPoint()
										.row, 0));
			}
		} else if ("map".equals(evt.getPropertyName())) {
			mapDimensions = model.getMapDimensions();
			hbar.getModel().setRangeProperties(0, 1, 0,
					mapDimensions.cols - model.getDimensions().getWidth(),
					false);
			vbar.getModel().setRangeProperties(0, 1, 0,
					mapDimensions.rows - model.getDimensions().getHeight(),
					false);
			dimensions = model.getDimensions();
		}
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
	 *
	 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
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
