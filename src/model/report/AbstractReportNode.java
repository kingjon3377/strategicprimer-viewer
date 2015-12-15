package model.report;

import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.map.Point;
import model.map.PointFactory;
import util.EnumerationWrapper;

/**
 * A superclass for report-nodes.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractReportNode extends DefaultMutableTreeNode
		implements IReportNode, Iterable<@NonNull AbstractReportNode> {
	/**
	 * The point, if any, in the map that this node represents something on.
	 */
	@Nullable
	private Point point;
	/**
	 * The (usually header) text. May be empty, but not null.
	 */
	private String text;

	/**
	 * Constructor.
	 *
	 * @param pt the point in the map that this node represents something on
	 * @param txt the (header) text.
	 */
	protected AbstractReportNode(final Point pt, final String txt) {
		super(txt);
		text = txt;
		setText(txt);
		point = pt;
	}

	/**
	 * Constructor.
	 *
	 * @param txt the (header) text.
	 */
	protected AbstractReportNode(final String txt) {
		super(txt);
		text = txt;
		setText(txt);
		point = null;
	}
	/**
	 * @return the HTML representation of the node.
	 */
	@Override
	public abstract String produce();

	/**
	 * @param builder a string builder
	 * @return that builder, with an HTML representation of the node added.
	 */
	@Override
	public abstract StringBuilder produce(final StringBuilder builder);

	/**
	 * @return an approximation of how large the HTML produced by this node will
	 *         be.
	 */
	@Override
	public abstract int size();

	/**
	 * @return the text of the node, usually the header.
	 */
	@Override
	public final String getText() {
		return text;
	}

	/**
	 * @param txt the new text for the node
	 */
	@Override
	public final void setText(final String txt) {
		text = txt;
		setUserObject(text);
	}

	/**
	 * @param obj an object to compare to.
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final IReportNode obj) {
		return produce().compareTo(obj.produce());
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof IReportNode
				&& equalsImpl((IReportNode) obj);
	}

	/**
	 * @param obj a node
	 * @return whether it's equal to this one.
	 */
	protected abstract boolean equalsImpl(final IReportNode obj);

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return hashCodeImpl();
	}

	/**
	 * @return a hash code for the object
	 */
	protected abstract int hashCodeImpl();

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return getText();
	}

	/**
	 * Add a node. Do nothing if null, rather than crashing.
	 *
	 * @param node the node to add
	 */
	@Override
	public void add(@Nullable final MutableTreeNode node) {
		if (node != null) {
			if (node instanceof AbstractReportNode) {
				if (!((AbstractReportNode) node).isEmptyNode()) {
					super.add(node);
				}
			} else {
				super.add(node);
			}
		}
	}

	/**
	 * @return the point, if any, in the map that this represents something on
	 */
	public final Point getPoint() {
		if (point != null) {
			return point;
		} else {
			Point locPoint = null;
			for (final AbstractReportNode child : this) {
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
	public final void setPoint(final Point pt) {
		point = pt;
	}
	/**
	 * @return an iterator over the children
	 */
	@Override
	public Iterator<AbstractReportNode> iterator() {
		return new EnumerationWrapper<>(children());
	}
	/**
	 * @return whether this is "the empty node," which should always be ignored.
	 */
	protected boolean isEmptyNode() {
		return false;
	}
}
