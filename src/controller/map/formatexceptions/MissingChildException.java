package controller.map.formatexceptions;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * A custom exception for when a tag (or a Node) requuires a child and it isn't there.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
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
public final class MissingChildException extends SPFormatException {
	/**
	 * The current tag.
	 */
	private final QName context;

	/**
	 * @return the current tag.
	 */
	public QName getTag() {
		return context;
	}

	/**
	 * @param tag  the current tag (the one that needs a child)
	 */
	public MissingChildException(final StartElement tag) {
		super("Tag " + tag.getName().getLocalPart() + " missing a child", tag.getLocation());
		context = tag.getName();
	}
}
