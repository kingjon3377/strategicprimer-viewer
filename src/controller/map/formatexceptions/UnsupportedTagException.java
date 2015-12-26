package controller.map.formatexceptions;

import javax.xml.stream.Location;

/**
 * A custom exception for not-yet-supported tags.
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
public final class UnsupportedTagException extends SPFormatException {
	/**
	 * The tag.
	 */
	private final String tag;

	/**
	 * @return the tag.
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param uTag the unsupported tag
	 * @param location where it occurred
	 */
	public UnsupportedTagException(final String uTag, final Location location) {
		super("Unexpected tag " + uTag
				      + "; probably a more recent map format than viewer", location);
		tag = uTag;
	}
}
