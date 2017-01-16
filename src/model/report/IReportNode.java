package model.report;

import java.util.Formatter;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.swing.tree.MutableTreeNode;
import model.map.Point;
import model.map.PointFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.EnumerationWrapper;

/**
 * An interface for report nodes.
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
@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public interface IReportNode
		extends Comparable<@NonNull IReportNode>, MutableTreeNode,
						Iterable<IReportNode> {
	/**
	 * By default delegates to {@link #produce(Formatter)} after calculating the
	 * appropriate size for the StringBuilder.
	 * @return the HTML representation of the node.
	 */
	default String produce() {
		final StringBuilder builder = new StringBuilder(size());
		try (final Formatter formatter = new Formatter(builder)) {
			produce(formatter);
		}
		return builder.toString();
	}

	/**
	 * Write the HTML representation to a Formatter.
	 * @param formatter a Formatter to write the HTML representation to
	 */
	void produce(Formatter formatter);

	/**
	 * How large the HTML representation will be, approximately.
	 * @return an approximation of how large the HTML produced by this node will be.
	 */
	int size();

	/**
	 * The text of this node.
	 * @return the text of the node, usually the header.
	 */
	String getText();

	/**
	 * Set the text for this node.
	 * @param txt the new text for the node
	 */
	void setText(String txt);

	/**
	 * Add a node. Do nothing if null, rather than crashing.
	 *
	 * @param node the node to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void add(@Nullable final MutableTreeNode node);

	/**
	 * Add a node as our first child. Do nothing if null.
	 *
	 * @param node the node to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	default void addAsFirst(@Nullable final MutableTreeNode node) {
		if (isNonEmptyNode(node)) {
			insert(node, 0);
		}
	}

	/**
	 * The point, if any, that this and its children represent something on.
	 * @return the point, if any, in the map that this represents something on
	 */
	default Point getPoint() {
		final Point point = getLocalPoint();
		if (point != null) {
			return point;
		} else {
			Point locPoint = null;
			for (final IReportNode child : this) {
				if (locPoint == null) {
					locPoint = child.getPoint();
				} else if (!locPoint.equals(child.getPoint())) {
					locPoint = PointFactory.point(Integer.MIN_VALUE,
							Integer.MIN_VALUE);
				}
			}
			if (locPoint == null) {
				return PointFactory.point(Integer.MIN_VALUE, Integer.MIN_VALUE);
			} else {
				return locPoint;
			}
		}
	}

	/**
	 * Set the point that this represents something at.
	 * @param pt the point, if any, in the map that this represents something on
	 */
	void setPoint(final Point pt);

	/**
	 * The point, if any, in the map that this node (rather than its children)
	 * represents anything on.
	 * @return the point, if any, in the map that this node, as opposed to any of its
	 * children, represents something on.
	 */
	@Nullable
	Point getLocalPoint();

	/**
	 * Whether this is "the empty node."
	 * @return whether this is "the empty node," which should always be ignored.
	 */
	@SuppressWarnings({"MethodReturnAlwaysConstant", "BooleanMethodIsAlwaysInverted"})
	default boolean isEmptyNode() {
		return false;
	}

	/**
	 * Compare to another node. Note that this is an expensive implementation, producing
	 * and delegating the HTML representation.
	 * @param obj an object to compare to.
	 * @return the result of the comparison
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	default int compareTo(final IReportNode obj) {
		return produce().compareTo(obj.produce());
	}

	/**
	 * Add children iff they have children of their own.
	 * @param children new children to add, each only if it has children of its own
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	default void addIfNonEmpty(final MutableTreeNode... children) {
		for (final MutableTreeNode child : children) {
			if (child.getChildCount() != 0) {
				add(child);
			}
		}
	}
	/**
	 * A stream of the children of this node.
	 * @return a stream over the children of this node
	 */
	default Stream<IReportNode> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
	/**
	 * A node is empty if it is an IReportNode and "the empty node."
	 * @param node a node
	 * @return whether it is either a non-empty IReportNode or not an IReportNode at
	 * all, but not null.
	 */
	static boolean isNonEmptyNode(@Nullable final MutableTreeNode node) {
		if (node instanceof IReportNode) {
			return !((IReportNode) node).isEmptyNode();
		} else {
			return node != null;
		}
	}
	/**
	 * An iterator over the node's children.
	 * @return an iterator over the node's children
	 */
	@SuppressWarnings("unchecked")
	@Override
	default Iterator<IReportNode> iterator() {
		return new EnumerationWrapper<>(children());
	}
}