package drivers.map_viewer;

/**
 * An (x, y) pair, to reduce the number of arguments to a {@link TileDrawHelper}.
 *
 * TODO: Use a Tuple instead?
 *
 * TODO: Test performance implications of that
 */
public class Coordinate {
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * The X coordinate or width.
	 */
	private final int x;

	/**
	 * The Y coordinate or height.
	 */
	private final int y;

	/**
	 * The X coordinate or width.
	 */
	public int getX() {
		return x;
	}

	/**
	 * The Y coordinate or height.
	 */
	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return String.format("[%d, %d]", x, y);
	}
}
