package model.report;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import javax.swing.tree.MutableTreeNode;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A node that sorts itself after every addition.
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
@SuppressWarnings({"CloneableClassInSecureContext", "CloneableClassWithoutClone"})
public final class SortedSectionListReportNode extends SectionListReportNode {
	/**
	 * Constructor.
	 *
	 * @param level the header level
	 * @param text  the header text
	 */
	public SortedSectionListReportNode(final int level, final String text) {
		super(level, text);
	}

	/**
	 * Add a node, then sort.
	 *
	 * @param newChild the node to add
	 */
	@SuppressWarnings("unchecked") // Nothing we can do about it ...
	@Override
	public void add(@Nullable final MutableTreeNode newChild) {
		super.add(newChild);
		Collections.sort(children);
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
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
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
