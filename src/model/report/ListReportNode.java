package model.report;

import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import model.map.Point;
import org.eclipse.jdt.annotation.Nullable;
import util.EnumerationWrapper;
import util.NullCleaner;

/**
 * A node representing a list.
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
public final class ListReportNode extends DefaultMutableTreeNode
		implements IReportNode {
	/**
	 * The length of the boilerplate even if we have no text and no children.
	 */
	private static final int BOILERPLATE_LEN = "\n<ul>\n</ul>\n".length();

	/**
	 * The estimated size of a child: half a kilobyte, which is absurdly high, but we
	 * *really* don't want to resize the buffer!
	 */
	private static final int CHILD_BUF_SIZE = 512;
	/**
	 * The length of the boilerplate per child.
	 */
	private static final int PER_CHILD_BOILERPLATE = "<li></li>\n".length();
	/**
	 * The point, if any, in the map that this node represents something on.
	 */
	@SuppressWarnings("FieldHasSetterButNoGetter")
	@Nullable
	private Point point;
	/**
	 * The (usually header) text. May be empty, but not null.
	 */
	private String text;

	/**
	 * @param pt the point in the map that this represents something on
	 * @param txt  the header text
	 */
	public ListReportNode(final Point pt, final String txt) {
		super(txt);
		setText(txt);
		point = pt;
	}

	/**
	 * @param txt the header text
	 */
	public ListReportNode(final String txt) {
		super(txt);
		setText(txt);
		point = null;
	}

	/**
	 * @return the HTML representation of the node.
	 */
	@Override
	public String produce() {
		// Assume each child is half a K.
		final StringBuilder builder = new StringBuilder(text.length() + BOILERPLATE_LEN +
				                                                (getChildCount() *
						                                                 CHILD_BUF_SIZE))
				                              .append(text);
		builder.append("\n<ul>\n");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof IReportNode) {
				builder.append("<li>");
				builder.append(((IReportNode) child).produce());
				builder.append("</li>\n");
			}
		}
		builder.append("</ul>\n");
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		builder.append(text);
		builder.append("\n<ul>\n");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof IReportNode) {
				builder.append("<li>");
				((IReportNode) child).produce(builder);
				builder.append("</li>\n");
			}
		}
		builder.append("</ul>\n");
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		int retval = BOILERPLATE_LEN + text.length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof IReportNode) {
				retval += ((IReportNode) child).size()
								  + PER_CHILD_BOILERPLATE;
			}
		}
		return retval;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof ListReportNode) &&
				                         text.equals(((IReportNode) obj).getText()) &&
				                         children()
						                         .equals(((IReportNode) obj).children()));
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return text.hashCode();
	}

	/**
	 * Add a node. Do nothing if null, rather than crashing.
	 *
	 * @param node the node to add
	 */
	@Override
	public void add(final @Nullable MutableTreeNode node) {
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
	 * @return an iterator over the children
	 */
	@Override
	public Iterator<IReportNode> iterator() {
		return new EnumerationWrapper<>(children());
	}

	/**
	 * Add generic-type information for the compiler.
	 */
	@SuppressWarnings({"unchecked", "EmptyMethod"})
	@Override
	public Enumeration<IReportNode> children() {
		return super.children();
	}

	/**
	 * @return the text of the node, usually the header.
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * @param txt the new text for the node
	 */
	@Override
	public void setText(final String txt) {
		text = txt;
		setUserObject(text);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return text;
	}

	/**
	 * @param pt the point, if any, in the map that this represents something on
	 */
	@Override
	public void setPoint(final Point pt) {
		point = pt;
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
