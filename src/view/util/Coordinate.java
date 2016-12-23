package view.util;

/**
 * A wrapper to reduce the number of arguments a TileDrawHelper has to take.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class Coordinate {
	/**
	 * The X coordinate or width.
	 */
	public final int x;
	/**
	 * The Y coordinate or height.
	 */
	public final int y;

	/**
	 * Constructor.
	 *
	 * @param xCoordinate the X coordinate or width
	 * @param yCoordinate the Y coordinate or height
	 */
	public Coordinate(final int xCoordinate, final int yCoordinate) {
		x = xCoordinate;
		y = yCoordinate;
	}

	/**
	 * The format is "[x, y]".
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return String.format("[%d, %d]", Integer.valueOf(x), Integer.valueOf(y));
	}
}
