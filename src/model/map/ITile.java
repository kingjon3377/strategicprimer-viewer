package model.map;

import java.util.Iterator;
/**
 * An interface for the contents of a tile.
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
public interface ITile extends FixtureIterable<TileFixture>, Subsettable<ITile> {

	/**
	 * @return the contents of the tile
	 */
	@Override
	Iterator<TileFixture> iterator();

	/**
	 * A tile is "empty" if its tile type is NotVisible and it has no contents.
	 *
	 * @return whether this tile is "empty".
	 */
	boolean isEmpty();

	/**
	 * @return whether there are any rivers on the tile
	 */
	boolean hasRiver();

	/**
	 * Call hasRiver() before this, because this may throw
	 * IllegalStateException if we don't actually contain a river.
	 *
	 * @return the rivers that we contain
	 */
	Iterable<River> getRivers();

	/**
	 * @return the kind of tile this is
	 */
	TileType getTerrain();
}
