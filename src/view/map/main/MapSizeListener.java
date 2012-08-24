package view.map.main;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import model.viewer.MapModel;
import model.viewer.VisibleDimensions;

/**
 * A listener to adjust the number of displayed tiles based on the area to
 * display them in.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapSizeListener extends ComponentAdapter {
	/**
	 * The map model we'll be modifying.
	 */
	private final MapModel model;

	/**
	 * Constructor.
	 *
	 * @param map the map model we'll be modifying.
	 */
	public MapSizeListener(final MapModel map) {
		super();
		model = map;
	}

	/**
	 * Adjust the visible size of the map based on the map component being
	 * resized.
	 *
	 * @param event the resize event
	 */
	@Override
	public void componentResized(final ComponentEvent event) {
		if (event.getComponent() instanceof MapGUI) {
			synchronized (model) {
				final int visibleCols = event.getComponent().getWidth()
						/ ((MapGUI) event.getSource()).getTileSize();
				final int visibleRows = event.getComponent().getHeight()
						/ ((MapGUI) event.getSource()).getTileSize();
				int minCol = model.getDimensions().getMinimumCol();
				int maxCol = model.getDimensions().getMaximumCol();
				int minRow = model.getDimensions().getMinimumRow();
				int maxRow = model.getDimensions().getMaximumRow();
				final int totalRows = model.getSizeRows();
				final int totalCols = model.getSizeCols();
				if (visibleCols != maxCol - minCol
						|| visibleRows != maxRow - minRow) {
					if (visibleCols >= totalCols) {
						minCol = 0;
						maxCol = totalCols - 1;
					} else if (minCol + visibleCols >= totalCols) {
						maxCol = totalCols - 1;
						minCol = totalCols - visibleCols - 2;
					} else {
						maxCol = minCol + visibleCols - 1;
					}
					if (visibleRows >= totalRows) {
						minRow = 0;
						maxRow = totalRows - 1;
					} else if (minRow + visibleRows >= totalRows) {
						maxRow = totalRows - 1;
						minRow = totalRows - visibleRows - 2;
					} else {
						maxRow = minRow + visibleRows - 1;
					}
					model.setDimensions(new VisibleDimensions(minRow, maxRow,
							minCol, maxCol));
				}
			}
		}
	}

	/**
	 * Treat a "shown" event as a "resized" event.
	 *
	 * @param event the event to handle.
	 */
	@Override
	public void componentShown(final ComponentEvent event) {
		componentResized(event);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapSizeListener";
	}
}
