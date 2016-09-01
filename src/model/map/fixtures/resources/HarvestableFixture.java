package model.map.fixtures.resources;

import model.map.HasMutableImage;
import model.map.TileFixture;

/**
 * A (for now marker) interface for fixtures that can have resources harvested, mined,
 * etc., from them. TODO: what methods should this have?
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
public interface HarvestableFixture extends TileFixture, HasMutableImage {
	// Marker interface
}
