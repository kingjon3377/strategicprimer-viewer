package util;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.DuplicateIDException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import view.util.SystemOut;

/**
 * A class to log warnings.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
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
	@SuppressWarnings("NonFinalFieldInEnum")
	private Consumer<Exception> customHandle = wrapHandler(SystemOut.SYS_OUT::println);
	/**
	 * Default handler for Custom.
	 * @param handler a method handle that does something with a String
	 * @return a method handle that applies it to the message of any given exception.
	 */
	public static Consumer<Exception> wrapHandler(final Consumer<String> handler) {
		return warning -> {
			if (warning instanceof SPFormatException) {
				handler.accept("SP format warning: " + warning.getLocalizedMessage());
			} else {
				handler.accept("Warning: " + warning.getLocalizedMessage());
			}
		};
	}
	/**
	 * In Custom, set the custom printing method. In others, throw.
	 * @param printer the printing method to use
	 */
	public void setCustomPrinter(final Consumer<Exception> printer) {
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
			throw new FatalWarningException(warning);
		case Warn:
			final Class<?> warnClass;
			if (warning.getStackTrace().length > 0) {
				warnClass = StackTraceElement.class;
			} else {
				warnClass = Warning.class;
			}
			final Logger logger = TypesafeLogger.getLogger(warnClass);
			if ((warning instanceof SPFormatException) ||
						(warning instanceof DuplicateIDException)) {
				logger.warning("Warning: " + warning.getMessage());
			} else {
				logger.log(Level.WARNING, "Warning: ", warning);
			}
			break;
		case Ignore:
			break;
		case Custom:
			customHandle.accept(warning);
			break;
		default:
			TypesafeLogger.getLogger(Warning.class)
					.warning("Got to default case in Warning switch");
			Warn.warn(warning);
		}
	}
	/**
	 * The default level. This is provided so that it can in theory be changed later in
	 * one place rather than everywhere.
	 */
	public static final Warning DEFAULT = Warn;
}
