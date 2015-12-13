package model.viewer;


/**
 * A class to encapsulate how big the GUI representation of a tile should be.
 * Now suppoting zooming in and out (changing the size to view more tiles or see
 * the tiles more clearly, not changing what's on them yet).
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2013 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * TODO: Even better zoom support.
 *
 * TODO: tests
 *
 * @author Jonathan Lovelace
 *
 */
public final class TileViewSize {
	/**
	 * Scale the specified zoom level for the specified map version.
	 *
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
