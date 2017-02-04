package model.map;

/**
 * An encapsulation of a map's dimensions (and its map version as well).
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2017 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface MapDimensions {
	/**
	 * The number of rows in the map.
	 * @return The number of rows in the map.
	 */
	int getRows();

	/**
	 * The number of columns in the map.
	 * @return The number of columns in the map.
	 */
	int getColumns();

	/**
	 * The map version.
	 * @return The map version.
	 */
	int getVersion();
}
