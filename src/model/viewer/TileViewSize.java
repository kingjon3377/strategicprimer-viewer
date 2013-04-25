package model.viewer;

import java.io.Serializable;

/**
 * A class to encapsulate how big the GUI representation of a tile should be.
 * Now suppoting zooming in and out (changing the size to view more tiles or see
 * the tiles more clearly, not changing what's on them yet).
 *
 * TODO: Even better zoom support.
 *
 * TODO: tests
 *
 * @author Jonathan Lovelace
 *
 */
public class TileViewSize implements Serializable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Scale the specified zoom level for the specified map version.
	 * @param zoom the zoom level
	 * @param ver the map version
	 * @return the size of a tile in that version at that zoom level.
	 */
	public static int scaleZoom(final int zoom, final int ver) {
		if (ver == 1) {
			return zoom * 2; // NOPMD
		} else if (ver == 2) {
			return zoom * 3;
		} else {
			throw new IllegalArgumentException("Unknown version");
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TileViewSize";
	}
}
