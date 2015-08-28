package controller.map.formatexceptions;

/**
 * A custom exception for XML format errors.
 *
 * TODO: Take Location rather than int for the location in the XML.
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class SPFormatException extends Exception {
	/**
	 * The line of the XML file containing the mistake.
	 */
	private final int line;

	/**
	 *
	 * @return the line of the XML file containing the mistake
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Constructor.
	 *
	 * @param message a message describing what's wrong with the XML.
	 * @param errorLine the line containing the error.
	 */
	protected SPFormatException(final String message, final int errorLine) {
		super("Incorrect SP XML at line " + errorLine + ": " + message);
		line = errorLine;
	}
	/**
	 * @param message a message describing what's wrong with the XML
	 * @param errorLine the line containing the error
	 * @param cause the "initial cause" of this
	 */
	protected SPFormatException(final String message, final int errorLine,
			final Throwable cause) {
		super("Incorrect SP XML at line " + errorLine + ": " + message, cause);
		line = errorLine;
	}
}
