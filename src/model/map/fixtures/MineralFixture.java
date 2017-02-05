package model.map.fixtures;

import model.map.HasKind;
import model.map.TileFixture;

/**
 * An (at present marker) interface for tile fixtures representing some kind of
 * "mineral" (as opposed to organic) resource or thing: bare rock or the ground, stone
 * deposit, mineral vein, mine, etc.
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
public interface MineralFixture extends TileFixture, HasKind {
	// TODO: any members?
}
