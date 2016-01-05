package model.report;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.tree.MutableTreeNode;
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
public final class EmptyReportNode extends AbstractReportNode implements IReportNode {
	/**
	 * Let's make this singleton, to reduce object allocations further.
	 */
	public static final EmptyReportNode NULL_NODE = new EmptyReportNode();

	/**
	 * Constructor.
	 */
	private EmptyReportNode() {
		super("");
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
	 * @param obj an object
	 * @return whether it equals this; all EmptyReportNodes are equal.
	 */
	@Override
	protected boolean equalsNode(final IReportNode obj) {
		return (this == obj) || (obj instanceof EmptyReportNode);
	}

	/**
	 * @return a constant hash code
	 */
	@Override
	protected int hashCodeImpl() {
		return 0;
	}

	/**
	 * @return true: this is "the empty node."
	 */
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
		return (this == obj) || ((obj instanceof IReportNode)
				                         && equalsNode((IReportNode) obj));
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return hashCodeImpl();
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
}
