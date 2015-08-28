package controller.map.formatexceptions;

/**
 * A custom exception for not-yet-supported tags.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
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
public class UnsupportedTagException extends SPFormatException {
	/**
	 * The tag.
	 */
	private final String tag;

	/**
	 * @return the tag.
	 */
	public final String getTag() {
		return tag;
	}

	/**
	 * @param uTag the unsupported tag
	 * @param line the line it's on
	 */
	public UnsupportedTagException(final String uTag, final int line) {
		super("Unexpected tag " + uTag
				+ "; probably a more recent map format than viewer", line);
		tag = uTag;
	}
}
