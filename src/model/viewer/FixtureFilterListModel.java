package model.viewer;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractListModel;
import util.NullCleaner;

/**
 * The data model for a FixtureFilterList.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class FixtureFilterListModel extends
		AbstractListModel<FixtureMatcher> implements Iterable<FixtureMatcher> {
	/**
	 * The list backing this model.
	 */
	private final List<FixtureMatcher> backing = new ArrayList<>();

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
	public FixtureMatcher getElementAt(final int index) {
		return NullCleaner.assertNotNull(backing.get(index));
	}

	/**
	 * @param item an item in the list
	 * @return its index
	 */
	public int indexOf(final FixtureMatcher item) {
		return backing.indexOf(item);
	}

	/**
	 * @param item an item to add to the list
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public void add(final FixtureMatcher item) {
		backing.add(item);
		fireIntervalAdded(item, getSize() - 1, getSize() - 1);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final String listString = backing.toString();
		return String.format("FixtureFilterListModel: %s", listString);
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * @return an iterator over the list's contents
	 */
	@Override
	public Iterator<FixtureMatcher> iterator() {
		return backing.iterator();
	}
}
