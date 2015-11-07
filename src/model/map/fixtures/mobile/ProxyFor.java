package model.map.fixtures.mobile;
/**
 * An interface for 'proxy' implementations.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
 * @param <T> the type being proxied
 */
public interface ProxyFor<T> /* extends T */ {
	/**
	 * Add another object to be proxied.
	 * @param item the object to be proxied.
	 */
	void addProxied(T item);
	/**
	 * This should probably only ever be used in tests.
	 * @return the proxied items.
	 */
	Iterable<T> getProxied();
}
