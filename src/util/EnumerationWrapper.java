package util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A wrapper around an Enumeration to make it fit the Iterable interface.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the type parameter
 * @author Jonathan Lovelace
 */
public final class EnumerationWrapper<@NonNull T> implements Iterator<@NonNull T> {
	/**
	 * The object we're wrapping.
	 */
	private final Enumeration<T> wrapped;

	/**
	 * Constructor.
	 * @param wrappedEnumeration the object we're wrapping.
	 */
	public EnumerationWrapper(final Enumeration<T> wrappedEnumeration) {
		wrapped = wrappedEnumeration;
	}

	/**
	 * Whether there are any more elements.
	 * @return whether there are more elements
	 */
	@Override
	public boolean hasNext() {
		return wrapped.hasMoreElements();
	}

	/**
	 * Get the next element.
	 * @return the next element
	 * @throws NoSuchElementException if no more elements; required by superclass
	 */
	@SuppressWarnings(
			{"IteratorNextCanNotThrowNoSuchElementException", "ThrowsRuntimeException"})
	@Override
	public T next()
			throws NoSuchElementException { // @throws required by superclass
		return wrapped.nextElement();
	}

	/**
	 * Not supported.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported by Enumeration");
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "EnumerationWrapper";
	}
}
