package model.report;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Iterator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import model.map.Point;
import model.map.PointFactory;
import org.eclipse.jdt.annotation.Nullable;
import util.NoCloneException;

/**
 * A node to replace usages of null.
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
@SuppressWarnings("CloneableClassInSecureContext")
public final class EmptyReportNode extends DefaultMutableTreeNode
		implements IReportNode {
	/**
	 * Let's make this singleton, to reduce object allocations further.
	 */
	public static final IReportNode NULL_NODE = new EmptyReportNode();
	/**
	 * The point, if any, in the map that this node represents something on.
	 */
	private static final Point POINT =
			PointFactory.point(Integer.MIN_VALUE, Integer.MIN_VALUE);

	/**
	 * Constructor.
	 */
	private EmptyReportNode() {
		super("");
	}

	/**
	 * Always returns the empty string.
	 * @return the empty string
	 */
	@Override
	public String produce() {
		return "";
	}

	/**
	 * This is a no-op, since the HTML representation is the empty string.
	 * @param formatter ignored
	 */
	@Override
	public void produce(final Formatter formatter) {
		// do nothing
	}

	/**
	 * Always returns 0.
	 * @return the number of characters we'll add to the report, namely zero.
	 */
	@Override
	public int size() {
		return 0;
	}

	/**
	 * This is the empty node.
	 * @return true: this is "the empty node."
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public boolean isEmptyNode() {
		return true;
	}

	/**
	 * All EmptyReportNodes are equal to each other.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || (obj instanceof EmptyReportNode);
	}

	/**
	 * Our hash code is always 0.
	 * @return a hash code for the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * Add a node. Do nothing if null, rather than crashing.
	 *
	 * @param newChild the node to add
	 */
	@Override
	public void add(final @Nullable MutableTreeNode newChild) {
		// Do nothing.
	}

	/**
	 * Nothing done to an empty node should have any effect.
	 * TODO: should we log a warning?
	 * @param node ignored: nothing done to an empty node has any effect
	 */
	@Override
	public void addAsFirst(final @Nullable MutableTreeNode node) {
		// Do nothing.
	}

	/**
	 * Our iterator is empty.
	 * @return an iterator over the children
	 */
	@Override
	public Iterator<IReportNode> iterator() {
		return Collections.emptyIterator();
	}

	/**
	 * Add generic-type information for the compiler.
	 */
	@Override
	public Enumeration<IReportNode> children() {
		return Collections.emptyEnumeration();
	}

	/**
	 * Return the empty string.
	 * @return the text of the node, usually the header.
	 */
	@Override
	public String getText() {
		return "";
	}

	/**
	 * Do nothing. TODO: should this log a warning?
	 * @param txt the new text for the node
	 */
	@Override
	public void setText(final String txt) {
		// Do nothing
	}

	/**
	 * Returns the empty string. TODO: Inline this call?
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return getText();
	}

	/**
	 * Do nothing. TODO: should we log a warning?
	 * @param pt the point, if any, in the map that this represents something on
	 */
	@Override
	public void setPoint(@Nullable final Point pt) {
		// Do nothing
	}

	/**
	 * Returns our constant null-equivalent Point.
	 * @return the point, if any, in the map that this node in particular represents
	 * something on
	 */
	@Override
	@Nullable
	public Point getLocalPoint() {
		return POINT;
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

	/**
	 * Superclass removes CloneNotSupportedException from method signature, but we still
	 * want to throw it, so we wrap it in a RuntimeException.
	 *
	 * @return never
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public EmptyReportNode clone() {
		throw new NoCloneException("cloning prohibited");
	}
}
