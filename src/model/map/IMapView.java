package model.map;

/**
 * An interface for map-views.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated the old API is deprecated in this branch
 */
@Deprecated
public interface IMapView extends IMap {

	/**
	 * @return the current turn
	 */
	int getCurrentTurn();

	/**
	 * TODO: How does this interact with changesets? This is primarily used
	 * (should probably *only* be used) in serialization.
	 *
	 * @return the map this wraps
	 */
	IMap getMap();

	/**
	 * FIXME: Add tests to ensure that a zeroed map is still a subset, and a
	 * non-zeroed map is still equal.
	 *
	 * @return a copy of this map view
	 * @param zero
	 *            whether to "zero" sensitive data (probably just DCs)
	 */
	@Override
	IMapView copy(boolean zero);

}
