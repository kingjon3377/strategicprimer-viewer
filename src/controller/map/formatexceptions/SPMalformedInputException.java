package controller.map.formatexceptions;
/**
 * For cases of malformed input where we can't use XMLStreamException.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2014-2014 Jonathan Lovelace
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
public class SPMalformedInputException extends SPFormatException {
	/**
	 * Constructor.
	 * @param line where this occurred
	 */
	public SPMalformedInputException(final int line) {
		super("Malformed input", line);
	}
	/**
	 * @param line where this occurred
	 * @param cause the underlying exception
	 */
	public SPMalformedInputException(final int line, final Throwable cause) {
		super("Malformed input", line, cause);
	}
}
