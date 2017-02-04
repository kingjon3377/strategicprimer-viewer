package view.util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JLabel;

/**
 * Combines JLabel with String.format().
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
public class FormattedLabel extends JLabel {
	/**
	 * The format string to use.
	 */
	private final String formatString;
	/**
	 * Constructor.
	 * @param format the format string to use
	 * @param args initial parameters
	 */
	public FormattedLabel(final String format, final Object... args) {
		super(String.format(format, args));
		formatString = format;
	}
	/**
	 * Set the text using the same format string but new parameters.
	 * @param args arguments to format using the original format string
	 */
	public void setArgs(final Object... args) {
		setText(String.format(formatString, args));
	}
	/**
	 * A simple toString().
	 */
	@Override
	public String toString() {
		return "FormattedLabel with format " + formatString;
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
