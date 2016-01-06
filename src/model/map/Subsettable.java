package model.map;

import java.io.IOException;
import java.util.Objects;
import org.eclipse.jdt.annotation.Nullable;


/**
 * An interface to let us check converted player maps against the main map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
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
 * @param <T> The type itself.
 * @author Jonathan Lovelace
 */
@FunctionalInterface
public interface Subsettable<T> {
	/**
	 * @param obj     an object
	 * @param ostream the stream to write details to
	 * @param context a string to print before every line of output, describing the
	 *                context; it should be passed through and appended to. Whenever
	 *                it is
	 *                put onto ostream, it should probably be followed by a tab.
	 * @return whether it is a strict subset of this object---with no members that aren't
	 * also in this.
	 * @throws IOException on I/O error writing output to the stream
	 */
	boolean isSubset(T obj, Appendable ostream, String context) throws IOException;

	/**
	 * A helper method to compare two items and, if they're not equal, report this to
	 * the stream.
	 * @param ostream the stream to write to
	 * @param messages messages to write if the two aren't equal
	 * @param first the first item
	 * @param second the second item
	 * @return whether the two items are equal
	 * @throws IOException on error writing to the stream
	 */
	default boolean areItemsEqual(final Appendable ostream, @Nullable final Object first,
	                              @Nullable final Object second, final String... messages)
			throws IOException {
		if (Objects.equals(first, second)) {
			return true;
		} else {
			for (final String message : messages) {
				ostream.append(message);
			}
			return false;
		}
	}
	/**
	 * A helper method to compare two items and, if they're not equal, report this to
	 * the stream.
	 * @param ostream the stream to write to
	 * @param messages messages to write if the two aren't equal
	 * @param first the first item
	 * @param second the second item
	 * @return whether the two items are equal
	 * @throws IOException on error writing to the stream
	 */
	default boolean areItemsEqual(final Appendable ostream, final boolean first,
	                              final boolean second, final String... messages)
			throws IOException {
		if (first == second) {
			return true;
		} else {
			for (final String message : messages) {
				ostream.append(message);
			}
			return false;
		}
	}
	/**
	 * A helper method to compare two items and, if they're not equal, report this to
	 * the stream.
	 * @param ostream the stream to write to
	 * @param messages messages to write if the two aren't equal
	 * @param first the first item
	 * @param second the second item
	 * @return whether the two items are equal
	 * @throws IOException on error writing to the stream
	 */
	default boolean areIntItemsEqual(final Appendable ostream, final int first,
	                                 final int second, final String... messages)
			throws IOException {
		if (first == second) {
			return true;
		} else {
			for (final String message : messages) {
				ostream.append(message);
			}
			return false;
		}
	}
	/**
	 * A helper method to report a message to the stream if a condition isn't true.
	 * @param ostream the stream to write to
	 * @param condition the condition to check
	 * @param messages the messages to write if it isn't true
	 * @return whether it's true
	 * @throws IOException on error writing to the stream
	 */
	default boolean isConditionTrue(final Appendable ostream, final boolean condition,
	                                final String... messages) throws IOException {
		if (condition) {
			return true;
		} else {
			for (final String message : messages) {
				ostream.append(message);
			}
			return false;
		}
	}
}
