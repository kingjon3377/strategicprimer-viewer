package view.util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JComponent;
import util.ActionWrapper;
import util.NullCleaner;

/**
 * An action to request focus in a component.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings({"CloneableClassInSecureContext", "CloneableClassWithoutClone"})
public class FocusRequester extends ActionWrapper {
	/**
	 * The type of component we're handling.
	 */
	private final String type;

	/**
	 * Constructor.
	 *
	 * @param component The component to request focus in.
	 */
	public FocusRequester(final JComponent component) {
		super(evt -> component.requestFocusInWindow());
		type = NullCleaner.assertNotNull(component.getClass().getSimpleName());
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * @return a String representation of the action
	 */
	@Override
	public String toString() {
		return "Requesting focus in a " + type;
	}
}
