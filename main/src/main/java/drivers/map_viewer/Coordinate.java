package drivers.map_viewer;

/**
 * An (x, y) pair, to reduce the number of arguments to a {@link TileDrawHelper}.
 *
 * TODO: Use a Tuple instead?
 *
 * TODO: Test performance implications of that
 *
 * @param x The X coordinate or width.
 * @param y The Y coordinate or height.
 */
public record Coordinate(int x, int y) {

	/**
	 * The X coordinate or width.
	 */
	@Override
	public int x() {
		return x;
	}

	/**
	 * The Y coordinate or height.
	 */
	@Override
	public int y() {
		return y;
	}

	@Override
	public String toString() {
		return "[%d, %d]".formatted(x, y);
	}
}
