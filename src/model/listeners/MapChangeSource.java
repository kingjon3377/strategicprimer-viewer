package model.listeners;

/**
 * An interface for things that can fire notifications of a new map being
 * loaded.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
 *
 */
public interface MapChangeSource {
	/**
	 * Add a MapChangeListener.
	 *
	 * @param list the listener to add
	 */
	void addMapChangeListener(MapChangeListener list);

	/**
	 * Remove a MapChangeListener.
	 *
	 * @param list the listener to remove
	 */
	void removeMapChangeListener(MapChangeListener list);
}
