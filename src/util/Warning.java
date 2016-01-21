package util;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.DuplicateIDException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to log warnings.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
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
public enum Warning {
	/**
	 * Don't do anything with warnings.
	 */
	Ignore,
	/**
	 * Log each warning, but let them pass.
	 */
	Warn,
	/**
	 * Default to sending the warning to stderr, but allows the user to set a custom
	 * output method.
	 */
	Custom,
	/**
	 * Treat warnings as errors.
	 */
	Die;
	/**
	 * The output stream to log to. Used only by Custom.
	 */
	private Consumer<String> customHandle = System.out::println;
	/**
	 * In Custom, set the custom printing method. In others, throw.
	 * @param printer the printing method to use
	 */
	public void setCustomPrinter(final Consumer<String> printer) {
		if (this == Custom) {
			customHandle = printer;
		} else {
			throw new IllegalStateException("Custom printer is only valid for Custom");
		}
	}
	/**
	 * Handle a warning, e.g. if a particular map-format construct is deprecated.
	 * @param warning the warning
	 */
	public void warn(final Exception warning) {
		switch (this) {
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
			if ((warning instanceof SPFormatException)
					    || (warning instanceof DuplicateIDException)) {
				logger.warning("Warning: " + warning.getMessage());
			} else {
				logger.log(Level.WARNING, "Warning: ", warning);
			}
			break;
		case Ignore:
			break;
		case Custom:
			// TODO: Should this switch be outside, and the handle take the Exception?
			if (warning instanceof SPFormatException) {
				customHandle.accept("SP format warning: " + warning.getLocalizedMessage());
			} else {
				customHandle.accept("Warning: " + warning.getLocalizedMessage());
			}
		}
	}
	/**
	 * The default level. This is provided so that it can in theory be changed later in
	 * one place rather than everywhere.
	 */
	public static final Warning DEFAULT = Warn;
}
