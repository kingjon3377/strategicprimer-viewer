package model.map.fixtures.towns;

import model.map.HasName;
import model.map.HasOwner;
import model.map.Player;
import model.map.TileFixture;

/**
 * An interface for towns and similar fixtures. Needed because we don't want
 * fortresses and villages to be Events.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
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
 * @author Jonathan Lovelace
 *
 */
public interface ITownFixture extends TileFixture, HasName, HasOwner {

	/**
	 * @return the name of the town, fortress, or city.
	 */
	@Override
	String getName();

	/**
	 *
	 * @return the status of the town, fortress, or city
	 */
	TownStatus status();

	/**
	 *
	 * @return the size of the town, fortress, or city
	 */
	TownSize size();

	/**
	 * @return the player that owns the town, fortress, or city
	 */
	@Override
	Player getOwner();

}
