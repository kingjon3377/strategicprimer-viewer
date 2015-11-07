package util;

import java.util.logging.Logger;

/**
 * A class to get Loggers and guarantee that they are not null, since the JDK
 * isn't annotated yet.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class TypesafeLogger {
	/**
	 * Do not instantiate.
	 */
	private TypesafeLogger() {
		// Do not instantiate.
	}

	/**
	 * @param type the class that will be calling the logger
	 * @return the logger produced by {@link Logger#getLogger(String)}.
	 */
	public static Logger getLogger(final Class<?> type) {
		final Logger retval = Logger.getLogger(type.getName());
		if (retval == null) {
			throw new IllegalStateException("Logger was null");
		} else {
			return retval;
		}
	}
}
