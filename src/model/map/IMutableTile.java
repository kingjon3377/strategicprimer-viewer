package model.map;
/**
 * An interface for modifying, not just querying, the contents of a tile.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
 * @deprecated the old map API is deprecated in this branch
 */
@Deprecated
public interface IMutableTile extends ITile {
	/**
	 * @param fix something new on the tile
	 * @return true iff it was not already in the set.
	 */
	boolean addFixture(TileFixture fix);

	/**
	 * @param fix something to remove from the tile
	 * @return the result of the operation
	 */
	boolean removeFixture(TileFixture fix);

	/**
	 * @param river a river to add
	 */
	void addRiver(River river);

	/**
	 * @param river a river to remove
	 */
	void removeRiver(River river);

	/**
	 * @param ttype the tile's new terrain type
	 */
	void setTerrain(TileType ttype);
}
