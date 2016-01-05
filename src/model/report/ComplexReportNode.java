package model.report;

import javax.swing.tree.TreeNode;
import model.map.Point;
import util.NullCleaner;

/**
 * A node for cases slightly more complex than a {@link SimpleReportNode} covers: the text
 * here isn't really a header, and no wrapping children as a list, but we can *have*
 * children. For example, when a report needs to have multiple lists, each with its own
 * header.
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
public final class ComplexReportNode extends AbstractReportNode {
	/**
	 * @param text  the main text of the node
	 * @param point the point, if any, in the map that this represents something on
	 */
	public ComplexReportNode(final Point point, final String text) {
		super(point, text);
	}

	/**
	 * @param text the main text of the node
	 */
	public ComplexReportNode(final String text) {
		super(text);
	}

	/**
	 * @return an HTML representation of the node.
	 */
	@Override
	public String produce() {
		return NullCleaner.assertNotNull(produce(new StringBuilder(size()))
				                                 .toString());
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		builder.append(getText());
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode node = getChildAt(i);
			if (node instanceof IReportNode) {
				((IReportNode) node).produce(builder);
			}
		}
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		int retval = getText().length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof IReportNode) {
				retval += ((IReportNode) child).size();
			}
		}
		return retval;
	}

	/**
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsNode(final IReportNode obj) {
		return (obj instanceof ComplexReportNode)
				       && getText().equals(obj.getText())
				       && children().equals(obj.children());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return getText().hashCode();
	}
}
