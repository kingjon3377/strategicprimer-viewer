package model.report;

import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import model.map.Point;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.EnumerationWrapper;

/**
 * A superclass for report-nodes.
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
public abstract class AbstractReportNode extends DefaultMutableTreeNode
		implements IReportNode, Iterable<@NonNull IReportNode> {
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
	 * @param pt  the point in the map that this node represents something on
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
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof IReportNode)
				                         && equalsNode((IReportNode) obj));
	}

	/**
	 * @param obj a node
	 * @return whether it's equal to this one.
	 */
	protected abstract boolean equalsNode(final IReportNode obj);

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
			if (node instanceof IReportNode) {
				if (!((IReportNode) node).isEmptyNode()) {
					super.add(node);
				}
			} else {
				super.add(node);
			}
		}
	}

	/**
	 * @param pt the point, if any, in the map that this represents something on
	 */
	@Override
	public final void setPoint(final Point pt) {
		point = pt;
	}
	/**
	 * @return the point, if any, in the map that this node in particular represents something on
	 */
	@Override
	@Nullable
	public final Point getLocalPoint() {
		return point;
	}
	/**
	 * @return an iterator over the children
	 */
	@Override
	public Iterator<IReportNode> iterator() {
		return new EnumerationWrapper<>(children());
	}

	/**
	 * Add generic-type information for the compiler.
	 */
	@Override
	public Enumeration<IReportNode> children() {
		return super.children();
	}
}
