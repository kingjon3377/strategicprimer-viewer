package model.viewer;
/**
 * A class to encapsulate how big the GUI representation of a tile should be.
 * @author Jonathan Lovelace
 *
 */
public class TileViewSize {
	/**
	 * @param version the map version we're using
	 * @return how big each tile should be
	 */
	public int getSize(final int version) {
		if (version == 1) {
			return 16; // NOPMD
		} else if (version == 2) {
			return 24;
		} else {
			throw new IllegalArgumentException("Unknown version");
		}
	}
}
