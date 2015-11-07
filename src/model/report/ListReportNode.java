package model.report;

import javax.swing.tree.TreeNode;

import model.map.Point;
import util.NullCleaner;

/**
 * A node representing a list.
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
 */
public class ListReportNode extends AbstractReportNode {
	/**
	 * The length of the boilerplate even if we have no text and no children.
	 */
	private static final int BOILERPLATE_LEN = "\n<ul>\n</ul>\n".length();

	/**
	 * The estimated size of a child: half a kilobyte, which is absurdly high,
	 * but we *really* don't want to resize the buffer!
	 */
	private static final int CHILD_BUF_SIZE = 512;
	/**
	 * The length of the boilerplate per child.
	 */
	private static final int PER_CHILD_BOILERPLATE = "<li></li>\n".length();
	/**
	 * @param point the point in the map that this represents something on
	 * @param text the header text
	 */
	public ListReportNode(final Point point, final String text) {
		super(point, text);
	}

	/**
	 * @param text the header text
	 */
	public ListReportNode(final String text) {
		super(text);
	}
	/**
	 * @return the HTML representation of the node.
	 */
	@Override
	public String produce() {
		// Assume each child is half a K.
		final StringBuilder builder = new StringBuilder(getText().length()
				+ BOILERPLATE_LEN + getChildCount() * CHILD_BUF_SIZE)
				.append(getText());
		builder.append("\n<ul>\n");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				builder.append("<li>");
				builder.append(((AbstractReportNode) child).produce());
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
		builder.append(getText());
		builder.append("\n<ul>\n");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				builder.append("<li>");
				((AbstractReportNode) child).produce(builder);
				builder.append("</li>\n");
			}
		}
		builder.append("</ul>\n");
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will
	 *         be.
	 */
	@Override
	public int size() {
		int retval = BOILERPLATE_LEN + getText().length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				// ESCA-JAVA0076:
				retval += ((AbstractReportNode) child).size()
						+ PER_CHILD_BOILERPLATE;
			}
		}
		return retval;
	}

	/**
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsImpl(final IReportNode obj) {
		return obj instanceof ListReportNode && getText().equals(obj.getText())
				&& children().equals(obj.children());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return getText().hashCode() /* | children().hashCode() */;
	}
}
