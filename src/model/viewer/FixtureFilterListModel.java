package model.viewer;

import model.map.TileFixture;
import util.NullCleaner;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The data model for a FixtureFilterList.
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
public final class FixtureFilterListModel extends
		AbstractListModel<Class<? extends TileFixture>> {
	/**
	 * The list backing this model.
	 */
	private final List<Class<? extends TileFixture>> backing = new ArrayList<>();

	/**
	 * @return the size of the list
	 */
	@Override
	public int getSize() {
		return backing.size();
	}

	/**
	 * @param index an index in the list
	 * @return the element there
	 */
	@Override
	public Class<? extends TileFixture> getElementAt(final int index) {
		return NullCleaner.assertNotNull(backing.get(index));
	}

	/**
	 * @param item an item in the list
	 * @return its index
	 */
	public int indexOf(final Class<? extends TileFixture> item) {
		return backing.indexOf(item);
	}

	/**
	 * @param item an item to add to the list
	 */
	public void add(final Class<? extends TileFixture> item) {
		backing.add(item);
		fireIntervalAdded(item.getClass(), getSize() - 1, getSize() - 1);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final String listString = backing.toString();
		final StringBuilder builder = new StringBuilder(25 + listString.length());
		builder.append("FixtureFilterListModel: ");
		builder.append(listString);
		return NullCleaner.assertNotNull(builder.toString());
	}
}
