package controller.map.formatexceptions;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * A custom exception for cases where one property is deprecated in favor of another.
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
public final class DeprecatedPropertyException extends SPFormatException {
	/**
	 * The current tag.
	 */
	private final QName context;

	/**
	 * The old property.
	 */
	private final String old;

	/**
	 * The preferred form.
	 */
	private final String preferred;

	/**
	 * @return the current tag.
	 */
	public QName getTag() {
		return context;
	}

	/**
	 * @return the old name for the property.
	 */
	public String getOld() {
		return old;
	}

	/**
	 * @return the preferred orm.
	 */
	public String getPreferred() {
		return preferred;
	}

	/**
	 * @param tag        the current tag
	 * @param deprecated the old form
	 * @param newForm    the preferred form
	 */
	public DeprecatedPropertyException(final StartElement tag,
	                                   final String deprecated, final String newForm) {
		super("Use of the property '" + deprecated + "' in tag '" +
				      tag.getName().getLocalPart() + "' is deprecated; use '" + newForm +
				      "' instead", tag.getLocation());
		context = tag.getName();
		old = deprecated;
		preferred = newForm;
	}
}
