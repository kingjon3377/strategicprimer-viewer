package controller.map.formatexceptions;

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
	private final String tag;

	/**
	 * The unwanted child.
	 */
	private final String chld;

	/**
	 * FIXME: Take optional Throwable
	 * @param parent    the current tag
	 * @param child     the unwanted child
	 * @param errorLoc  the location where this happened
	 */
	public UnwantedChildException(final String parent, final String child,
	                              final Location errorLoc) {
		super("Unexpected child " + child + " in tag " + parent, errorLoc);
		tag = parent;
		chld = child;
	}

	/**
	 * @return the current tag.
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @return the unwanted child.
	 */
	public String getChild() {
		return chld;
	}
}
