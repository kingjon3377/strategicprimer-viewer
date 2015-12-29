package controller.map.formatexceptions;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * An exception for cases where a parameter is required (or, if this is merely logged,
 * recommended) but missing.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
public final class MissingPropertyException extends SPFormatException {

	/**
	 * The current tag.
	 */
	private final QName context;
	/**
	 * The missing parameter.
	 */
	private final String param;

	/**
	 * @param tag       the current tag
	 * @param parameter the missing parameter
	 * @param cause the underlying cause
	 */
	public MissingPropertyException(final StartElement tag, final String parameter,
	                                final Throwable cause) {
		super("Missing parameter " + parameter + " in tag " + tag.getName(),
				tag.getLocation(), cause);
		context = tag.getName();
		param = parameter;
	}
	/**
	 * @param tag       the current tag
	 * @param parameter the missing parameter
	 */
	public MissingPropertyException(final StartElement tag, final String parameter) {
		super("Missing parameter " + parameter + " in tag " + tag.getName(), tag.getLocation());
		context = tag.getName();
		param = parameter;
	}
	/**
	 * @return the current tag
	 */
	public QName getTag() {
		return context;
	}

	/**
	 * @return the missing parameter
	 */
	public String getParam() {
		return param;
	}
}
