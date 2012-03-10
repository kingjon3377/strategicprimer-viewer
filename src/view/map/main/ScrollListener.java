package view.map.main;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JScrollBar;

import model.viewer.MapModel;
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
	 * @param map
	 *            the map model to work with
	 * @param horizBar
	 *            the horizontal scroll bar to work with
	 * @param vertBar
	 *            the vertical scroll bar to work with
	 */
	public ScrollListener(final MapModel map, final JScrollBar horizBar,
			final JScrollBar vertBar) {
		model = map;
		dimensions = map.getDimensions();
		hbar = horizBar;
		vbar = vertBar;
	}
	/**
	 * Set up listeners. This is now done in a method rather than the constructor so we don't get "dead store" warnings.
	 */
	public void setUpListeners() {
		model.addPropertyChangeListener(this);
		hbar.addAdjustmentListener(this);
		vbar.addAdjustmentListener(this);
	}
	/**
	 * The map model we're working with.
	 */
	private final MapModel model;
	/**
	 * The current dimensions of the map.
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
	 * @param evt
	 *            the event to handle
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("dimensions".equals(evt.getPropertyName())
				&& evt.getNewValue() instanceof VisibleDimensions
				&& !((VisibleDimensions) evt.getNewValue())
						.isSameSize(dimensions)) {
			dimensions = model.getDimensions();
			hbar.getModel().setRangeProperties(
					Math.max(model.getSelectedTile().getLocation().col(), 0),
					model.getSizeCols()
							/ (dimensions.getMaximumCol() - dimensions
									.getMinimumCol()), 0, model.getSizeCols(),
					false);
			vbar.getModel().setRangeProperties(
					Math.max(model.getSelectedTile().getLocation().row(), 0),
					model.getSizeRows()
							/ (dimensions.getMaximumRow() - dimensions
									.getMinimumRow()), 0, model.getSizeRows(),
					false);
		} else if ("tile".equals(evt.getPropertyName())) {
			hbar.getModel().setValue(
					Math.max(model.getSelectedTile().getLocation().col(), 0));
			vbar.getModel().setValue(
					Math.max(model.getSelectedTile().getLocation().row(), 0));
		} else if ("map".equals(evt.getPropertyName())) {
			hbar.getModel().setRangeProperties(
					0,
					model.getSizeCols()
							/ (((VisibleDimensions) evt.getNewValue())
									.getMaximumCol() - ((VisibleDimensions) evt
									.getNewValue()).getMinimumCol()), 0,
					model.getSizeCols(), false);
			vbar.getModel().setRangeProperties(
					0,
					model.getSizeRows()
							/ (((VisibleDimensions) evt.getNewValue())
									.getMaximumRow() - ((VisibleDimensions) evt
									.getNewValue()).getMinimumRow()), 0,
					model.getSizeRows(), false);
			dimensions = model.getDimensions();
		}
	}

	/**
	 * Handle scroll-bar events.
	 * 
	 * @param evt
	 *            the event to handle
	 * 
	 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
	 */
	@Override
	public void adjustmentValueChanged(final AdjustmentEvent evt) {
		if (hbar == evt.getSource() || vbar == evt.getSource()) {
			model.setDimensions(new VisibleDimensions(vbar.getValue(),
					vbar.getValue()
							+ (dimensions.getMaximumRow() - dimensions
									.getMinimumRow()), hbar.getValue(), hbar
							.getValue()
							+ (dimensions.getMaximumCol() - dimensions
									.getMinimumCol())));
		}
	}
}
