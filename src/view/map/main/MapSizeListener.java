package view.map.main;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import model.viewer.MapModel;
import model.viewer.VisibleDimensions;
/**
 * A listener to adjust the number of displayed tiles based on the area to display them in.
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
	 * @param map the map model we'll be modifying.
	 */
	public MapSizeListener(final MapModel map) {
		super();
		model = map;
	}
	/**
	 * Adjust the visible size of the map based on the map component being resized.
	 * @param event the resize event
	 */
	@Override
	public void componentResized(final ComponentEvent event) {
		if (event.getSource() instanceof MapGUI && event.getSource() instanceof Component) {
			synchronized (model) {
				final int visibleCols = ((Component) event.getSource()).getWidth()
						/ ((MapGUI) event.getSource()).getTileSize();
				final int visibleRows = ((Component) event.getSource()).getHeight()
						/ ((MapGUI) event.getSource()).getTileSize();
				int minCol = model.getDimensions().getMinimumCol();
				int maxCol = model.getDimensions().getMaximumCol();
				int minRow = model.getDimensions().getMinimumRow();
				int maxRow = model.getDimensions().getMaximumRow();
				final int totalRows = model.getSizeRows();
				final int totalCols = model.getSizeCols();
				if ((visibleCols <= totalCols && visibleCols != (maxCol - minCol))
						|| (visibleRows <= totalRows && visibleRows != (maxRow - minRow))) {
					if (minCol + visibleCols < totalCols) {
						maxCol = minCol + visibleCols;
					} else {
						maxCol = totalCols - 1;
						minCol = Math.max(0, totalCols - visibleCols - 1);
					}
					if (minRow + visibleRows < totalRows) {
						maxRow = minRow + visibleRows;
					} else {
						maxRow = totalRows - 1;
						minRow = Math.max(0, totalRows - visibleRows - 1);
					}
					model.setDimensions(new VisibleDimensions(minRow, maxRow, minCol, maxCol));
				}
			}
		}
	}
}
