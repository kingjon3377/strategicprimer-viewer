package controller.map.formatexceptions;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

/**
 * A custom exception for when a tag has a child tag it can't handle.
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
public final class UnwantedChildException extends SPFormatException {

	/**
	 * The current tag.
	 */
	private final QName tag;

	/**
	 * The unwanted child.
	 */
	private final QName chld;

	/**
	 * @param parent    the current tag
	 * @param child     the unwanted child
	 * @param errorLoc  the location where this happened
	 */
	public UnwantedChildException(final QName parent, final QName child,
	                              final Location errorLoc) {
		super("Unexpected child " + child.getLocalPart() + " in tag " + parent.getLocalPart(), errorLoc);
		tag = parent;
		chld = child;
	}

	/**
	 * @param parent    the current tag
	 * @param child     the unwanted child
	 * @param errorLoc  the location where this happened
	 * @param cause     another exception that caused this one
	 */
	public UnwantedChildException(final QName parent, final QName child,
	                              final Location errorLoc, final Throwable cause) {
		super("Unexpected child " + child.getLocalPart() + " in tag " + parent.getLocalPart(), errorLoc, cause);
		tag = parent;
		chld = child;
	}
	/**
	 * @return the current tag.
	 */
	public QName getTag() {
		return tag;
	}

	/**
	 * @return the unwanted child.
	 */
	public QName getChild() {
		return chld;
	}
}
