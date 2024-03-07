package drivers.map_viewer;

/**
 * An encapsulation of the {@link #scaleZoom} method.
 *
 * TODO: Move into some class ... which?
 */
/* package */ final class TileViewSize {
	private TileViewSize() {
	}

	/**
	 * How big, in pixels, the GUI representation of a tile should be at
	 * the specified zoom level.
	 *
	 * TODO: Even better zoom support
	 *
	 * TODO: tests
	 */
	public static int scaleZoom(final int zoomLevel, final int mapVersion) {
		return switch (mapVersion) {
			case 1 -> zoomLevel * 2;
			case 2 -> zoomLevel * 3;
			default -> throw new IllegalArgumentException("Unsupported map version number");
		};
	}
}
