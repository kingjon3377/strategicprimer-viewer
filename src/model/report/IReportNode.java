package model.report;

import javax.swing.tree.MutableTreeNode;
import model.map.Point;
import model.map.PointFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for report nodes.
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
@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public interface IReportNode
		extends Comparable<@NonNull IReportNode>, MutableTreeNode, Iterable<IReportNode> {
	/**
	 * @return the HTML representation of the node.
	 */
	String produce();

	/**
	 * @param builder a string builder
	 * @return that builder, with an HTML representation of the node added.
	 */
	StringBuilder produce(StringBuilder builder);

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
	 * @return the point, if any, in the map that this node, as opposed to any of its
	 * children, represents something on.
	 */
	@Nullable
	Point getLocalPoint();
	/**
	 * @param pt the point, if any, in the map that this represents something on
	 */
	void setPoint(final Point pt);
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
	@Override
	default int compareTo(final IReportNode obj) {
		return produce().compareTo(obj.produce());
	}
}