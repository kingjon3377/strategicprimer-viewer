package drivers.common;

/**
 * An interface for the map viewer driver.
 */
public interface ViewerDriver extends GUIDriver {
	/**
	 * Center the view on the currently selected tile.
	 */
	void center();

	/**
	 * Zoom in.
	 */
	void zoomIn();

	/**
	 * Zoom out.
	 */
	void zoomOut();

	/**
	 * Reset the zoom level.
	 */
	void resetZoom();
}
