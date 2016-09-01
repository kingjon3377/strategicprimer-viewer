package util;

import java.util.Map;

/**
 * A Map that actually executes the removal of elements only when the coalesce() method is
 * called.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @param <K> the first type parameter
 * @param <V> the second type parameter
 */
public interface PatientMap<K, V> extends Map<K, V> {

	/**
	 * Apply all scheduled removals.
	 */
	void coalesce();
}
