package model.map;
/**
 * An interface for a collection of tiles.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated the old map API is deprecated in this branch
 */
@Deprecated
public interface ITileCollection extends Iterable<Point>,
		Subsettable<ITileCollection> {

	/**
	 * Get the specified point. If it isn't in the collection, add a new "empty"
	 * one there and return that. This should never return null.
	 *
	 * @param point a point
	 *
	 * @return the tile at that point, or a new "empty" tile at that point.
	 */
	ITile getTile(Point point);

	/**
	 * @param point a point
	 * @return whether there's a non-empty tile at that point
	 */
	boolean hasTile(Point point);

}
