package model.map;
/**
 * An interface for mutable map-views.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
 * @deprecated the old API is deprecated in this branch
 */
@Deprecated
public interface IMutableMapView extends IMutableMap, IMapView {

	/**
	 * Set the current player.
	 *
	 * @param current the new current player (number)
	 */
	void setCurrentPlayer(int current);

	/**
	 * Set the current turn.
	 *
	 * @param current the new current turn
	 */
	void setCurrentTurn(int current);

}