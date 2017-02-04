package util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.DefaultListModel;

/**
 * Adds an implementation of Reorderable to the DefaultListModel class.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the type of things stored in the list
 * @author Jonathan Lovelace
 */
public class ReorderableListModel<T> extends DefaultListModel<T> implements Reorderable {
	/**
	 * Move a row of the list from one position to another.
	 *
	 * @param fromIndex the index to remove from
	 * @param toIndex   the index to move to (its index *before* removing the old!)
	 */
	@Override
	public void reorder(final int fromIndex, final int toIndex) {
		if (fromIndex != toIndex) {
			if (fromIndex > toIndex) {
				add(toIndex, remove(fromIndex));
			} else {
				add(toIndex - 1, remove(fromIndex));
			}
		}
	}
	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
