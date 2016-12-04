package model.map.fixtures.mobile;

/**
 * An interface for 'proxy' implementations.
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
 * @param <T> the type being proxied
 * @author Jonathan Lovelace
 */
public interface ProxyFor<T> /* extends T */ {
	/**
	 * Add another object to be proxied.
	 *
	 * @param item the object to be proxied.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addProxied(T item);

	/**
	 * This should probably only ever be used in tests.
	 *
	 * @return the proxied items.
	 */
	Iterable<T> getProxied();

	/**
	 * @return Whether this should be considered (if true) a proxy for multiple
	 * representations of the same T, e.g. in different maps, or (if false) a proxy for
	 * different related Ts.
	 */
	boolean isParallel();
}
