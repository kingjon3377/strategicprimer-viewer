package util;

import java.util.logging.Level;
import java.util.logging.Logger;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A class to log warnings.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
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
public class Warning {
	/**
	 * An instance.
	 */
	public static final Warning INSTANCE = new Warning();

	/**
	 * How we should deal with warnings.
	 */
	private final Action state;

	/**
	 * An enumeration of possible states.
	 */
	public static enum Action {
		/**
		 * Don't do anything with a warning.
		 */
		Ignore,
		/**
		 * Default: Log each warning, but let them pass.
		 */
		Warn,
		/**
		 * Treat warnings as errors: Throw them as runtime exceptions.
		 */
		Die;
	}

	/**
	 * Constructor.
	 *
	 * @param action what action to take with each warning
	 */
	public Warning(final Action action) {
		state = action;
	}

	/**
	 * Constructor. Only warn on warnings.
	 */
	public Warning() {
		this(Action.Warn);
	}

	/**
	 * Log a warning, e.g. if a particular map-format construct is deprecated.
	 *
	 * @param warning the warning
	 */
	public void warn(final Exception warning) {
		switch (state) {
		case Die:
			throw new FatalWarningException(warning); // NOPMD
		case Warn:
			final Class<?> warnClass;
			if (warning.getStackTrace().length > 0) {
				warnClass = StackTraceElement.class;
			} else {
				warnClass = Warning.class;
			}
			final Logger logger = TypesafeLogger.getLogger(warnClass);
			if (warning instanceof SPFormatException
					|| warning instanceof IDFactory.DuplicateIDException) {
				logger.warning("Warning: " + warning.getMessage());
			} else {
				logger.log(Level.WARNING, "Warning: ", warning);
			}
			break;
		case Ignore:
			break;
		default:
			throw new IllegalStateException(
					"Default case of an enum-switch that isn't missing any cases");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "Warning";
	}
}
