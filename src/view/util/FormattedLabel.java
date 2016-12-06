package view.util;

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
}
