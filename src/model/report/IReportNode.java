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
	 * @param formatter a Formatter to write the HTML representation to
	 */
	void produce(Formatter formatter);

	/**
	 * @return an approximation of how large the HTML produced by this node will be.
	 */
	int size();

	/**
	 * @return the text of the node, usually the header.
	 */
	String getText();

	/**
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
	 * @param pt the point, if any, in the map that this represents something on
	 */
	void setPoint(final Point pt);

	/**
	 * @return the point, if any, in the map that this node, as opposed to any of its
	 * children, represents something on.
	 */
	@Nullable
	Point getLocalPoint();

	/**
	 * @return whether this is "the empty node," which should always be ignored.
	 */
	@SuppressWarnings({"MethodReturnAlwaysConstant", "BooleanMethodIsAlwaysInverted"})
	default boolean isEmptyNode() {
		return false;
	}

	/**
	 * @param obj an object to compare to.
	 * @return the result of the comparison
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	default int compareTo(final IReportNode obj) {
		return produce().compareTo(obj.produce());
	}

	/**
	 * @param children new children to add, each only if it has children of its own
	 */
	default void addIfNonEmpty(final MutableTreeNode... children) {
		for (final MutableTreeNode child : children) {
			if (child.getChildCount() != 0) {
				add(child);
			}
		}
	}
	/**
	 * @return a stream over the children of this node
	 */
	default Stream<IReportNode> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
	/**
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
	 * @return an iterator over the node's children
	 */
	@Override
	default Iterator<IReportNode> iterator() {
		return new EnumerationWrapper<>(children());
	}
}