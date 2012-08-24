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
	 * @param map the map model to work with
	 * @param horizBar the horizontal scroll bar to work with
	 * @param vertBar the vertical scroll bar to work with
	 */
	public ScrollListener(final MapModel map, final JScrollBar horizBar,
			final JScrollBar vertBar) {
		model = map;
		dimensions = map.getDimensions();
		hbar = horizBar;
		hbar.getModel().setRangeProperties(
				Math.max(model.getSelectedTile().getLocation().col, 0), 1, 0,
				model.getSizeCols() - model.getDimensions().getWidth(), false);
		hbar.setInputVerifier(new InputVerifier() {
			/**
			 * Verify input
			 *
			 * @param input the input event to verify
			 * @return whether to let it proceed
			 */
			@Override
			public boolean verify(final JComponent input) {
				return input instanceof JScrollBar && isInRange(0,
						((JScrollBar) input).getValue(), map.getSizeCols()
								- map.getDimensions().getWidth());
			}
		});
		vbar = vertBar;
		vbar.getModel().setRangeProperties(
				Math.max(model.getSelectedTile().getLocation().row, 0), 1, 0,
				model.getSizeRows() - model.getDimensions().getHeight(), false);
		vbar.setInputVerifier(new InputVerifier() {
			/**
			 * Verify input
			 *
			 * @param input the input event to verify
			 * @return whether to let it proceed
			 */
			@Override
			public boolean verify(final JComponent input) {
				return input instanceof JScrollBar && isInRange(0,
						((JScrollBar) input).getValue(), map.getSizeRows()
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
	public ScrollListener(final MapModel map, final JComponent component) {
		model = map;
		dimensions = map.getDimensions();
		hbar = new JScrollBar(Adjustable.HORIZONTAL);
		hbar.getModel().setRangeProperties(
				Math.max(model.getSelectedTile().getLocation().col, 0), 1, 0,
				model.getSizeCols() - model.getDimensions().getWidth(), false);
		hbar.setInputVerifier(new InputVerifier() {
			/**
			 * Verify input
			 *
			 * @param input the input event to verify
			 * @return whether to let it proceed
			 */
			@Override
			public boolean verify(final JComponent input) {
				return input instanceof JScrollBar && isInRange(0,
						((JScrollBar) input).getValue(), map.getSizeCols()
								- map.getDimensions().getWidth());
			}
		});
		component.add(hbar, BorderLayout.SOUTH);
		vbar = new JScrollBar(Adjustable.VERTICAL);
		vbar.getModel().setRangeProperties(
				Math.max(model.getSelectedTile().getLocation().row, 0), 1, 0,
				model.getSizeRows() - model.getDimensions().getHeight(), false);
		vbar.setInputVerifier(new InputVerifier() {
			/**
			 * Verify input
			 *
			 * @param input the input event to verify
			 * @return whether to let it proceed
			 */
			@Override
			public boolean verify(final JComponent input) {
				return input instanceof JScrollBar && isInRange(0,
						((JScrollBar) input).getValue(), map.getSizeRows()
								- map.getDimensions().getHeight());
			}
		});
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
	 * @param evt the event to handle
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
					Math.max(model.getSelectedTile().getLocation().col, 0),
					1, 0,
					model.getSizeCols() - model.getDimensions().getWidth(),
					false);
			vbar.getModel().setRangeProperties(
					Math.max(model.getSelectedTile().getLocation().row, 0),
					1, 0,
					model.getSizeRows() - model.getDimensions().getHeight(),
					false);
		} else if ("tile".equals(evt.getPropertyName())) {
			if (!isInRange(model.getDimensions().getMinimumCol(), model
					.getSelectedTile().getLocation().col, model
					.getDimensions().getMaximumCol())) {
				hbar.getModel()
						.setValue(
								Math.max(model.getSelectedTile().getLocation()
										.col, 0));
			}
			if (!isInRange(model.getDimensions().getMinimumRow(), model
					.getSelectedTile().getLocation().row, model
					.getDimensions().getMaximumRow())) {
				vbar.getModel()
						.setValue(
								Math.max(model.getSelectedTile().getLocation()
										.row, 0));
			}
		} else if ("map".equals(evt.getPropertyName())) {
			hbar.getModel().setRangeProperties(0, 1, 0,
					model.getSizeCols() - model.getDimensions().getWidth(),
					false);
			vbar.getModel().setRangeProperties(0, 1, 0,
					model.getSizeRows() - model.getDimensions().getHeight(),
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
	public void adjustmentValueChanged(final AdjustmentEvent evt) {
		if (hbar == evt.getSource() || vbar == evt.getSource()) {
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
