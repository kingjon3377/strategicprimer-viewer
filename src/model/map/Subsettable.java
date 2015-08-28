package model.map;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;


/**
 * An interface to let us check converted player maps against the main map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
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
 * @param <T> The type itself.
 */
public interface Subsettable<@NonNull T> {
	/**
	 * @param obj
	 *            an object
	 * @return whether it is a strict subset of this object---with no members
	 *         that aren't also in this.
	 * @param ostream
	 *            the stream to write details to
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context; it should be passed through and appended to. Whenever
	 *            it is put onto ostream, it should probably be followed by a
	 *            tab.
	 * @throws IOException
	 *             on I/O error writing output to the stream
	 */
	boolean isSubset(T obj, Appendable ostream, String context) throws IOException;
}
