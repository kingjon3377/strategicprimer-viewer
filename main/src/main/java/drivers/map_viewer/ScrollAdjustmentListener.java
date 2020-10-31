package drivers.map_viewer;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import common.map.Point;

import javax.swing.BoundedRangeModel;

import javax.swing.event.ChangeEvent;

/* package */ class ScrollAdjustmentListener { // FIXME: Listen to some events so we can reset on map or selected point change
	private final IViewerModel model;

	public ScrollAdjustmentListener(final IViewerModel model) {
		this.model = model;
	}

	// TODO: Do we really need to track these, or can we just rely on {@link model.getCursor}?
	private @Nullable Integer oldRow = null;
	private @Nullable Integer oldColumn = null;

	// TODO: Should probably track horizonal and vertical scrolling separately
	private boolean adjusting = false;

	public boolean isAdjusting() {
		return adjusting;
	}

	// FIXME: Do we really need to expose the setter to all clients?
	public void setAdjusting(final boolean adjusting) {
		this.adjusting = adjusting;
	}

	public void horizontalScroll(final ChangeEvent event) {
		final BoundedRangeModel source = (BoundedRangeModel) event.getSource();
		if (adjusting) {
			LovelaceLogger.trace(
				"Waiting for scrollbar to stop adjusting before handling horizontal scroll");
			return;
		}
		LovelaceLogger.trace("Starting to respond to horizontal scroll");
		final VisibleDimensions oldDimensions = model.getVisibleDimensions();
		final int newValue = source.getValue();
		final VisibleDimensions newDimensions;
		if (newValue < 0) {
			LovelaceLogger.warning("Tried to scroll to negative column, skipping ...");
			return;
		} else if (newValue > (model.getMapDimensions().columns() +
				oldDimensions.getColumns().size())) {
			LovelaceLogger.warning("Tried to scroll too far to the right, skipping ...");
			return;
		} else if (oldColumn == null) {
			final int newMinColumn;
			final int newMaxColumn;
			if (oldDimensions.getMinimumColumn() > newValue) {
				LovelaceLogger.debug("User scrolled left");
				newMinColumn = newValue;
				newMaxColumn = newValue + oldDimensions.getWidth() - 1;
			} else if (oldDimensions.getMaximumColumn() < newValue) {
				LovelaceLogger.debug("User scrolled right");
				newMaxColumn = newValue;
				newMinColumn = newValue - oldDimensions.getWidth() + 1;
			} else {
				LovelaceLogger.debug("No cached horizontal coordinate and new value within previous visible area, skipping ...");
				oldColumn = newValue;
				return;
			}
			oldColumn = newValue;
			newDimensions = new VisibleDimensions(oldDimensions.getMinimumRow(),
				oldDimensions.getMaximumRow(), newMinColumn, newMaxColumn);
		} else {
			if (oldColumn == newValue) {
				LovelaceLogger.trace(
					"Horizontal scroll to same value, possibly reentrant. Skipping ...");
				return;
			}
			final int offset = newValue - oldColumn;
			LovelaceLogger.debug("User scrolled horizontally by %d tiles, to %d.", offset, newValue);
			oldColumn = newValue;
			if (oldDimensions.getMinimumColumn() + offset < 0) {
				LovelaceLogger.debug("Offset would make negative columns visible, constraining the view.");
				newDimensions = new VisibleDimensions(oldDimensions.getMinimumRow(),
					oldDimensions.getMaximumRow(), 0, oldDimensions.getColumns().size() - 1);
			} else if (oldDimensions.getMaximumColumn() + offset >
					model.getMapDimensions().columns()) {
				LovelaceLogger.debug("Offset would go too far to the right ([%d + %d] / %d), constraining the view.",
					oldDimensions.getMaximumColumn(), offset,
					model.getMapDimensions().columns());
				newDimensions = new VisibleDimensions(oldDimensions.getMinimumRow(),
					oldDimensions.getMaximumRow(),
					Math.max(0, model.getMapDimensions().columns() -
						oldDimensions.getColumns().size() - 1),
					model.getMapDimensions().columns() - 1);
			} else { // TODO: check how this constraint effect makes scrolling feel
				newDimensions = new VisibleDimensions(oldDimensions.getMinimumRow(),
					oldDimensions.getMaximumRow(), oldDimensions.getMinimumColumn() + offset,
					oldDimensions.getMaximumColumn() + offset);
			}
		}
		final boolean oldAdjusting = adjusting;
		adjusting = true;
		model.setCursor(new Point(model.getCursor().row(), newValue));
		model.setVisibleDimensions(newDimensions);
		adjusting = oldAdjusting;
	}

	public void verticalScroll(final ChangeEvent event) {
		final BoundedRangeModel source = (BoundedRangeModel) event.getSource();
		if (adjusting) {
			// TODO: We'd like to do *some* handling, in case the user is dragging the tongue. Mutex flag again?
			LovelaceLogger.trace(
				"Waiting for scrollbar to stop adjusting before handling vertical scroll");
			return;
		}
		LovelaceLogger.trace("Starting to respond to vertical scroll");
		final int newValue = source.getValue();
		final VisibleDimensions oldDimensions = model.getVisibleDimensions();
		final VisibleDimensions newDimensions;
		if (newValue < 0) {
			LovelaceLogger.warning("Tried to scroll to negative row, skipping ...");
			return;
		} else if (newValue > (model.getMapDimensions().rows() +
				oldDimensions.getRows().size())) {
			LovelaceLogger.warning("Tried to scroll too far down, skipping ...");
			return;
		} else if (oldRow != null) {
			if (oldRow == newValue) {
				LovelaceLogger.trace(
					"Vertical scroll to same value, possibly reentrant. Skipping ...");
				return;
			}
			final int offset = newValue - oldRow;
			LovelaceLogger.debug("User scrolled vertically by %d tiles, to %d.", offset, newValue);
			oldRow = newValue;
			if (oldDimensions.getMinimumRow() + offset < 0) {
				LovelaceLogger.debug("Offset would make negative rows visible, constraining the view.");
				newDimensions = new VisibleDimensions(0, oldDimensions.getRows().size() - 1,
					oldDimensions.getMinimumColumn(), oldDimensions.getMaximumColumn());
			} else if (oldDimensions.getMaximumRow() + offset > model.getMapDimensions().rows()) {
				LovelaceLogger.debug("Offset would go too far down ([%d + %d] / %d), constraining the view.",
					oldDimensions.getMaximumRow(), offset, model.getMapDimensions().rows());
				newDimensions = new VisibleDimensions(
					Math.max(0, model.getMapDimensions().rows() -
						oldDimensions.getRows().size() - 1),
					model.getMapDimensions().rows() - 1,
					oldDimensions.getMinimumColumn(), oldDimensions.getMaximumColumn());
			} else { // TODO: check how this constraint effect makes scrolling feel
				newDimensions = new VisibleDimensions(oldDimensions.getMinimumRow() + offset,
					oldDimensions.getMaximumRow() + offset, oldDimensions.getMinimumColumn(),
					oldDimensions.getMaximumColumn());
			}
		} else {
			final int newMinRow;
			final int newMaxRow;
			if (oldDimensions.getMinimumRow() > newValue) {
				LovelaceLogger.debug("User scrolled down");
				newMinRow = newValue;
				newMaxRow = newValue + oldDimensions.getHeight() - 1;
			} else if (oldDimensions.getMaximumRow() < newValue) {
				LovelaceLogger.debug("User scrolled up");
				newMaxRow = newValue;
				newMinRow = newValue - oldDimensions.getHeight() + 1;
			} else {
				LovelaceLogger.debug("No cached vertical coordinate and new value within previous visible area, skipping ...");
				oldRow = newValue;
				return;
			}
			oldRow = newValue;
			newDimensions = new VisibleDimensions(newMinRow, newMaxRow,
				oldDimensions.getMinimumColumn(), oldDimensions.getMaximumColumn());
		}
		final boolean oldAdjusting = adjusting;
		adjusting = true;
		model.setCursor(new Point(newValue, model.getCursor().column()));
		model.setVisibleDimensions(newDimensions);
		adjusting = oldAdjusting;
	}
}
