package model.report;

import javax.swing.tree.TreeNode;
import util.NullCleaner;

/**
 * The root of a node hierarchy.
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
public final class RootReportNode extends AbstractReportNode {
	/**
	 * Constructor.
	 *
	 * @param title the title text
	 */
	public RootReportNode(final String title) {
		super(title);
	}

	/**
	 * @return the HTML representation of the tree of nodes.
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
		builder.append("<html>\n<head><title>").append(getText())
				.append("</title></head>\n<body>");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				((AbstractReportNode) child).produce(builder);
			}
		}
		builder.append("</body>\n</html>\n");
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		int retval = 72 + getText().length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				retval += ((AbstractReportNode) child).size();
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
		return (obj instanceof RootReportNode) && getText().equals(obj.getText())
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
