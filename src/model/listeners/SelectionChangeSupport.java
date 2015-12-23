package model.listeners;

import java.util.ArrayList;
import java.util.Collection;
import model.map.Point;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A helper class to proxy selection-changing calls.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SelectionChangeSupport implements SelectionChangeSource {
	/**
	 * The list of listeners to notify.
	 */
	private final Collection<SelectionChangeListener> listeners = new ArrayList<>();

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addSelectionChangeListener(final SelectionChangeListener list) {
		listeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeSelectionChangeListener(final SelectionChangeListener list) {
		listeners.remove(list);
	}

	/**
	 * Tell all listeners about a change. All in one like this rather than implementing
	 * {@link SelectionChangeListener} to prevent accidental infinite recursion.
	 *
	 * @param oldPoint the previously selected location
	 * @param newPoint the newly selected location.
	 */
	public void fireChanges(@Nullable final Point oldPoint,
	                        final Point newPoint) {
		for (final SelectionChangeListener list : listeners) {
			list.selectedPointChanged(oldPoint, newPoint);
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SelectionChangeSupport";
	}
}
