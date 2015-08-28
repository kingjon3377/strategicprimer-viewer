package model.map;


/**
 * An interface for the map and any wrappers around it.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
 * @deprecated The old map API is deprecated in the mapng branch
 */
// ESCA-JAVA0237:
@Deprecated
public interface IMap extends Subsettable<IMap>, Comparable<IMap> {
	/**
	 * @return The map's dimensions and version.
	 */
	MapDimensions getDimensions();

	/**
	 * @param point a point
	 * @return the tile at those coordinates
	 */
	ITile getTile(Point point);

	/**
	 *
	 * @return the players in the map
	 */
	IPlayerCollection getPlayers();

	/**
	 * We need this for subset calculations if nothing else.
	 *
	 * @return the collection of tiles.
	 */
	ITileCollection getTiles();
}
