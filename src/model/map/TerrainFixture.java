package model.map;

/**
 * A marker interface for TileFixtures that are terrain-related and so, if not the top
 * Fixture on the tile, should change the tile's presentation.
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
 */
public interface TerrainFixture extends TileFixture {
	// Marker interface. TODO: Should there be any members?
}
