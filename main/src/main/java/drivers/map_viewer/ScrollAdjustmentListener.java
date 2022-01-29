package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Adjustable;

import common.map.MapDimensions;
import common.map.Point;

import lovelace.util.BorderedPanel;

import drivers.common.MapChangeListener;
import drivers.common.SelectionChangeListener;

import javax.swing.JScrollBar;
import javax.swing.JComponent;
import javax.swing.BoundedRangeModel;
import javax.swing.InputVerifier;

import common.map.fixtures.mobile.IUnit;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.event.ChangeEvent;
import java.util.logging.Logger;
import java.util.logging.Level;

/* package */ class ScrollAdjustmentListener { // FIXME: Listen to some events so we can reset on map or selected point change
	private static final Logger LOGGER = Logger.getLogger(ScrollAdjustmentListener.class.getName());

	private final IViewerModel model;

	public ScrollAdjustmentListener(IViewerModel model) {
		this.model = model;
	}

	// TODO: Do we really need to track these, or can we just rely on {@link model.getCursor}?
	@Nullable
	private Integer oldRow = null;
	@Nullable
	private Integer oldColumn = null;

	// TODO: Should probably track horizonal and vertical scrolling separately
	private boolean adjusting = false;

	public boolean isAdjusting() {
		return adjusting;
	}

	// FIXME: Do we really need to expose the setter to all clients?
	public void setAdjusting(boolean adjusting) {
		this.adjusting = adjusting;
	}

	public void horizontalScroll(ChangeEvent event) {
		BoundedRangeModel source = (BoundedRangeModel) event.getSource();
		if (adjusting) {
			LOGGER.finer(
				"Waiting for scrollbar to stop adjusting before handling horizontal scroll");
			return;
		}
		LOGGER.finer("Starting to respond to horizontal scroll");
		VisibleDimensions oldDimensions = model.getVisibleDimensions();
		int newValue = source.getValue();
		VisibleDimensions newDimensions;
		if (newValue < 0) {
			LOGGER.warning("Tried to scroll to negative column, skipping ...");
			return;
		} else if (newValue > (model.getMapDimensions().getColumns() +
				oldDimensions.getColumns().size())) {
			LOGGER.warning("Tried to scroll too far to the right, skipping ...");
			return;
		} else if (oldColumn != null) { // TODO: invert
			if (oldColumn.intValue() == newValue) {
				LOGGER.finer(
					"Horizontal scroll to same value, possibly reentrant. Skipping ...");
				return;
			}
			int offset = newValue - oldColumn;
			LOGGER.fine(String.format("User scrolled horizontally by %d tiles, to %d.",
				offset, newValue));
			oldColumn = newValue;
			newDimensions = new VisibleDimensions(oldDimensions.getMinimumRow(),
				oldDimensions.getMaximumRow(), oldDimensions.getMinimumColumn() + offset,
				oldDimensions.getMaximumColumn() + offset);
		} else {
			int newMinColumn;
			int newMaxColumn;
			if (oldDimensions.getMinimumColumn() > newValue) {
				LOGGER.fine("User scrolled left");
				newMinColumn = newValue;
				newMaxColumn = newValue + oldDimensions.getWidth() - 1;
			} else if (oldDimensions.getMaximumColumn() < newValue) {
				LOGGER.fine("User scrolled right");
				newMaxColumn = newValue;
				newMinColumn = newValue - oldDimensions.getWidth() + 1;
			} else {
				LOGGER.fine("No cached horizontal coordinate and new value within previous visible area, skipping ...");
				oldColumn = newValue;
				return;
			}
			oldColumn = newValue;
			newDimensions = new VisibleDimensions(oldDimensions.getMinimumRow(),
				oldDimensions.getMaximumRow(), newMinColumn, newMaxColumn);
		}
		boolean oldAdjusting = adjusting;
		adjusting = true;
		model.setCursor(new Point(model.getCursor().getRow(), newValue));
		model.setVisibleDimensions(newDimensions);
		adjusting = oldAdjusting;
	}

	public void verticalScroll(ChangeEvent event) {
		BoundedRangeModel source = (BoundedRangeModel) event.getSource();
		if (adjusting) {
			// TODO: We'd like to do *some* handling, in case the user is dragging the tongue. Mutex flag again?
			LOGGER.finer(
				"Waiting for scrollbar to stop adjusting before handling vertical scroll");
			return;
		}
		LOGGER.finer("Starting to respond to vertical scroll");
		int newValue = source.getValue();
		VisibleDimensions oldDimensions = model.getVisibleDimensions();
		VisibleDimensions newDimensions;
		if (newValue < 0) {
			LOGGER.warning("Tried to scroll to negative row, skipping ...");
			return;
		} else if (newValue > (model.getMapDimensions().getRows() +
				oldDimensions.getRows().size())) {
			LOGGER.warning("Tried to scroll too far down, skipping ...");
			return;
		} else if (oldRow != null) {
			if (oldRow.intValue() == newValue) {
				LOGGER.finer(
					"Vertical scroll to same value, possibly reentrant. Skipping ...");
				return;
			}
			int offset = newValue - oldRow;
			LOGGER.fine(String.format("User scrolled vertically by %d tiles, to %d.",
				offset, newValue));
			oldRow = newValue;
			newDimensions = new VisibleDimensions(oldDimensions.getMinimumRow() + offset,
				oldDimensions.getMaximumRow() + offset, oldDimensions.getMinimumColumn(),
				oldDimensions.getMaximumColumn());
		} else {
			int newMinRow;
			int newMaxRow;
			if (oldDimensions.getMinimumRow() > newValue) {
				LOGGER.fine("User scrolled down");
				newMinRow = newValue;
				newMaxRow = newValue + oldDimensions.getHeight() - 1;
			} else if (oldDimensions.getMaximumRow() < newValue) {
				LOGGER.fine("User scrolled up");
				newMaxRow = newValue;
				newMinRow = newValue - oldDimensions.getHeight() + 1;
			} else {
				LOGGER.fine("No cached vertical coordinate and new value within previous visible area, skipping ...");
				oldRow = newValue;
				return;
			}
			oldRow = newValue;
			newDimensions = new VisibleDimensions(newMinRow, newMaxRow,
				oldDimensions.getMinimumColumn(), oldDimensions.getMaximumColumn());
		}
		boolean oldAdjusting = adjusting;
		adjusting = true;
		model.setCursor(new Point(newValue, model.getCursor().getColumn()));
		model.setVisibleDimensions(newDimensions);
		adjusting = oldAdjusting;
	}
}
