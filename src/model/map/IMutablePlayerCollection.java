package model.map;
/**
 * An interface for player collections that can be modified.
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
 */
public interface IMutablePlayerCollection extends IPlayerCollection {

	/**
	 * Add a player to the collection.
	 *
	 * @param player the player to add
	 * @return whether the collection was changed by the operation.
	 */
	boolean add(Player player);

	/**
	 * Remove an object from the collection.
	 *
	 * @param obj an object
	 * @return true if it was removed as a result of this call
	 */
	boolean remove(Object obj);
	/**
	 * @param zero whether to "zero" sensitive details: should probably be ignored
	 * @return a copy of this collection
	 */
	@Override
	IMutablePlayerCollection copy(boolean zero);

}
