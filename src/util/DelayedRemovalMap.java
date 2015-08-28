package util;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A Map that actually executes the removal of elements only when the coalesce()
 * method is called.
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 * @param <K> the first type parameter
 * @param <V> the second type parameter
 */
public interface DelayedRemovalMap<@NonNull K, V> extends Map<@NonNull K, V> {

	/**
	 * Apply all scheduled removals.
	 */
	void coalesce();
}
