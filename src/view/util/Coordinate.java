package view.util;

/**
 * A wrapper to reduce the number of arguments a TileDrawHelper has to take.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2013 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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
		return '[' + Integer.toString(x) + ", " + Integer.toString(y) + ']';
	}
}
