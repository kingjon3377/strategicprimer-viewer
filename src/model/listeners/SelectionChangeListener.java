package model.listeners;

import java.util.EventListener;

import org.eclipse.jdt.annotation.Nullable;

import model.map.Point;

/**
 * An interface for objects that want to know when the selected tile, or its
 * location, changes.
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
 */
@FunctionalInterface
public interface SelectionChangeListener extends EventListener {
	/**
	 * The selected tile's location changed.
	 *
	 * @param old the previously selected location
	 * @param newPoint the newly selected location
	 */
	void selectedPointChanged(@Nullable  Point old,  Point newPoint);
}
