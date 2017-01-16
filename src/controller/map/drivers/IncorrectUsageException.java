package controller.map.drivers;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * An exception to throw when a driver fails because the user tried to use it improperly.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class IncorrectUsageException extends DriverFailedException {
	/**
	 * An object describing the correct usage.
	 */
	private final IDriverUsage usage;

	/**
	 * Constructor.
	 *
	 * @param correctUsage the object describing the correct usage of the driver
	 */
	public IncorrectUsageException(final IDriverUsage correctUsage) {
		super("Incorrect usage", new IllegalArgumentException("Incorrect usage"));
		usage = correctUsage;
	}

	/**
	 * The usage object for the driver.
	 * @return an object describing the correct usage of the driver
	 */
	public final IDriverUsage getCorrectUsage() {
		return usage;
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
