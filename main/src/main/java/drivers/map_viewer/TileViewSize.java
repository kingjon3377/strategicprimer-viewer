package drivers.map_viewer;

/**
 * An encapsulation of the {@link scaleZoom} method.
 *
 * TODO: Move into some class ... which?
 */
/* package */ class TileViewSize {
	private TileViewSize() {}

	/**
	 * How big, in pixels, the GUI representation of a tile should be at
	 * the specified zoom level.
	 *
	 * TODO: Even better zoom support
	 *
	 * TODO: tests
	 */
	public static int scaleZoom(int zoomLevel, int mapVersion) {
		switch (mapVersion) {
		case 1:
			return zoomLevel * 2;
		case 2:
			return zoomLevel * 3;
		default:
			throw new IllegalArgumentException("Unsupported map version number");
		}
	}
}
