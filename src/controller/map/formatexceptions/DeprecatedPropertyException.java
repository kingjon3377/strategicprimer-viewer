package controller.map.formatexceptions;

/**
 * A custom exception for cases where one property is deprecated in favor of
 * another.
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
public final class DeprecatedPropertyException extends SPFormatException {
	/**
	 * The current tag.
	 */
	private final String context;

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
	public final String getTag() {
		return context;
	}

	/**
	 * @return the old name for the property.
	 */
	public final String getOld() {
		return old;
	}

	/**
	 * @return the preferred orm.
	 */
	public final String getPreferred() {
		return preferred;
	}

	/**
	 * @param tag the current tag
	 * @param deprecated the old form
	 * @param newForm the preferred form
	 * @param line where this occurred
	 */
	public DeprecatedPropertyException(final String tag,
			final String deprecated, final String newForm, final int line) {
		super("Use of the property '" + deprecated + "' in tag '" + tag
				+ "' is deprecated; use '" + newForm + "' instead", line);
		context = tag;
		old = deprecated;
		preferred = newForm;
	}
}
