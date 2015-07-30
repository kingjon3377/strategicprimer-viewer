package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.eclipse.jdt.annotation.Nullable;

import model.map.MapDimensions;
import model.map.Point;
import model.viewer.IViewerModel;
import model.viewer.VisibleDimensions;

/**
 * A class to listen for zoom menu events.
 *
 * @author Jonathan Lovelace
 */
public final class ZoomListener implements ActionListener {
	/**
	 * The map model.
	 */
	private final IViewerModel model;

	/**
	 * Constructor.
	 *
	 * @param vmodel the viewer model, which now handles the zoom level
	 */
	public ZoomListener(final IViewerModel vmodel) {
		model = vmodel;
	}

	/**
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return;
		} else if ("zoom in".equalsIgnoreCase(evt.getActionCommand())) {
			model.zoomIn();
		} else if ("zoom out".equalsIgnoreCase(evt.getActionCommand())) {
			model.zoomOut();
		} else if ("center".equalsIgnoreCase(evt.getActionCommand())) {
			Point selection = model.getSelectedPoint();
			MapDimensions dims = model.getMapDimensions();
			VisibleDimensions vDims = model.getDimensions();
			int topRow, leftColumn;
			if ((selection.row - vDims.getHeight() / 2) <= 0) {
				topRow = 0;
			} else if ((selection.row + vDims.getHeight() / 2) >= dims.getRows()) {
				topRow = dims.getRows() - vDims.getHeight();
			} else {
				topRow = selection.row - vDims.getHeight() / 2;
			}
			if ((selection.col - vDims.getWidth() / 2) <= 0) {
				leftColumn = 0;
			} else if ((selection.col + vDims.getWidth() / 2) >= dims.getColumns()) {
				leftColumn = dims.getColumns() - vDims.getWidth();
			} else {
				leftColumn = selection.col - vDims.getWidth() / 2;
			}
			VisibleDimensions next =
					new VisibleDimensions(topRow, topRow + dims.getRows(),
							leftColumn, leftColumn + dims.getColumns());
			model.setDimensions(next);
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ZoomListener";
	}
}
