package util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to remove the "taint" of null from values.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("UtilityClassCanBeEnum")
public final class NullCleaner {
	/**
	 * Do not instantiate.
	 */
	private NullCleaner() {
		// Static-only class.
	}

	/**
	 * Assert that a value isn't null.
	 *
	 * @param <T> the type of the value
	 * @param val the value
	 * @return it, if it isn't null.
	 */
	public static <@NonNull T> T assertNotNull(@Nullable final T val) {
		assert val != null;
		return val;
	}

}
