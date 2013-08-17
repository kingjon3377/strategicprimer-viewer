package view.util;

/**
 * A wrapper to reduce the number of arguments a TileDrawHelper has to take.
 *
 * @author Jonathan Lovelace
 *
 */
public class Coordinate {
	/**
	 * Constructor.
	 *
	 * @param xCoord the X coordinate or width
	 * @param yCoord the Y coordinate or height
	 */
	public Coordinate(final int xCoord, final int yCoord) {
		x = xCoord;
		y = yCoord;
	}
	/**
	 * The X coordinate or width.
	 */
	public final int x; // NOPMD
	/**
	 * The Y coordinate or height.
	 */
	public final int y; // NOPMD
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "[" + x + ", " + y + "]";
	}
}
