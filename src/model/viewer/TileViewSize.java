package model.viewer;


/**
 * A class to encapsulate how big the GUI representation of a tile should be. Now
 * supporting zooming in and out (changing the size to view more tiles or see the tiles
 * more clearly, not changing what's on them yet).
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 * TODO: Even better zoom support.
 *
 * TODO: tests
 */
public final class TileViewSize {
	/**
	 * Scale the specified zoom level for the specified map version.
	 *
	 * @param zoom the zoom level
	 * @param ver  the map version
	 * @return the size of a tile in that version at that zoom level.
	 */
	public static int scaleZoom(final int zoom, final int ver) {
		if (ver == 1) {
			return zoom * 2;
		} else if (ver == 2) {
			return zoom * 3;
		} else {
			throw new IllegalArgumentException("Unknown version");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TileViewSize";
	}
}
