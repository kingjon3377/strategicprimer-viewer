package controller.map.drivers;

/**
 * An exception to throw when a driver fails because the user tried to use it improperly.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
public class IncorrectUsageException extends DriverFailedException {
	/**
	 * Constructor.
	 * @param correctUsage the object describing the correct usage of the driver
	 */
	public IncorrectUsageException(final DriverUsage correctUsage) {
		super("Incorrect usage", new IllegalArgumentException("Incorrect usage"));
		usage = correctUsage;
	}
	/**
	 * An object describing the correct usage.
	 */
	private final DriverUsage usage;
	/**
	 * @return an object describing the correct usage of the driver
	 */
	public final DriverUsage getCorrectUsage() {
		return usage;
	}
}
