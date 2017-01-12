package model.map.fixtures.towns;

import model.map.HasMutableOwner;
import model.map.HasName;
import model.map.HasPortrait;
import model.map.TileFixture;

/**
 * An interface for towns and similar fixtures. Needed because we don't want fortresses
 * and villages to be Events.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface ITownFixture extends TileFixture, HasName, HasMutableOwner, HasPortrait {
	/**
	 * The status of the town.
	 * @return the status of the town, fortress, or city
	 */
	TownStatus status();

	/**
	 * The size of the town.
	 * @return the size of the town, fortress, or city
	 */
	TownSize size();

	/**
	 * What kind of "town" this is.
	 * @return a description of what kind of 'town' this is.
	 */
	String kind();
}
