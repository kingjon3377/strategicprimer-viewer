package model.report;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import model.map.Point;
import model.map.PointFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A node to replace usages of null.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public final class EmptyReportNode extends DefaultMutableTreeNode
		implements IReportNode, Iterable<@NonNull IReportNode> {
	/**
	 * Let's make this singleton, to reduce object allocations further.
	 */
	public static final IReportNode NULL_NODE = new EmptyReportNode();
	/**
	 * The point, if any, in the map that this node represents something on.
	 */
	@SuppressWarnings("FieldHasSetterButNoGetter")
	@Nullable
	private final Point point = PointFactory.point(Integer.MIN_VALUE, Integer.MIN_VALUE);

	/**
	 * Constructor.
	 */
	private EmptyReportNode() {
		super("");
		setText("");
		setPoint(null);
	}

	/**
	 * @return the empty string
	 */
	@Override
	public String produce() {
		return "";
	}

	/**
	 * @param builder the string-builder used to build the report
	 * @return it, unmodified
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		return builder;
	}

	/**
	 * @return the number of characters we'll add to the report, namely zero.
	 */
	@Override
	public int size() {
		return 0;
	}

	/**
	 * @return true: this is "the empty node."
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public boolean isEmptyNode() {
		return true;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || (obj instanceof EmptyReportNode);
	}

	/**
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
	 * @param node the node to add
	 */
	@Override
	public void add(final @Nullable MutableTreeNode node) {
		// Do nothing.
	}

	/**
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
	 * @return the text of the node, usually the header.
	 */
	@Override
	public String getText() {
		return "";
	}

	/**
	 * @param txt the new text for the node
	 */
	@Override
	public void setText(final String txt) {
		// Do nothing
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return getText();
	}

	/**
	 * @param pt the point, if any, in the map that this represents something on
	 */
	@Override
	public void setPoint(final Point pt) {
		// Do nothing
	}

	/**
	 * @return the point, if any, in the map that this node in particular represents something on
	 */
	@Override
	@Nullable
	public Point getLocalPoint() {
		return point;
	}
}
