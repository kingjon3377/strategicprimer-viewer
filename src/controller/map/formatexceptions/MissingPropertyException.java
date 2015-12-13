package controller.map.formatexceptions;

/**
 * An exception for cases where a parameter is required (or, if this is merely
 * logged, recommended) but missing.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MissingPropertyException extends SPFormatException {

	/**
	 * The current tag.
	 */
	private final String context;
	/**
	 * The missing parameter.
	 */
	private final String param;

	/**
	 * @param tag the current tag
	 * @param parameter the missing parameter
	 * @param errorLine the line where this occurred
	 */
	public MissingPropertyException(final String tag, final String parameter,
			final int errorLine) {
		super("Missing parameter " + parameter + " in tag " + tag, errorLine);
		context = tag;
		param = parameter;
	}

	/**
	 * @return the current tag
	 */
	public final String getTag() {
		return context;
	}

	/**
	 * @return the missing parameter
	 */
	public final String getParam() {
		return param;
	}
}
