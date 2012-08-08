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
	 * Factory method.
	 * @param xCoord the X coordinate or width
	 * @param yCoord the Y coordinate or height
	 * @return an instance containing those coordinates.
	 */
	public static Coordinate factory(final int xCoord, final int yCoord) {
		return new Coordinate(xCoord, yCoord);
	}
	/**
	 * The X coordinate or width.
	 */
	public final int x; // NOPMD
	/**
	 * The Y coordinate or height.
	 */
	public final int y; // NOPMD
}
